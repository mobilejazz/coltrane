package com.mobilejazz.coltrane.provider.gdrive;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.LruCache;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.action.IntentPendingAction;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.ListCursor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GoogleDriveProvider extends DocumentsProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private static final String ROOT_FOLDER = "root";

    private static final int REQUEST_RESOLVE_AUTH_ISSUE = 5469;

    public static class GDriveRoot extends Root {

        private Account mAccount;
        private Drive mDrive;

        private Document mRootDocument;

        public GDriveRoot(GoogleDriveProvider provider, Account account, Drive drive) {
            mAccount = account;
            mDrive = drive;

            setId(account.name);
            setTitle(account.name);
            setFlags(0); // TODO
            setIcon(R.drawable.ic_provider_gdrive);
            setProvider(provider);

            mRootDocument = new Document(this, ROOT_FOLDER);
            setDocumentId(mRootDocument.getDocumentId());
        }

        public Account getAccount() {
            return mAccount;
        }

        public Drive getDrive() {
            return mDrive;
        }

        public Document getRootDocument() {
            return mRootDocument;
        }

        @Override
        public void update() {
            try {
                About about = mDrive.about().get().execute();
                setAvailableBytes(about.getQuotaBytesTotal());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static class Document {

        private FileAccessor mAccessor;
        private GDriveRoot mRoot;
        private String mDriveId;

        public Document(GDriveRoot root, String driveId) {
            mRoot = root;
            mDriveId = driveId;
            mAccessor = new FileAccessor(mRoot);
        }

        public Document(Map<String, GDriveRoot> roots, String documentId) {
            String[] ids = documentId.split(":");
            mRoot = roots.get(ids[0]);
            mDriveId = ids[1];
            mAccessor = new FileAccessor(mRoot);
        }

        public GDriveRoot getRoot() {
            return mRoot;
        }

        public FileAccessor getAccessor() {
            return mAccessor;
        }

        public String getDriveId() {
            return mDriveId;
        }

        public String getDocumentId() {
            return getDocumentId(mRoot, mDriveId);
        }

        public static String getDocumentId(GDriveRoot root, String driveId) {
            return root.getAccount().name + ":" + driveId;
        }

    }

    public static final String ID = "com.mobilejazz.coltrane.provider.gdrive";

    private GoogleApiClient mGoogleApiClient;

    private ConnectionResult mConnectionResult;

    private Map<String, GDriveRoot> mRoots;

    private LruCache<String, File> mDocumentCache = new LruCache<String, File>(256);
    private LruCache<String, List<File>> mChildrenCache = new LruCache<String, List<File>>(1024) {

        @Override
        protected int sizeOf(String key, List<File> value) {
            return value.size();
        }
    };

    private boolean isFolder(File f) {
        return f.getMimeType().equals(GoogleDriveProvider.FOLDER_MIME_TYPE);
    }

    public GoogleDriveProvider(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    protected GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    @Override
    public Collection<? extends Root> getRoots() throws FileNotFoundException {
        if (mRoots == null) {
            mRoots = new TreeMap<String, GDriveRoot>();

            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE));
            Account[] accounts = credential.getAllAccounts();
            for (Account a : accounts) {
                credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE));
                credential.setSelectedAccountName(a.name);
                Drive service = new Drive.Builder(new NetHttpTransport(), new AndroidJsonFactory(), credential).setApplicationName(getContext().getApplicationInfo().name).build();
                mRoots.put(a.name, new GDriveRoot(this, a, service));
            }
        }
        return mRoots.values();
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, parentDocumentId);
            List<File> files = mChildrenCache.get(parentDocumentId);
            if (files == null) {
                files = d.getRoot().getDrive().files().list().setQ("'" + d.getDriveId() + "'" + " in parents and trashed=false and not (mimeType != 'application/vnd.google-apps.folder' and mimeType contains 'application/vnd.google-apps')").execute().getItems();
                mChildrenCache.put(parentDocumentId, files);
            }
            return new ListCursor<File>(files, d.getAccessor(), projection, sortOrder);
        } catch (UserRecoverableAuthIOException e) {
            throw new UserRecoverableException(e.getLocalizedMessage(), e, new IntentPendingAction(e.getIntent()));
        } catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            File file = mDocumentCache.get(documentId);
            if (file == null) {
                file = d.getRoot().getDrive().files().get(d.getDriveId()).execute();
                mDocumentCache.put(documentId, file);
            }
            return new ListCursor<File>(Collections.singletonList(file), d.getAccessor(), projection, null);
        } catch (UserRecoverableAuthIOException e) {
            throw new UserRecoverableException(e.getLocalizedMessage(), e, new IntentPendingAction(e.getIntent()));
        }  catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        OutputStream out = null;
        try {
            Document d = new Document(mRoots, documentId);
            File file = d.getRoot().getDrive().files().get(d.getDriveId()).execute();
            HttpResponse resp =
                    d.getRoot().getDrive().getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
                            .execute();

            java.io.File downloadedFile = java.io.File.createTempFile(documentId, "_thumbnail", getContext().getCacheDir());
            out = new FileOutputStream(downloadedFile);
            resp.download(out);

            final boolean isWrite = (mode.indexOf('w') != -1);
            if (isWrite) {
                return ParcelFileDescriptor.open(downloadedFile, ParcelFileDescriptor.MODE_READ_WRITE);
            } else {
                return ParcelFileDescriptor.open(downloadedFile, ParcelFileDescriptor.MODE_READ_ONLY);
            }
        } catch (UserRecoverableAuthIOException e) {
            throw new UserRecoverableException(e.getLocalizedMessage(), e, new IntentPendingAction(e.getIntent()));
        }  catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new FileNotFoundException(e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public Uri getDocumentThumbnailUri(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            File file = d.getRoot().getDrive().files().get(d.getDriveId()).execute();
            String thumbnailLink = file.getThumbnailLink();
            return Uri.parse(thumbnailLink.substring(0, thumbnailLink.length() - 4) + "s" + Math.max(sizeHint.x, sizeHint.y));
        } catch (UserRecoverableAuthIOException e) {
            throw new UserRecoverableException(e.getLocalizedMessage(), e, new IntentPendingAction(e.getIntent()));
        }  catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static void register(Context context) {
        DocumentsProviderRegistry.get().register(ID, new GoogleDriveProvider(context));
    }

}
