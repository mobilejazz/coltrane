package com.mobilejazz.coltrane.library.utils;

import android.provider.BaseColumns;
import android.text.TextUtils;

import static com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Document;

import com.mobilejazz.coltrane.library.compatibility.MatrixCursor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ListCursor<T> extends MatrixCursor {

    private static final String[] sColumnNames = new String[]{
            BaseColumns._ID,
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_SUMMARY,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_ICON,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    private DocumentAccessor<T> mAccessor;
    private HashMap<String, ValueGetter<? extends Comparable, T>> mValueGetters;

    public ListCursor(List<T> items, DocumentAccessor<T> accessor, String[] projection, String sortOrder) {
        super(projection != null ? projection : sColumnNames, items.size());
        mAccessor = accessor;

        mValueGetters = new HashMap<String, ValueGetter<? extends Comparable, T>>();
        mValueGetters.put(Document.COLUMN_DOCUMENT_ID, new ValueGetter<String, T>() {
            @Override
            public String getValue(T item) {
                return mAccessor.getDocumentId(item);
            }
        });
        mValueGetters.put(Document.COLUMN_MIME_TYPE, new ValueGetter<String, T>() {
            @Override
            public String getValue(T item) {
                return mAccessor.getMimeType(item);
            }
        });
        mValueGetters.put(Document.COLUMN_DISPLAY_NAME, new ValueGetter<String, T>() {
            @Override
            public String getValue(T item) {
                return mAccessor.getDisplayName(item);
            }
        });
        mValueGetters.put(Document.COLUMN_LAST_MODIFIED, new ValueGetter<Long, T>() {
            @Override
            public Long getValue(T item) {
                return mAccessor.getLastModified(item);
            }
        });
        mValueGetters.put(Document.COLUMN_SIZE, new ValueGetter<Long, T>() {
            @Override
            public Long getValue(T item) {
                return mAccessor.getSize(item);
            }
        });

        int index = 0;
        if (!TextUtils.isEmpty(sortOrder)) {
            Collections.sort(items, new ItemComparator(sortOrder));
        }
        for (T i : items) {
            RowBuilder row = newRow();
            row.add(BaseColumns._ID, index++);
            row.add(Document.COLUMN_DOCUMENT_ID, accessor.getDocumentId(i));
            row.add(Document.COLUMN_MIME_TYPE, accessor.getMimeType(i));
            row.add(Document.COLUMN_DISPLAY_NAME, accessor.getDisplayName(i));
            row.add(Document.COLUMN_SUMMARY, accessor.getSummary(i));
            row.add(Document.COLUMN_LAST_MODIFIED, accessor.getLastModified(i));
            row.add(Document.COLUMN_ICON, accessor.getIcon(i));
            row.add(Document.COLUMN_FLAGS, accessor.getFlags(i));
            row.add(Document.COLUMN_SIZE, accessor.getSize(i));
        }
    }

    private class ItemComparator implements Comparator<T> {

        private ValueGetter<? extends Comparable, T> valueGetter;

        public ItemComparator(String columnName) {
            this.valueGetter = mValueGetters.get(columnName);
            if (valueGetter == null) {
                throw new IllegalArgumentException("Cannot sort by column " + columnName);
            }
        }

        @Override
        public int compare(T lhs, T rhs) {
            if (mAccessor.isFolder(lhs) && !mAccessor.isFolder(rhs)) {
                return -1;
            } else if (!mAccessor.isFolder(lhs) && mAccessor.isFolder(rhs)) {
                return 1;
            } else {
                return valueGetter.getValue(lhs).compareTo(valueGetter.getValue(rhs));
            }
        }

    }

    private static interface ValueGetter<V extends Comparable, T> {

        public V getValue(T item);

    }

}
