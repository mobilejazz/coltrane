package com.mobilejazz.coltrane.provider.gdrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

public class ResourceCursor extends MetadataCursor {

    private DriveResource mResource;

    private Metadata mData;

    public ResourceCursor(GoogleApiClient client, DriveResource resource) {
        super(client);
        this.mResource = resource;
    }

    @Override
    protected Metadata getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public void deactivate() {
        // do nothing;
    }

    @Override
    public boolean requery() {
        DriveResource.MetadataResult result = mResource.getMetadata(getClient()).await();
        if (result.getStatus().isSuccess()) {
            mData = result.getMetadata();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        // do nothing;
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
