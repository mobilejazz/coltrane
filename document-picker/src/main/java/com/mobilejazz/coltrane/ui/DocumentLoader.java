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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.action.PendingAction;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;

import java.io.FileNotFoundException;

/**
 * Loader that returns a list of Files in a given file path.
 * 
 * @version 2013-12-11
 * @author paulburke (ipaulpro)
 */
public class DocumentLoader extends AsyncTaskLoader<Cursor> {

    private DocumentsProvider mProvider;
	private String mParentDocumentId;

    private PendingAction mPendingAction;

    private String mMimeTypeFilter;

	public DocumentLoader(Context context, DocumentsProvider provider, String parentDocumentId, String mimeTypeFilter) {
		super(context);
        this.mProvider = provider;
		this.mParentDocumentId = parentDocumentId;
        this.mMimeTypeFilter = mimeTypeFilter;

        // run only once
        onContentChanged();
	}

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        // That's how we start every AsyncTaskLoader...
        // -  code snippet from  android.content.CursorLoader  (method  onStartLoading)
        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
	public Cursor loadInBackground() {
        try {
            return mProvider.queryChildDocuments(mParentDocumentId, null, DocumentsContract.Document.COLUMN_DISPLAY_NAME, mMimeTypeFilter);
        } catch (FileNotFoundException e) {
            return null;
        } catch (UserRecoverableException e) {
            mPendingAction = e.getPendingAction();
            return null;
        }
    }

    public PendingAction getPendingAction() {
        return mPendingAction;
    }

}