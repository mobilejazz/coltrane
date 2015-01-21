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
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;

/**
 * Fragment that displays a list of Files in a given path.
 */
public class DocumentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String PROVIDER = "com.mobilejazz.coltrane.ui.browser.fragment.provider";

    public static final int REQUEST_RESOLVE_PROVIDER_ISSUE = 35092;

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

    private View mPendingActionView;
    private Button mExecutePendingAction;
    private TextView mEmptyTextView;
    private View mProgressContainer;
    private View mListContainer;
    private SwipeRefreshLayout mSwipeLayout;

    private Intent mPendingAction;

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
        args.putString(PROVIDER, providerId);
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
        String providerId = getArguments().getString(PROVIDER);
        mProvider = DocumentsProviderRegistry.get().getProvider(providerId);
        mCurrentDocumentId = getArguments().getString(DocumentBrowserActivity.PATH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_list, container, false);

        mPendingActionView = view.findViewById(R.id.pending_action);
        mExecutePendingAction = (Button)mPendingActionView.findViewById(R.id.execute_pending_action);
        mExecutePendingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmptyView(mEmptyTextView);
                setListShown(false);
                startActivityForResult(mPendingAction, REQUEST_RESOLVE_PROVIDER_ISSUE);
            }
        });
        mEmptyTextView = (TextView)view.findViewById(R.id.empty);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        mListContainer = view.findViewById(R.id.listContainer);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
    }

    protected void changeEmptyView(View v) {
        View old = getListView().getEmptyView();
        if (old != null) {
            old.setVisibility(View.GONE);
        }
        getListView().setEmptyView(v);
    }

    @Override
    public void onStart() {
        super.onStart();

        changeEmptyView(mEmptyTextView);
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
        mSwipeLayout.setRefreshing(false);

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);


        Intent pendingAction = ((DocumentLoader)loader).getPendingAction();
        if (pendingAction != null) {
            if (mPendingAction == null) {
                mPendingAction = pendingAction;
                changeEmptyView(mPendingActionView);
                startActivityForResult(mPendingAction, REQUEST_RESOLVE_PROVIDER_ISSUE);
            }
        } else if (data == null) {
            // network error:
            mEmptyTextView.setText(R.string.network_error);
        } else {
            mEmptyTextView.setText(R.string.empty_directory);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_RESOLVE_PROVIDER_ISSUE:
                if (resultCode == Activity.RESULT_OK) {
                    // a provider request has been resolved
                    mPendingAction = null;
                    getLoaderManager().restartLoader(LOADER_ID, null, this);
                } else {
                    // TODO
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    // ------------ THIS IS COPIED FROM THE SUPER CLASS BECAUSE OF ACCESS LIMITATIONS ---------- //

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(android.widget.ListAdapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     */
    @Override
    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    @Override
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }




}
