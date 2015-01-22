package com.mobilejazz.coltrane.library;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.mobilejazz.coltrane.library.action.PendingAction;

import java.io.FileNotFoundException;
import java.util.Collection;

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

    public abstract Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException, UserRecoverableException;

    public abstract Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException, UserRecoverableException;

    public abstract ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException;

    public abstract String getId();

    public abstract Collection<? extends Root> getRoots() throws FileNotFoundException;

    public abstract String getName();

    public abstract int getIcon();

    public Uri getDocumentThumbnailUri(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Document thumbnails not supported");
    }

    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Create not supported");
    }

    public String renameDocument(String documentId, String displayName) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Rename not supported");
    }

    public void deleteDocument(String documentId) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Delete not supported");
    }

    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Recent documents not supported");
    }

    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException, UserRecoverableException {
        throw new UnsupportedOperationException("Search not supported");
    }

    /**
     * Adds a new account for this provider.
     *
     * @return A {@link PendingAction} that needs to be executed in order to add a new account. A
     * provider returns {@code null}, if it doesn't support adding accounts.
     */
    public PendingAction linkAccount() {
        return null; // not supported
    }

    public boolean onCreate() {
        return true;
    }

}
