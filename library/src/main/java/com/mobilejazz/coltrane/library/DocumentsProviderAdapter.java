package com.mobilejazz.coltrane.library;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;

import java.io.FileNotFoundException;

@TargetApi(19)
public class DocumentsProviderAdapter extends DocumentsProvider {

    private DocumentBrowser mDelegate;

    public DocumentsProviderAdapter(DocumentBrowser delegate) {
        this.mDelegate = delegate;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return mDelegate.queryRoots(projection);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return mDelegate.queryDocument(documentId, projection);
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        return mDelegate.queryChildDocuments(parentDocumentId, projection, sortOrder);
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return mDelegate.openDocument(documentId, mode, signal);
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        return mDelegate.openDocumentThumbnail(documentId, sizeHint, signal);
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        return mDelegate.queryRecentDocuments(rootId, projection);
    }

    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        return mDelegate.querySearchDocuments(rootId, query, projection);
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        return mDelegate.createDocument(parentDocumentId, mimeType, displayName);
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        return mDelegate.renameDocument(documentId, displayName);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        mDelegate.deleteDocument(documentId);
    }

    @Override
    public boolean onCreate() {
        return true;
    }
}
