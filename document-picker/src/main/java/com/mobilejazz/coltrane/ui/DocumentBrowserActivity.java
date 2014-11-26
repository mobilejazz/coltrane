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

package com.mobilejazz.coltrane.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;
import com.mobilejazz.coltrane.library.utils.RootCursor;

import java.io.FileNotFoundException;

/**
 * Main Activity that handles the FileListFragments
 */
public class DocumentBrowserActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener, DocumentListFragment.Callbacks {

    public static final String PROVIDER = "provider";
    public static final String PATH = "path";

    public static final String RESULT_ID = DocumentsContract.Document.COLUMN_DOCUMENT_ID;

    private FragmentManager mFragmentManager;
    private BroadcastReceiver mStorageListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
            finishWithResult(null);
        }
    };

    private DocumentsProvider mProvider;
    private String mCurrentDocumentId;
    private String mRootId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mProvider = DocumentsProviderRegistry.get().getAll().iterator().next(); // TODO: make this more general
        try {
            RootCursor c = new RootCursor(mProvider.queryRoots(null));
            c.moveToFirst();
            mRootId = c.getDocumentId();
            if (savedInstanceState == null) {
                mCurrentDocumentId = mRootId;
                addFragment();
            } else {
                mCurrentDocumentId = savedInstanceState.getString(PATH);
            }
            setTitle(c.getTitle());
            c.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterStorageListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerStorageListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PATH, mCurrentDocumentId);
    }

    @Override
    public void onBackStackChanged() {
        int count = mFragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry fragment = mFragmentManager.getBackStackEntryAt(count - 1);
            mCurrentDocumentId = fragment.getName();
        } else {
            mCurrentDocumentId = mRootId;
        }

        setTitle(mCurrentDocumentId);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean hasBackStack = mFragmentManager.getBackStackEntryCount() > 0;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(hasBackStack);
        actionBar.setHomeButtonEnabled(hasBackStack);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mFragmentManager.popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Add the initial Fragment with given path.
     */
    private void addFragment() {
        DocumentListFragment fragment = DocumentListFragment.newInstance(mProvider.getId(), mCurrentDocumentId);
        mFragmentManager.beginTransaction()
                .add(android.R.id.content, fragment).commit();
    }

    private void replaceFragment(String providerId, String documentId) {
        mCurrentDocumentId = documentId;
        DocumentListFragment fragment = DocumentListFragment.newInstance(providerId, documentId);
        mFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(mCurrentDocumentId).commit();
    }


    /**
     * Finish this Activity with a result code and URI of the selected file.
     *
     * @param document The selected document.
     */
    private void finishWithResult(DocumentCursor document) {
        Intent i = new Intent();
        i.putExtra(RESULT_ID, document.getId());
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onDocumentSelected(DocumentCursor document) {
        if (document.isBeforeFirst() || document.isAfterLast()) {
            Toast.makeText(DocumentBrowserActivity.this, R.string.error_selecting_file,
                    Toast.LENGTH_SHORT).show();
        } else {
            if (document.isDirectory()) {

                replaceFragment(mProvider.getId(), document.getId());
            } else {
                finishWithResult(document);
            }
        }
    }

    /**
     * Register the external storage BroadcastReceiver.
     */
    private void registerStorageListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mStorageListener, filter);
    }

    /**
     * Unregister the external storage BroadcastReceiver.
     */
    private void unregisterStorageListener() {
        unregisterReceiver(mStorageListener);
    }
}
