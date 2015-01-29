package com.mobilejazz.coltrane.provider.dropbox;

import com.dropbox.sync.android.DbxPath;

import java.util.Map;

public class Document {

        private DropboxProvider.DropboxRoot mRoot;
        private String mDriveId;

        private FileAccessor mAccessor;
        private DbxPath mPath;

        private void init(DropboxProvider.DropboxRoot root, String driveId) {
            mRoot = root;
            mDriveId = driveId;
            mPath = new DbxPath(mDriveId);
            mAccessor = new FileAccessor(mRoot);
        }

        public Document(DropboxProvider.DropboxRoot root, String driveId) {
            init(root, driveId);
        }

        public Document(Map<String, DropboxProvider.DropboxRoot> roots, String documentId) {
            String[] ids = documentId.split(":");
            init(roots.get(ids[0]), ids[1]);
        }

        public DropboxProvider.DropboxRoot getRoot() {
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

        public static String getDocumentId(DropboxProvider.DropboxRoot root, String driveId) {
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

        public static String getDocumentId(DropboxProvider.DropboxRoot root, DbxPath path) {
            return getDocumentId(root, getPathString(path));
        }

    }