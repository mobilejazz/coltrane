package com.mobilejazz.coltrane.example;

import android.app.Application;

import com.mobilejazz.coltrane.provider.filesystem.FileSystemProvider;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileSystemProvider.register(getApplicationContext());
    }
}
