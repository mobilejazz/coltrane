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
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;
import com.mobilejazz.coltrane.library.utils.RootCursor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Main Activity that handles the FileListFragments
 */
public class DocumentBrowserActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener, DocumentListFragment.Callbacks {

    public static final String PATH = "com.mobilejazz.coltrane.ui.browser.path";
    public static final String SELECTED_ITEM = "com.mobilejazz.coltrane.ui.browser.selected";

    public static final String RESULT_ID = DocumentsContract.Document.COLUMN_DOCUMENT_ID;

    private FragmentManager mFragmentManager;
    private BroadcastReceiver mStorageListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
            finishWithResult(null);
        }
    };

    private ActionBar.OnNavigationListener mNavigationListener = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            mFragmentManager.popBackStack((int)itemId, 0);
            return true;
        }
    };

    private Root mRoot;
    private String mCurrentDocumentId;
    private String mRootId;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ArrayAdapter<Root> mDrawerAdapter;
    private BackStackAdapter mNavigationAdapter;
    private Map<Root, Integer> mRootIndices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.picker);

        // Setting up the navigation:
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerAdapter = new RootAdapter(this, R.layout.root, DocumentsProviderRegistry.get().getAllRoots());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

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
        populateRootIndices();

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mNavigationAdapter = new BackStackAdapter(this, getFragmentManager(), R.layout.navigation_item, R.layout.navigation_item_dropdown);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mNavigationAdapter, mNavigationListener);

        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        if (savedInstanceState == null) {
            final Root root = DocumentsProviderRegistry.get().getDefault().getRoots().get(0);
            replaceFragment(root, null);
            mDrawerList.setItemChecked(mRootIndices.get(root), true);
        } else {
            int selected = savedInstanceState.getInt(SELECTED_ITEM);
            mCurrentDocumentId = savedInstanceState.getString(PATH);
            selectItem(selected);
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

        getActionBar().setSelectedNavigationItem(newDepth - 1);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //boolean hasBackStack = mFragmentManager.getBackStackEntryCount() > 0;

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(hasBackStack);
        //actionBar.setHomeButtonEnabled(hasBackStack);

        return true;
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

    private void populateRootIndices() {
        mRootIndices = new HashMap<Root, Integer>();
        for (int pos = 0; pos < mDrawerAdapter.getCount(); ++pos) {
            mRootIndices.put(mDrawerAdapter.getItem(pos), pos);
        }
    }

    private RootCursor getRoot(DocumentsProvider provider) {
        try {
            RootCursor c = new RootCursor(provider.queryRoots(null));
            c.moveToFirst();
            return c;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return null;
        }
    }

    private void selectItem(int position) {
        Root root = (Root)mDrawerList.getItemAtPosition(position);
        if (root != mRoot) {
            replaceFragment(root, null);
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void changeProvider(Root root, String documentId) {
        if (root != mRoot) {
            mRootId = root.getDocumentId();
            setTitle(root.getTitle());
            mRoot = root;
            mCurrentDocumentId = (documentId != null) ? documentId : mRootId;
        } else if (documentId != null) {
            mCurrentDocumentId = documentId;
        }
    }

    private void replaceFragment(Root root, String documentId) {
        try {
            changeProvider(root, documentId);
            DocumentListFragment fragment = DocumentListFragment.newInstance(root.getProvider().getId(), mCurrentDocumentId);

            DocumentCursor c = new DocumentCursor(mRoot.getProvider().queryDocument(mCurrentDocumentId, null));
            c.moveToFirst();

            FragmentTransaction t = mFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            if (mCurrentDocumentId == mRootId) {
                t.addToBackStack(mRoot.getTitle());
            } else {
                t.addToBackStack(c.getName());
            }
            t.commit();

            c.close();
        } catch (FileNotFoundException e) {
            Timber.e(e, e.getLocalizedMessage());
        }
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
                replaceFragment(mRoot, document.getId());
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}
