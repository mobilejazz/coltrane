package com.mobilejazz.coltrane.example;

import android.app.Application;

import com.mobilejazz.coltrane.provider.filesystem.FileSystemProvider;
import com.mobilejazz.coltrane.provider.gdrive.GoogleDriveProvider;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileSystemProvider.register(getApplicationContext());
        GoogleDriveProvider.register(getApplicationContext());
    }
}
