package com.mobilejazz.coltrane.library;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.mobilejazz.coltrane.library.utils.RootCursor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public abstract class DocumentsProvider {

    private static final int MATCH_ROOTS = 1;
    private static final int MATCH_ROOT = 2;
    private static final int MATCH_RECENT = 3;
    private static final int MATCH_SEARCH = 4;
    private static final int MATCH_DOCUMENT = 5;
    private static final int MATCH_CHILDREN = 6;
    private static final int MATCH_DOCUMENT_TREE = 7;
    private static final int MATCH_CHILDREN_TREE = 8;

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public DocumentsProvider(Context context) {
        mContext = context;
    }

    public abstract Cursor queryRoots(String[] projection) throws FileNotFoundException;

    public abstract Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException;

    public abstract Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException;

    public abstract ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException;

    public abstract String getId();

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

    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        throw new UnsupportedOperationException("Document thumbnails not supported");
    }

    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Create not supported");
    }

    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Rename not supported");
    }

    public void deleteDocument(String documentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Delete not supported");
    }

    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Recent documents not supported");
    }

    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Search not supported");
    }

    public abstract Uri getContentUri(String documentId);

    public boolean onCreate() {
        return true;
    }

}
