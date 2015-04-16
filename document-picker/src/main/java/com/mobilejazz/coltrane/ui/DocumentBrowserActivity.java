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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobilejazz.coltrane.library.DocumentUriProvider;
import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.AsyncLoader;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main Activity that handles the FileListFragments
 */
public class DocumentBrowserActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener, DocumentListFragment.Callbacks, LoaderManager.LoaderCallbacks<List<Root>>, ProviderChooser.OnProviderSelectedListener {

    private static final int LOADER_ROOTS = 1;
    private static final int REQUEST_NEW_ACCOUNT = 35093;

    public static final String PATH = "com.mobilejazz.coltrane.ui.browser.path";
    public static final String FILTER = "com.mobilejazz.coltrane.ui.browser.filter";
    public static final String SELECTED_ITEM = "com.mobilejazz.coltrane.ui.browser.selected";

    public static final String EXTRA_PROVIDER = "com.mobilejazz.coltrane.ui.browser.result.provider";
    public static final String EXTRA_DOCUMENT_ID = "com.mobilejazz.coltrane.ui.browser.result.document.id";
    public static final String EXTRA_DOCUMENT_NAME = "com.mobilejazz.coltrane.ui.browser.result.document.name";

    public static final String DOCUMENT_URI_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".documentUriProvider";

    public static final String RESULT_ID = DocumentsContract.Document.COLUMN_DOCUMENT_ID;

    private FragmentManager mFragmentManager;
    private BroadcastReceiver mStorageListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
            finishWithResult(null);
        }
    };

    private AdapterView.OnItemSelectedListener mNavigationListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (id >= 0) {
                mFragmentManager.popBackStack((int) id, 0);
            } else {
                // pop all:
                mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private Root mRoot;
    private String mCurrentDocumentId;
    private String mRootId;

    private Toolbar mToolbar;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Spinner mNavigationDropDown;

    private ArrayAdapter<Root> mDrawerAdapter;
    private BackStackAdapter mNavigationAdapter;
    private Map<Root, Integer> mRootIndices;
    private ArrayList<String> mLinkableProviderIds;

    private Bundle mSavedInstanceState;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.picker);

        mLinkableProviderIds = new ArrayList<String>();
        for (DocumentsProvider p : DocumentsProviderRegistry.get().getAll()) {
            if (p.linkAccount() != null) {
                mLinkableProviderIds.add(p.getId());
            }
        }
        if (mLinkableProviderIds.isEmpty()) {
            findViewById(R.id.link_account).setVisibility(View.GONE);
        }

        mHandler = new Handler();

        // Setting up the navigation:
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerAdapter = new RootAdapter(this, R.layout.root);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        getActionBar().hide();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setAdapter(mDrawerAdapter);


        mNavigationAdapter = new BackStackAdapter(this, getFragmentManager(), R.layout.navigation_item, R.layout.navigation_item_dropdown);
        mNavigationDropDown = (Spinner) findViewById(R.id.navigation_dropdown);
        mNavigationDropDown.setOnItemSelectedListener(mNavigationListener);
        mNavigationDropDown.setAdapter(mNavigationAdapter);

        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mSavedInstanceState = savedInstanceState;

        getLoaderManager().initLoader(LOADER_ROOTS, null, this);
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
        outState.putInt(SELECTED_ITEM, mDrawerList.getSelectedItemPosition());
    }

    @Override
    public void onBackStackChanged() {
        int newDepth = mFragmentManager.getBackStackEntryCount();
        if (newDepth > 0) {
            FragmentManager.BackStackEntry fragment = mFragmentManager.getBackStackEntryAt(newDepth - 1);
            mCurrentDocumentId = fragment.getName();
        } else {
            mCurrentDocumentId = mRootId;
        }
        mNavigationDropDown.setSelection(newDepth);

        invalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mFragmentManager.popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.account_linked, Toast.LENGTH_LONG).show();
                getLoaderManager().restartLoader(LOADER_ROOTS, null, this);
            }
        }
    }

    private void populateRootIndices() {
        mRootIndices = new HashMap<Root, Integer>();
        for (int pos = 0; pos < mDrawerAdapter.getCount(); ++pos) {
            mRootIndices.put(mDrawerAdapter.getItem(pos), pos);
        }
    }

    /**
     * This method is called whenever an item in the left navigation drawer is selected. This can
     * be the case when a user clicks on it, or when it is programmatically selected.
     *
     * @param position The position of the selected item in the list.
     */
    private void selectItem(final int position) {
        final Root root = (Root) mDrawerList.getItemAtPosition(position);

        if (root != mRoot) {
            replaceFragment(root, null, root.getTitle());
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawers();
    }

    private void changeProvider(Root root, String documentId) {
        if (root != mRoot) {
            // pop all:
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mRootId = root.getDocumentId();
            mNavigationAdapter.setHeader(root.getTitle());
            mRoot = root;
            mCurrentDocumentId = (documentId != null) ? documentId : mRootId;
        } else if (documentId != null) {
            mCurrentDocumentId = documentId;
        }
    }

    private void replaceFragment(final Root root, String documentId, String documentName) {
        changeProvider(root, documentId);
        DocumentListFragment fragment = DocumentListFragment.newInstance(root.getProvider().getId(), mCurrentDocumentId);

        FragmentTransaction t = mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (!mCurrentDocumentId.equals(mRootId)) {
            t.addToBackStack(documentName);
        }
        t.commit();
    }


    /**
     * Finish this Activity with a result code and URI of the selected file.
     *
     * @param document The selected document.
     */
    private void finishWithResult(DocumentCursor document) {
        Intent i = new Intent();

        String providerId = mRoot.getProvider().getId();
        String documentId = document.getId();

        i.setDataAndType(DocumentUriProvider.getUri(DOCUMENT_URI_PROVIDER_AUTHORITY, providerId, documentId), document.getMimeType());
        i.putExtra(EXTRA_PROVIDER, providerId);
        i.putExtra(EXTRA_DOCUMENT_ID, documentId);
        i.putExtra(EXTRA_DOCUMENT_NAME, document.getName());
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
                replaceFragment(mRoot, document.getId(), document.getName());
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

    public void linkAccount(View v) {
        if (mLinkableProviderIds.size() > 1) {
            new ProviderChooser().show(getFragmentManager(), "ProviderChooser");
        } else {
            DocumentsProviderRegistry.get().getProvider(mLinkableProviderIds.get(0)).linkAccount().performWith(this, REQUEST_NEW_ACCOUNT);
        }
    }

    @Override
    public Loader<List<Root>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<Root>>(this) {
            @Override
            public List<Root> loadInBackground() {
                return DocumentsProviderRegistry.get().getAllRoots();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Root>> loader, final List<Root> data) {
        mDrawerAdapter.clear();
        mDrawerAdapter.addAll(data);
        populateRootIndices();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (data.size() > 0) {
                    if (mSavedInstanceState == null) {
                        selectItem(0);
                    } else {
                        int selected = mSavedInstanceState.getInt(SELECTED_ITEM);
                        mCurrentDocumentId = mSavedInstanceState.getString(PATH);
                        selectItem(selected);
                    }
                } else {
                    mNavigationAdapter.setHeader(getString(R.string.no_provider));
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<List<Root>> loader) {
        mDrawerAdapter.clear();
    }

    @Override
    public void onProviderSelected(DocumentsProvider provider) {
        // link new account:
        provider.linkAccount().performWith(this, REQUEST_NEW_ACCOUNT);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}
