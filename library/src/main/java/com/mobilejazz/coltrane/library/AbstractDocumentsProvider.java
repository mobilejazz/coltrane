package com.mobilejazz.coltrane.library;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.CancellationSignal;

import com.mobilejazz.coltrane.library.utils.RootCursor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public abstract class AbstractDocumentsProvider implements DocumentsProvider {

    @Override
    public List<Root> getRoots() {
        List<Root> result = new ArrayList<Root>();
        try {
            RootCursor c = new RootCursor(queryRoots(null));
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result.add(new Root(this, c.getId(), c.getDocumentId(), c.getTitle(), c.getIcon()));
                c.moveToNext();
            }
        } catch (FileNotFoundException e) {
            Timber.e(e, e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        throw new UnsupportedOperationException("Document thumbnails not supported");
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Create not supported");
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Rename not supported");
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Recent documents not supported");
    }

    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Search not supported");
    }

}
