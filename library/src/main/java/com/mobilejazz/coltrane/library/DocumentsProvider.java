package com.mobilejazz.coltrane.library;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.util.List;

public interface DocumentsProvider {

    public Cursor queryRoots(String[] projection) throws FileNotFoundException;

    public List<Root> getRoots();

    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException;

    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException;

    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException;

    public AssetFileDescriptor openDocumentThumbnail(final String documentId, final Point sizeHint, final CancellationSignal signal) throws FileNotFoundException;

    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException;

    public String renameDocument(String documentId, String displayName) throws FileNotFoundException;

    public void deleteDocument(String documentId) throws FileNotFoundException;

    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException;

    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException;

    public String getId();

}
