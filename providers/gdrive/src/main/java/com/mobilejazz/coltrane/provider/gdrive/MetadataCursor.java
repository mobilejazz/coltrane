package com.mobilejazz.coltrane.provider.gdrive;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Metadata;

import static com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MetadataCursor implements Cursor {

    private static final int INDEX_DOCUMENT_ID = 0;
    private static final int INDEX_MIME_TYPE = 1;
    private static final int INDEX_DISPLAY_NAME = 2;
    private static final int INDEX_SUMMARY = 3;
    private static final int INDEX_LAST_MODIFIED = 4;
    private static final int INDEX_ICON = 5;
    private static final int INDEX_FLAGS = 6;
    private static final int INDEX_SIZE = 7;

    private static final Map<String, Integer> sName2Index = new HashMap<String, Integer>() {{
        put(Document.COLUMN_DOCUMENT_ID, INDEX_DOCUMENT_ID);
        put(Document.COLUMN_MIME_TYPE, INDEX_MIME_TYPE);
        put(Document.COLUMN_DISPLAY_NAME, INDEX_DISPLAY_NAME);
        put(Document.COLUMN_SUMMARY, INDEX_SUMMARY);
        put(Document.COLUMN_LAST_MODIFIED, INDEX_LAST_MODIFIED);
        put(Document.COLUMN_ICON, INDEX_ICON);
        put(Document.COLUMN_FLAGS, INDEX_FLAGS);
        put(Document.COLUMN_SIZE, INDEX_SIZE);
    }};

    private static final String[] sIndex2Name = new String[] {
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_SUMMARY,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_ICON,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    private static final int[] sTypes = new int[] {
            FIELD_TYPE_STRING,
            FIELD_TYPE_STRING,
            FIELD_TYPE_STRING,
            FIELD_TYPE_STRING,
            FIELD_TYPE_INTEGER,
            FIELD_TYPE_NULL,
            FIELD_TYPE_INTEGER,
            FIELD_TYPE_INTEGER
    };

    private int mPosition;

    private GoogleApiClient mClient;

    private List<ContentObserver> mContentObservers;
    private List<DataSetObserver> mDataSetObservers;

    public MetadataCursor(GoogleApiClient client) {
        mClient = client;
        mPosition = -1;
        mContentObservers = new ArrayList<ContentObserver>();
        mDataSetObservers = new ArrayList<DataSetObserver>();
        requery();
    }

    protected abstract Metadata getData();

    protected GoogleApiClient getClient() {
        return mClient;
    }

    @Override
    public int getPosition() {
        return mPosition;
    }

    private boolean coercePosition() {
        if (mPosition < 0) {
            mPosition = -1;
            return false;
        } else if (mPosition >= getCount()) {
            mPosition = getCount();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean move(int offset) {
        mPosition += offset;
        return coercePosition();
    }

    @Override
    public boolean moveToPosition(int position) {
        mPosition = position;
        return coercePosition();
    }

    @Override
    public boolean moveToFirst() {
        mPosition = 0;
        return getCount() > 0;
    }

    @Override
    public boolean moveToLast() {
        mPosition = getCount() - 1;
        return getCount() > 0;
    }

    @Override
    public boolean moveToNext() {
        mPosition++;
        return coercePosition();
    }

    @Override
    public boolean moveToPrevious() {
        mPosition--;
        return coercePosition();
    }

    @Override
    public boolean isFirst() {
        return mPosition == 0;
    }

    @Override
    public boolean isLast() {
        return mPosition == getCount() - 1;
    }

    @Override
    public boolean isBeforeFirst() {
        return mPosition < 0;
    }

    @Override
    public boolean isAfterLast() {
        return mPosition >= getCount();
    }

    @Override
    public int getColumnIndex(String columnName) {
        Integer index = sName2Index.get(columnName);
        if (index != null) {
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        Integer index = sName2Index.get(columnName);
        if (index != null) {
            return index;
        } else {
            throw new IllegalArgumentException("Invalid column name: " + columnName);
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return sIndex2Name[columnIndex];
    }

    @Override
    public String[] getColumnNames() {
        return sIndex2Name;
    }

    @Override
    public int getColumnCount() {
        return sIndex2Name.length;
    }

    private Object get(int columnIndex) {
        switch (columnIndex) {
            case INDEX_DOCUMENT_ID:
                return getData().getDriveId().encodeToString();
            case INDEX_MIME_TYPE:
                return getData().getMimeType();
            case INDEX_DISPLAY_NAME:
                return getData().getTitle();
            case INDEX_SUMMARY:
                return getData().getDescription();
            case INDEX_LAST_MODIFIED:
                return getData().getModifiedDate();
            case INDEX_ICON:
                return 0;
            case INDEX_FLAGS:
                /**
                 * FLAG_SUPPORTS_WRITE
                 * @see #FLAG_SUPPORTS_DELETE
                 * @see #FLAG_SUPPORTS_THUMBNAIL
                 * @see #FLAG_DIR_PREFERS_GRID
                 * @see #FLAG_DIR_PREFERS_LAST_MODIFIED
                 */
                int flags = 0;
                if (getData().isEditable()) {
                    flags |= Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_DELETE;
                }
                return flags;
            case INDEX_SIZE:
                return getData().getFileSize();
            default:
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return (byte[])get(columnIndex);
    }

    @Override
    public String getString(int columnIndex) {
        return (String)get(columnIndex);
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        String value = (String)get(columnIndex);
        if (buffer.data == null || buffer.data.length < value.length()) {
            buffer.data = new char[value.length()];
        }
        value.getChars(0, value.length(), buffer.data, 0);
        buffer.sizeCopied = value.length();
    }

    @Override
    public short getShort(int columnIndex) {
        return (Short)get(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) {
        return (Integer)get(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        return (Long)get(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) {
        return (Float)get(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) {
        return (Double)get(columnIndex);
    }

    @Override
    public int getType(int columnIndex) {
        return sTypes[columnIndex];
    }

    @Override
    public boolean isNull(int columnIndex) {
        return get(columnIndex) == null;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        mContentObservers.add(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        mContentObservers.remove(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservers.remove(observer);
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        // TODO
    }

    @Override
    public Uri getNotificationUri() {
        // TODO
        return null;
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public Bundle getExtras() {
        // TODO
        return null;
    }

    @Override
    public Bundle respond(Bundle extras) {
        return null;
    }
}
