package com.mobilejazz.coltrane.ui;

import android.content.Intent;
import android.net.Uri;

import com.mobilejazz.coltrane.library.DocumentUriProvider;

public class DocumentUriHelper {

    public static Uri getUriFromIntent(String authority, Intent intent) {
        String providerId = intent.getStringExtra(DocumentBrowserActivity.EXTRA_PROVIDER);
        String documentId = intent.getStringExtra(DocumentBrowserActivity.EXTRA_DOCUMENT_ID);
        return DocumentUriProvider.getUri(authority, providerId, documentId);
    }

}
