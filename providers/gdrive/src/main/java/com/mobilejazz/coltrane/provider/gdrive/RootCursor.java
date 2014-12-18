package com.mobilejazz.coltrane.provider.gdrive;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.compatibility.MatrixCursor;

import java.util.Collection;

public class RootCursor extends MatrixCursor {

    private static final String[] columnNames = new String[] {
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_FLAGS
    };

    public RootCursor(Collection<GoogleDriveProvider.GDriveRoot> roots) {
        super(columnNames, roots.size());
        for (GoogleDriveProvider.GDriveRoot r : roots) {
            addRow(new Object[] {
                    r.getAccount().name,
                    r.getRootDocument().getDocumentId(),
                    r.getAccount().name,
                    R.drawable.ic_provider,
                    0 // TODO
            });
        }
    }
}
