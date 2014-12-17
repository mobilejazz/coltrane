package com.mobilejazz.coltrane.provider.gdrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Query;

public class FolderCursor extends MetadataCursor {

    private DriveFolder mFolder;

    private MetadataBuffer mBuffer;

    public FolderCursor(GoogleApiClient client, DriveFolder folder) {
        super(client);
        this.mFolder = folder;
    }

    @Override
    protected Metadata getData() {
        return mBuffer.get(getPosition());
    }

    @Override
    public int getCount() {
        return mBuffer.getCount();
    }

    @Override
    public void deactivate() {
        mBuffer.release();
    }

    @Override
    public boolean requery() {
        DriveApi.MetadataBufferResult result = mFolder.queryChildren(getClient(), new Query.Builder().build()).await();
        if (result.getStatus().isSuccess()) {
            mBuffer = result.getMetadataBuffer();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        mBuffer.release();
    }

    @Override
    public boolean isClosed() {
        return mBuffer.isClosed();
    }
}