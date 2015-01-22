package com.mobilejazz.coltrane.provider.dropbox;

import android.webkit.MimeTypeMap;

import com.dropbox.sync.android.DbxFileInfo;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.DocumentAccessor;

public class FileAccessor extends DocumentAccessor<DbxFileInfo> {

    @Override
    protected String getDocumentId(DbxFileInfo item) {
        return item.path.toString();
    }

    @Override
    protected String getMimeType(DbxFileInfo item) {
        if (item.isFolder) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            String ext = MimeTypeMap.getFileExtensionFromUrl(item.path.getName());
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
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
