package com.mobilejazz.coltrane.provider.filesystem;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderAdapter;

public class FileSystemAdapter extends DocumentsProviderAdapter {

    public static final String AUTHORITY = "com.mobilejazz.coltrane.provider.filesystem";

    private FileSystemProvider delegate;

    @Override
    protected DocumentsProvider getDelegate() {
        if (delegate == null) {
            delegate = new FileSystemProvider(getContext());
        }
        return delegate;
    }
}
