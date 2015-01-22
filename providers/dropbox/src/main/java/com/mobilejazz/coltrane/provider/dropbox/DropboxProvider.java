package com.mobilejazz.coltrane.provider.dropbox;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.action.PendingAction;
import com.mobilejazz.coltrane.library.utils.ListCursor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public PendingAction linkAccount() {
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

    public static class Document {

        private DropboxRoot mRoot;
        private String mDriveId;

        private FileAccessor mAccessor;
        private DbxPath mPath;

        private void init(DropboxRoot root, String driveId) {
            mRoot = root;
            mDriveId = driveId;
            mPath = new DbxPath(mDriveId);
            mAccessor = new FileAccessor(mRoot);
        }

        public Document(DropboxRoot root, String driveId) {
            init(root, driveId);
        }

        public Document(Map<String, DropboxRoot> roots, String documentId) {
            String[] ids = documentId.split(":");
            init(roots.get(ids[0]), ids[1]);
        }

        public DropboxRoot getRoot() {
            return mRoot;
        }

        public String getDriveId() {
            return mDriveId;
        }

        public String getDocumentId() {
            return getDocumentId(mRoot, mDriveId);
        }

        public DbxPath getPath() {
            return mPath;
        }

        public FileAccessor getAccessor() {
            return mAccessor;
        }

        public static String getDocumentId(DropboxRoot root, String driveId) {
            return root.getId() + ":" + driveId;
        }

        public static String getPathString(DbxPath path) {
//            if (path == null) {
//                return "";
//            } else if (path.equals(DbxPath.ROOT)) {
//                return "/";
//            } else {
//                return getPathString(path.getParent()) + "/" + path.getName();
//            }
            if (path.equals(DbxPath.ROOT)) {
                return "/";
            } else {
                return path.toString();
            }
        }

        public static String getDocumentId(DropboxRoot root, DbxPath path) {
            return getDocumentId(root, getPathString(path));
        }

    }

}
