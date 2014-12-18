package com.mobilejazz.coltrane.provider.gdrive;

import android.provider.BaseColumns;

import com.google.api.services.drive.model.File;
import static com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Document;

import com.mobilejazz.coltrane.library.compatibility.MatrixCursor;

import java.util.Collection;

public class FileCursor extends MatrixCursor {

    private static final String[] sColumnNames = new String[] {
            BaseColumns._ID,
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_SUMMARY,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_ICON,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    public FileCursor(GoogleDriveProvider.GDriveRoot root, Collection<File> files) {
        super(sColumnNames, files.size());
        int index = 0;
        for (File f : files) {
            int flags = 0;
            if (f.getEditable()) {
                flags |= Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_RENAME;
            }
            if (f.getThumbnailLink() != null) {
                flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
            }

            String mimeType = f.getMimeType();
            if (f.getMimeType().equals(GoogleDriveProvider.FOLDER_MIME_TYPE)) {
                mimeType = Document.MIME_TYPE_DIR;
            }

            addRow(new Object[] {
                    index++,
                    GoogleDriveProvider.Document.getDocumentId(root, f.getId()),
                    mimeType,
                    f.getTitle(),
                    f.getDescription(),
                    f.getModifiedDate().getValue(),
                    0,
                    flags,
                    f.getFileSize()
            });
        }
    }
}
