package com.mobilejazz.coltrane.example;

import android.app.Application;

import com.mobilejazz.coltrane.provider.dropbox.DropboxProvider;
import com.mobilejazz.coltrane.provider.filesystem.FileSystemProvider;
import com.mobilejazz.coltrane.provider.gdrive.GoogleDriveProvider;

import timber.log.Timber;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        FileSystemProvider.register(getApplicationContext());
        GoogleDriveProvider.register(getApplicationContext());
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void i(String message, Object... args) {
            // TODO e.g., Crashlytics.log(String.format(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            i(message, args); // Just add to the log.
        }

        @Override
        public void e(String message, Object... args) {
            i("ERROR: " + message, args); // Just add to the log.
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            e(message, args);

            // TODO e.g., Crashlytics.logException(t);
        }
    }


}
