package com.mobilejazz.coltrane.library.utils;

import android.content.AsyncTaskLoader;
import android.content.Context;

public abstract class AsyncLoader<D> extends AsyncTaskLoader<D> {

    public AsyncLoader(Context context) {
        super(context);
        onContentChanged();
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged())
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

}
