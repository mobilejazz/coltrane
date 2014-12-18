package com.mobilejazz.coltrane.provider.gdrive;

import com.google.api.services.drive.model.File;
import static com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Document;
import com.mobilejazz.coltrane.library.compatibility.MatrixCursor;

import java.util.Collection;

public class FileCursor extends MatrixCursor {

    private static final String[] sColumnNames = new String[] {
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
        for (File f : files) {
            int flags = 0;
            if (f.getEditable()) {
                flags |= Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_RENAME;
            }
            if (f.getThumbnailLink() != null) {
                flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
            }
            addRow(new Object[] {
                    GoogleDriveProvider.Document.getDocumentId(root, f.getId()),
                    f.getMimeType(),
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
