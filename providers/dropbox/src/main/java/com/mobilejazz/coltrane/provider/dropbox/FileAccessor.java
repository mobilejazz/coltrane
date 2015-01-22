package com.mobilejazz.coltrane.provider.dropbox;

import android.webkit.MimeTypeMap;

import com.dropbox.sync.android.DbxFileInfo;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.DocumentAccessor;

public class FileAccessor extends DocumentAccessor<DbxFileInfo> {

    private DropboxProvider.DropboxRoot mRoot;

    public FileAccessor(DropboxProvider.DropboxRoot root) {
        mRoot = root;
    }

    @Override
    protected String getDocumentId(DbxFileInfo item) {
        return DropboxProvider.Document.getDocumentId(mRoot, item.path);
    }

    @Override
    protected String getMimeType(DbxFileInfo item) {
        if (item.isFolder) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            String itemFileName = item.path.getName();
            final int lastDot = itemFileName.lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = itemFileName.substring(lastDot + 1);
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                return mime;
            }  else {
                return null;
            }
        }
    }

    @Override
    protected String getDisplayName(DbxFileInfo item) {
        return item.path.getName();
    }

    @Override
    protected String getSummary(DbxFileInfo item) {
        return null;
    }

    @Override
    protected long getLastModified(DbxFileInfo item) {
        return item.modifiedTime.getTime();
    }

    @Override
    protected int getIcon(DbxFileInfo item) {
        return 0;
    }

    @Override
    protected int getFlags(DbxFileInfo item) {
        int flags = DocumentsContract.Document.FLAG_SUPPORTS_WRITE | DocumentsContract.Document.FLAG_SUPPORTS_DELETE | DocumentsContract.Document.FLAG_SUPPORTS_RENAME; // todo: for now assume
        if (item.thumbExists) {
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
        }
        return flags;
    }

    @Override
    protected long getSize(DbxFileInfo item) {
        return item.size;
    }

}
