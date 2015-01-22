package com.mobilejazz.coltrane.provider.dropbox;

import android.os.ParcelFileDescriptor;

import com.dropbox.sync.android.DbxFile;

public class DbxParcelFileDescriptor extends ParcelFileDescriptor {

    private DbxFile mFile;

    public DbxParcelFileDescriptor(ParcelFileDescriptor wrapped, DbxFile file) {
        super(wrapped);
        mFile = file;
    }

    public void releaseResources() {
        mFile.close();
    }

}
