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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mobilejazz.coltrane.library.utils.DocumentCursor;

/**
 * List adapter for Files.
 * 
 * @version 2013-12-11
 * @author paulburke (ipaulpro)
 */
public class DocumentListAdapter extends CursorAdapter {

    private final static int ICON_FOLDER = R.drawable.ic_folder;
    private final static int ICON_FILE = R.drawable.ic_file;

    private final LayoutInflater mInflater;

    public DocumentListAdapter(Context context, Cursor cursor) {
        super(context, null, false);
        if (cursor != null) {
            swapCursor(wrap(cursor));
        }
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.file, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        DocumentCursor c = (DocumentCursor)cursor;

        TextView textView = (TextView) view;

        // Set the TextView as the file name
        textView.setText(c.getName());

        // If the item is not a directory, use the file icon
        int icon = c.isDirectory() ? ICON_FOLDER : ICON_FILE;
        textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(wrap(newCursor));
    }

    private DocumentCursor wrap(Cursor c) {
        if (c == null) {
            return null;
        } else if (c instanceof DocumentCursor) {
            return (DocumentCursor)c;
        } else {
            return new DocumentCursor(c);
        }
    }
}