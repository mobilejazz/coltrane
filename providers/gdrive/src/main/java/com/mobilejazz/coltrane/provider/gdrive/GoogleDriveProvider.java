package com.mobilejazz.coltrane.provider.gdrive;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;

import java.io.FileNotFoundException;

public class GoogleDriveProvider extends DocumentsProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ID = "com.mobilejazz.coltrane.provider.gdrive";

    private GoogleApiClient mGoogleApiClient;

    public GoogleDriveProvider(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        return true;
    }

    protected GoogleApiClient getClient() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.blockingConnect();
        }
        return mGoogleApiClient;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return new ResourceCursor(getClient(), Drive.DriveApi.getRootFolder(getClient()));
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        DriveFolder folder = Drive.DriveApi.getFolder(getClient(), DriveId.decodeFromString(parentDocumentId));
        return new FolderCursor(getClient(), folder);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, DriveId.decodeFromString(documentId));
        return new ResourceCursor(getClient(), file);
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Uri getContentUri(String documentId) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static void register(Context context) {
        DocumentsProviderRegistry.get().register(ID, new GoogleDriveProvider(context));
    }

}
