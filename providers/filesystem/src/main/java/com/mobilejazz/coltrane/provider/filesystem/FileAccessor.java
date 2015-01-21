package com.mobilejazz.coltrane.provider.filesystem;

import android.webkit.MimeTypeMap;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.DocumentAccessor;
import com.mobilejazz.coltrane.library.utils.thumbnail.Thumbnail;

import java.io.File;

public class FileAccessor extends DocumentAccessor<File> {

    @Override
    protected String getDocumentId(File item) {
        return item.getAbsolutePath();
    }

    @Override
    protected String getMimeType(File item) {
        if (item.isDirectory())
            return DocumentsContract.Document.MIME_TYPE_DIR;
        // From FileProvider.getType(Uri)
        final int lastDot = item.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = item.getName().substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    @Override
    protected String getDisplayName(File item) {
        return item.getName();
    }

    @Override
    protected String getSummary(File item) {
        return null;
    }

    @Override
    protected long getLastModified(File item) {
        return item.lastModified();
    }

    @Override
    protected int getIcon(File item) {
        return 0;
    }

    @Override
    protected int getFlags(File item) {
        int flags = item.canWrite() ? DocumentsContract.Document.FLAG_SUPPORTS_DELETE | DocumentsContract.Document.FLAG_SUPPORTS_WRITE
                : 0;
        if (Thumbnail.isSupported(getMimeType(item)))
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
        return flags;
    }

    @Override
    protected long getSize(File item) {
        return item.length();
    }

}
