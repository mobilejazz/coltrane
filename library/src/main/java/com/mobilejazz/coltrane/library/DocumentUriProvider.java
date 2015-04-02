package com.mobilejazz.coltrane.library;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.mobilejazz.coltrane.library.utils.DocumentCursor;

import java.io.FileNotFoundException;
import java.util.List;

public class DocumentUriProvider extends ContentProvider {

    public static final String AUTHORITY = "com.mobilejazz.coltrane.library.provider";

    private static class DocumentUri {

        private Uri uri;

        private DocumentsProvider provider;
        private String documentId;

        public DocumentUri(Uri uri) {
            this.uri = uri;
            List<String> path = uri.getPathSegments();
            provider = DocumentsProviderRegistry.get().getProvider(path.get(0));
            documentId = path.get(1);
        }

        public String getType() {
            DocumentCursor c = null;
            try {
                c = new DocumentCursor(provider.queryDocument(documentId, null));
                c.moveToFirst();
                return c.getMimeType();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("Invalid file uri: " + uri);
            } catch (UserRecoverableException e) {
                throw new IllegalStateException("Invalid file uri: " + uri);
            } finally {
                c.close();
            }
        }

    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        DocumentUri documentUri = new DocumentUri(uri);
        try {
            return documentUri.provider.queryDocument(documentUri.documentId, projection);
        } catch (FileNotFoundException e) {
            return null;
        } catch (UserRecoverableException e) {
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return new DocumentUri(uri).getType();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external deletes");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return openFile(uri, mode, null);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        DocumentUri docUri = new DocumentUri(uri);
        try {
            return docUri.provider.openDocument(docUri.documentId, mode, signal);
        } catch (UserRecoverableException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    public static Uri getUri(Context c, String providerId, String documentId) {
        return new Uri.Builder().scheme("content").authority(getAuthority(c)).appendPath(providerId).appendPath(documentId).build();
    }

    protected static String getAuthority(Context c) {
        return c.getString(R.string.document_uri_authority);
    }

}
