package com.mobilejazz.coltrane.library;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.compatibility.MatrixCursor;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

@TargetApi(19)
public abstract class NativeDocumentsProvider extends android.provider.DocumentsProvider {

    /**
     * Default root projection: everything but Root.COLUMN_MIME_TYPES
     */
    private final static String[] DEFAULT_ROOT_PROJECTION = new String[]{
            BaseColumns._ID,
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    };

    protected abstract DocumentsProvider getDelegate();

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        Collection<? extends Root> roots = getDelegate().getRoots();
        MatrixCursor cursor = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION, roots.size());
        for (Root r : roots) {
            MatrixCursor.RowBuilder row = cursor.newRow();
            row.add(DocumentsContract.Root.COLUMN_ROOT_ID, r.getId());
            row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, r.getDocumentId());
            row.add(DocumentsContract.Root.COLUMN_TITLE, r.getTitle());
            row.add(DocumentsContract.Root.COLUMN_ICON, r.getIcon());
            row.add(DocumentsContract.Root.COLUMN_FLAGS, r.getFlags());
            row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, r.getAvailableBytes());
        }
        return cursor;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        try {
            return getDelegate().queryDocument(documentId, projection);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        try {
            return getDelegate().queryChildDocuments(parentDocumentId, projection, sortOrder);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        try {
            return getDelegate().openDocument(documentId, mode, signal);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        try {
            return getDelegate().openDocumentThumbnail(documentId, sizeHint, signal);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        try {
            return getDelegate().queryRecentDocuments(rootId, projection);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        try {
            return getDelegate().querySearchDocuments(rootId, query, projection);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        try {
            return getDelegate().createDocument(parentDocumentId, mimeType, displayName);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        try {
            return getDelegate().renameDocument(documentId, displayName);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        try {
            getDelegate().deleteDocument(documentId);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage()); // TODO
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }
}
