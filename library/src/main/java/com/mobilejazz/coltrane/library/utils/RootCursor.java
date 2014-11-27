package com.mobilejazz.coltrane.library.utils;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Root;

public class RootCursor extends CursorWrapper {

    private int mId;
    private int mDocumentId;
    private int mTitle;
    private int mIcon;

    public RootCursor(Cursor c) {
        super(c);
        mId = c.getColumnIndex(Root.COLUMN_ROOT_ID);
        mDocumentId = c.getColumnIndex(Root.COLUMN_DOCUMENT_ID);
        mTitle = c.getColumnIndex(Root.COLUMN_TITLE);
        mIcon = c.getColumnIndex(Root.COLUMN_ICON);
    }

    public String getId() {
        return getWrappedCursor().getString(mId);
    }

    public String getDocumentId() {
        return getWrappedCursor().getString(mDocumentId);
    }

    public String getTitle() {
        return getWrappedCursor().getString(mTitle);
    }

    public int getIcon() {
        return getWrappedCursor().getInt(mIcon);
    }

    // TODO: complete

}
