package com.mobilejazz.coltrane.library;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;

@TargetApi(19)
public abstract class DocumentsProviderAdapter extends android.provider.DocumentsProvider {

    protected abstract DocumentsProvider getDelegate();

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return getDelegate().queryRoots(projection);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return getDelegate().queryDocument(documentId, projection);
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        return getDelegate().queryChildDocuments(parentDocumentId, projection, sortOrder);
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return getDelegate().openDocument(documentId, mode, signal);
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        return getDelegate().openDocumentThumbnail(documentId, sizeHint, signal);
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        return getDelegate().queryRecentDocuments(rootId, projection);
    }

    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        return getDelegate().querySearchDocuments(rootId, query, projection);
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        return getDelegate().createDocument(parentDocumentId, mimeType, displayName);
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        return getDelegate().renameDocument(documentId, displayName);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        getDelegate().deleteDocument(documentId);
    }

    @Override
    public boolean onCreate() {
        return true;
    }
}
