/*
 * Copyright (C) 2014 Mobilejazz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilejazz.coltrane.example;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobilejazz.coltrane.ui.DocumentBrowserActivity;

public class FileChooserExampleActivity extends Activity {

    private static final int REQUEST_CODE = 6384; // onActivityResult request code

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.example);
    }

    private void openCustomUI() {
        Intent selectFile = new Intent(this, DocumentBrowserActivity.class);
        startActivityForResult(selectFile, REQUEST_CODE);
    }

    public void openCustomUI(View view) {
        openCustomUI();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void openSafUI() {
        // supports SAF natively:
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent selectFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        selectFile.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        selectFile.setType("*/*");
        startActivityForResult(selectFile, REQUEST_CODE);
    }

    public void openSafWithFallback(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            openSafUI();
        } else {
            // Fallback to custom UI:
            openCustomUI();
        }
    }

    private void openWithSelector() {
        final Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        selectFile.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        selectFile.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(selectFile, REQUEST_CODE);
    }

    public void openNativeUI(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            openSafUI();
        } else {
            // Fallback to custom UI:
            openWithSelector();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        String documentId = data.getStringExtra(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                        try {
                            Toast.makeText(FileChooserExampleActivity.this,
                                    "File Selected: " + documentId, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
