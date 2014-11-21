package com.mobilejazz.coltrane.library;

import android.database.Cursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;

public interface DocumentBrowser {

    public Cursor queryRoots(String[] projection) throws FileNotFoundException;

    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException;

    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException;

    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException;

}
