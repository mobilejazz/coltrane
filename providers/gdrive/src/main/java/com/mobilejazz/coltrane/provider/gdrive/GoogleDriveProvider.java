package com.mobilejazz.coltrane.provider.gdrive;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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

        public GDriveRoot(GoogleDriveProvider provider, Account account, Drive drive) throws IOException {
            mAccount = account;
            mDrive = drive;

            setId(account.name);
            setTitle(account.name);
            setFlags(0); // TODO
            setIcon(R.drawable.ic_provider_gdrive);
            setProvider(provider);

            // check connection:
            update();
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
                mRootDocument = new Document(this, about.getRootFolderId());
                setDocumentId(mRootDocument.getDocumentId());
                setPendingAction(null);
            } catch (UserRecoverableAuthIOException e) {
                setPendingAction(e.getIntent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static class Document {

        private GDriveRoot mRoot;
        private String mDriveId;

        public Document(GDriveRoot root, String driveId) {
            mRoot = root;
            mDriveId = driveId;
        }

        public Document(Map<String, GDriveRoot> roots, String documentId) {
            String[] ids = documentId.split(":");
            mRoot = roots.get(ids[0]);
            mDriveId = ids[1];
        }

        public GDriveRoot getRoot() {
            return mRoot;
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
        try {
            if (mRoots == null) {
                mRoots = new TreeMap<String, GDriveRoot>();

                Account[] accounts = AccountManager.get(getContext()).getAccountsByType("com.google");
                for (Account a : accounts) {
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE));
                    credential.setSelectedAccountName(a.name);
                    Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), credential).build();
                    try {
                        mRoots.put(a.name, new GDriveRoot(this, a, service));
                    } catch (UserRecoverableAuthIOException e) {
                        mRoots.put(a.name, new GDriveRoot(this, a, null));
                    }
                }
            }
            return mRoots.values();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        try {
            Document d = new Document(mRoots, parentDocumentId);
            List<File> files = d.getRoot().getDrive().files().list().setQ("'" + d.getDriveId() + "'" + " in parents and trashed=false").execute().getItems();
            return new FileCursor(d.getRoot(), files);
        } catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        try {
            Document d = new Document(mRoots, documentId);
            File file = d.getRoot().getDrive().files().get(d.getDriveId()).execute();
            return new FileCursor(d.getRoot(), Collections.singletonList(file));
        } catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return null;
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
