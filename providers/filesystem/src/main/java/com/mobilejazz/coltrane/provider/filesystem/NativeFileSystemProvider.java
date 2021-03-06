package com.mobilejazz.coltrane.provider.filesystem;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.NativeDocumentsProvider;

public class NativeFileSystemProvider extends NativeDocumentsProvider {

    public static final String AUTHORITY = "com.mobilejazz.coltrane.provider.filesystem.documents";

    private FileSystemProvider delegate;

    @Override
    protected DocumentsProvider getDelegate() {
        if (delegate == null) {
            delegate = new FileSystemProvider(getContext());
        }
        return delegate;
    }
}
