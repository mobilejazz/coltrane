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

    private DbxAccountManager mAccountManager;
    private Map<String, DropboxRoot> mRoots;
    private FileAccessor mAccessor;

    public DropboxProvider(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {
        super.onCreate();
        mAccountManager = DbxAccountManager.getInstance(getContext(), getContext().getString(R.string.dropbox_api_key), getContext().getString(R.string.dropbox_api_secret));
        mAccessor = new FileAccessor();
        return true;
    }

    @Override
    public Collection<? extends Root> getRoots() throws FileNotFoundException {
        if (mRoots == null) {
            mRoots = new TreeMap<String, DropboxRoot>();

            try {
                List<DropboxRoot> roots = new ArrayList<DropboxRoot>();
                for (DbxAccount account : mAccountManager.getLinkedAccounts()) {
                    DbxFileSystem fs = DbxFileSystem.forAccount(account);
                    roots.add(new DropboxRoot(fs));
                }
                return roots;
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
            return new ListCursor<DbxFileInfo>(files, mAccessor, projection, sortOrder);
        } catch (DbxException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException, UserRecoverableException {
        try {
            Document d = new Document(mRoots, documentId);
            DbxFileInfo file = d.getRoot().getFileSystem().getFileInfo(d.getPath());
            return new ListCursor<DbxFileInfo>(Collections.singletonList(file), mAccessor, projection, null);
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
        return null;
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

    public class DropboxRoot extends Root {

        private DbxFileSystem mFileSystem;

        public DropboxRoot(DbxFileSystem fileSystem) {
            super(DropboxProvider.this, fileSystem.getAccount().getUserId(), DbxPath.ROOT.getName(), fileSystem.getAccount().getUserId(), R.drawable.ic_provider_dropbox, 0, 0);
            mFileSystem = fileSystem;
        }

        public DbxFileSystem getFileSystem() {
            return mFileSystem;
        }

    }

    public static class Document {

        private DropboxRoot mRoot;
        private String mDriveId;

        private DbxPath mPath;

        public Document(DropboxRoot root, String driveId) {
            mRoot = root;
            mDriveId = driveId;
            mPath = new DbxPath(driveId);
        }

        public Document(Map<String, DropboxRoot> roots, String documentId) {
            String[] ids = documentId.split(":");
            mRoot = roots.get(ids[0]);
            mDriveId = ids[1];
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

        public static String getDocumentId(DropboxRoot root, String driveId) {
            return root.getId() + ":" + driveId;
        }

    }

}
