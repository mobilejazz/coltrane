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
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;

/**
 * Fragment that displays a list of Files in a given path.
 */
public class DocumentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Interface to listen for events.
     */
    public interface Callbacks {
        /**
         * Called when a file is selected from the list.
         *
         * @param document A cursor to the selected document
         */
        public void onDocumentSelected(DocumentCursor document);
    }

    private static final int LOADER_ID = 0;

    private DocumentListAdapter mAdapter;
    private DocumentsProvider mProvider;
    private String mCurrentDocumentId;

    private Callbacks mListener;

    /**
     * Create a new instance with the given file path.
     *
     * @param path The absolute path of the file (directory) to display.
     * @return A new Fragment with the given file path.
     */
    public static DocumentListFragment newInstance(String providerId, String path) {
        DocumentListFragment fragment = new DocumentListFragment();
        Bundle args = new Bundle();
        args.putString(DocumentBrowserActivity.PATH, path);
        args.putString(DocumentBrowserActivity.PROVIDER, providerId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FileListFragment.Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new DocumentListAdapter(getActivity(), null);
        String providerId = getArguments().getString(DocumentBrowserActivity.PROVIDER);
        mProvider = DocumentsProviderRegistry.get().getProvider(providerId);
        mCurrentDocumentId = getArguments().getString(DocumentBrowserActivity.PATH);
    }

    @Override
    public void onStart() {
        super.onStart();

        setEmptyText(getString(R.string.empty_directory));
        setListAdapter(mAdapter);
        setListShown(false);

        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        DocumentListAdapter adapter = (DocumentListAdapter) l.getAdapter();
        if (adapter != null) {
            DocumentCursor c = (DocumentCursor)adapter.getCursor();
            c.moveToPosition(position);
            mListener.onDocumentSelected(c);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new DocumentLoader(getActivity(), mProvider, mCurrentDocumentId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }
}
