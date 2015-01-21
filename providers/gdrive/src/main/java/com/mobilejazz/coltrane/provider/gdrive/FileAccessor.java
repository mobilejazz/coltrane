package com.mobilejazz.coltrane.provider.gdrive;

import com.google.api.services.drive.model.File;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.DocumentAccessor;

public class FileAccessor extends DocumentAccessor<File> {

    private GoogleDriveProvider.GDriveRoot mRoot;

    public FileAccessor(GoogleDriveProvider.GDriveRoot root) {
        mRoot = root;
    }

    @Override
    protected String getDocumentId(File item) {
        return GoogleDriveProvider.Document.getDocumentId(mRoot, item.getId());
    }

    @Override
    protected String getMimeType(File item) {
        String mimeType = item.getMimeType();
        if (item.getMimeType().equals(GoogleDriveProvider.FOLDER_MIME_TYPE)) {
            mimeType = DocumentsContract.Document.MIME_TYPE_DIR;
        }
        return mimeType;
    }

    @Override
    protected String getDisplayName(File item) {
        return item.getTitle();
    }

    @Override
    protected String getSummary(File item) {
        return item.getDescription();
    }

    @Override
    protected long getLastModified(File item) {
        return item.getModifiedDate().getValue();
    }

    @Override
    protected int getIcon(File item) {
        return 0;
    }

    @Override
    protected int getFlags(File item) {
        int flags = 0;
        if (item.getEditable()) {
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_WRITE | DocumentsContract.Document.FLAG_SUPPORTS_DELETE | DocumentsContract.Document.FLAG_SUPPORTS_RENAME;
        }
        if (item.getThumbnailLink() != null) {
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
        }
        return flags;
    }

    @Override
    protected long getSize(File item) {
        Long size = item.getFileSize();
        if (size != null) {
            return size;
        } else {
            return 0;
        }
    }

}
