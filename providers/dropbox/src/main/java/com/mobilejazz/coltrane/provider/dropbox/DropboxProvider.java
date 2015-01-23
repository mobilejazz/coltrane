package com.mobilejazz.coltrane.provider.dropbox;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.action.PendingAction;
import com.mobilejazz.coltrane.library.utils.FileUtils;
import com.mobilejazz.coltrane.library.utils.ListCursor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;

public class DropboxProvider extends DocumentsProvider {

    public static final String ID = "com.mobilejazz.coltrane.provider.dropbox";

    private DbxAccountManager mAccountManager;
    private Map<String, DropboxRoot> mRoots;

    public DropboxProvider(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {
        super.onCreate();
        mAccountManager = DbxAccountManager.getInstance(getContext(), getContext().getString(R.string.dropbox_api_key), getContext().getString(R.string.dropbox_api_secret));
        return true;
    }

    @Override
    public Collection<? extends Root> getRoots() throws FileNotFoundException {
        if (mRoots == null) {
            mRoots = new TreeMap<String, DropboxRoot>();
            try {
                for (DbxAccount account : mAccountManager.getLinkedAccounts()) {
                    waitForAccountInfo(account);
                    DbxFileSystem fs = DbxFileSystem.forAccount(account);
                    DropboxRoot root = new DropboxRoot(fs);
                    mRoots.put(root.getId(), root);
                }
            } catch (DbxException.Unauthorized e) {
                throw new FileNotFoundException(e.getLocalizedMessage());
            }
        }
        return mRoots.values();
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, parentDocumentId);
            List<DbxFileInfo> files = d.getRoot().getFileSystem().listFolder(d.getPath());
            return new ListCursor<DbxFileInfo>(files, d.getAccessor(), projection, sortOrder);
        } catch (DbxException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            DbxFileInfo file = d.getRoot().getFileSystem().getFileInfo(d.getPath());
            return new ListCursor<DbxFileInfo>(Collections.singletonList(file), d.getAccessor(), projection, null);
        } catch (DbxException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            DbxFile file = d.getRoot().getFileSystem().open(d.getPath());
            FileInputStream in = file.getReadStream();
            ParcelFileDescriptor result = descriptorFromInputStream(documentId, in, mode);
            file.close();
            return result;
            // TODO make this work: return new DbxParcelFileDescriptor(ParcelFileDescriptor.dup(fd), file);
        } catch (DbxException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        } catch (IOException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    protected String getDocumentThumbnailId(final String documentId, final Point sizeHint) {
        return getTemporaryFileName(documentId) + "_" + sizeHint.x + "_" + sizeHint.y;
    }

    protected File getDocumentThumbnailFile(Document document, final Point sizeHint) throws FileNotFoundException {
        FileOutputStream out = null;
        try {
            File cacheDir = getContext().getCacheDir();
            File thumbnail = File.createTempFile(getDocumentThumbnailId(document.getDocumentId(), sizeHint), "", cacheDir);

            DbxFile dbxThumbnail = document.getRoot().getFileSystem().openThumbnail(document.getPath(), getThumbnailSize(sizeHint), DbxFileSystem.ThumbFormat.JPG);
            FileUtils.copyStream(dbxThumbnail.getReadStream(), new FileOutputStream(thumbnail));

            dbxThumbnail.close();
            return thumbnail;
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("Error writing thumbnail", e);
            return null;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e("Error closing thumbnail", e);
                }
        }
    }

    @Override
    public Uri getDocumentThumbnailUri(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            if (d.getRoot().getFileSystem().getFileInfo(d.getPath()).thumbExists) {
                return Uri.fromFile(getDocumentThumbnailFile(d, sizeHint));
            } else {
                return null;
            }
        } catch (DbxException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public PendingAction linkAccount() {
        mRoots = null; // clear roots so that they are refreshed when requeried
        return new LinkAction(mAccountManager);
    }

    @Override
    public String getName() {
        return getContext().getString(R.string.dropbox_name);
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_provider_dropbox;
    }

    public static void register(Context context) {
        DocumentsProviderRegistry.get().register(ID, new DropboxProvider(context));
    }

    private void waitForAccountInfo(DbxAccount account) {
        try {
            synchronized (account) {
                account.addListener(mAccountListener);
                account.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private DbxFileSystem.ThumbSize getThumbnailSize(Point sizeHint) {
        if (sizeHint.x >= 1024 || sizeHint.y >= 768) {
            return DbxFileSystem.ThumbSize.XL;
        } else if (sizeHint.x >= 640 || sizeHint.y >= 480) {
            return DbxFileSystem.ThumbSize.L;
        } else if (sizeHint.x >= 128 || sizeHint.y >= 128) {
            return DbxFileSystem.ThumbSize.M;
        } else if (sizeHint.x >= 64 || sizeHint.y >= 64) {
            return DbxFileSystem.ThumbSize.S;
        } else {
            return DbxFileSystem.ThumbSize.XS;
        }
    }

    public class DropboxRoot extends Root {

        private DbxFileSystem mFileSystem;

        public DropboxRoot(DbxFileSystem fileSystem) {
            super(DropboxProvider.this, fileSystem.getAccount().getUserId(), null, fileSystem.getAccount().getAccountInfo().displayName, R.drawable.ic_provider_dropbox, 0, 0);
            mFileSystem = fileSystem;
            setDocumentId(Document.getDocumentId(this, DbxPath.ROOT));
        }

        public DbxFileSystem getFileSystem() {
            return mFileSystem;
        }

    }

    private DbxAccount.Listener mAccountListener = new DbxAccount.Listener() {
        @Override
        public void onAccountChange(DbxAccount dbxAccount) {
            synchronized (dbxAccount) {
                dbxAccount.notify();
            }
        }
    };

}
