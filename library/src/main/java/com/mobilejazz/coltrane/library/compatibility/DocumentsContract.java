/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.mobilejazz.coltrane.library.compatibility;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.OnCloseListener;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.system.OsConstants.SEEK_SET;

public final class DocumentsContract {
    private static final String TAG = "Documents";

    private DocumentsContract() {
    }

    public final static class Document {
        private Document() {
        }

        /**
         * Unique ID of a document. This ID is both provided by and interpreted
         * by a {@link DocumentsProvider}, and should be treated as an opaque
         * value by client applications. This column is required.
         * <p>
         * Each document must have a unique ID within a provider, but that
         * single document may be included as a child of multiple directories.
         * <p>
         * A provider must always return durable IDs, since they will be used to
         * issue long-term URI permission grants when an application interacts
         * with {@link Intent#ACTION_OPEN_DOCUMENT} and
         * {@link Intent#ACTION_CREATE_DOCUMENT}.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_DOCUMENT_ID = "document_id";

        /**
         * Concrete MIME type of a document. For example, "image/png" or
         * "application/pdf" for openable files. A document can also be a
         * directory containing additional documents, which is represented with
         * the {@link #MIME_TYPE_DIR} MIME type. This column is required.
         * <p>
         * Type: STRING
         *
         * @see #MIME_TYPE_DIR
         */
        public static final String COLUMN_MIME_TYPE = "mime_type";

        /**
         * Display name of a document, used as the primary title displayed to a
         * user. This column is required.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_DISPLAY_NAME = OpenableColumns.DISPLAY_NAME;

        /**
         * Summary of a document, which may be shown to a user. This column is
         * optional, and may be {@code null}.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_SUMMARY = "summary";

        /**
         * Timestamp when a document was last modified, in milliseconds since
         * January 1, 1970 00:00:00.0 UTC. This column is required, and may be
         * {@code null} if unknown. A {@link DocumentsProvider} can update this
         * field using events from {@link OnCloseListener} or other reliable
         * {@link ParcelFileDescriptor} transports.
         * <p>
         * Type: INTEGER (long)
         *
         * @see System#currentTimeMillis()
         */
        public static final String COLUMN_LAST_MODIFIED = "last_modified";

        /**
         * Specific icon resource ID for a document. This column is optional,
         * and may be {@code null} to use a platform-provided default icon based
         * on {@link #COLUMN_MIME_TYPE}.
         * <p>
         * Type: INTEGER (int)
         */
        public static final String COLUMN_ICON = "icon";

        /**
         * Flags that apply to a document. This column is required.
         * <p>
         * Type: INTEGER (int)
         *
         * @see #FLAG_SUPPORTS_WRITE
         * @see #FLAG_SUPPORTS_DELETE
         * @see #FLAG_SUPPORTS_THUMBNAIL
         * @see #FLAG_DIR_PREFERS_GRID
         * @see #FLAG_DIR_PREFERS_LAST_MODIFIED
         */
        public static final String COLUMN_FLAGS = "flags";

        /**
         * Size of a document, in bytes, or {@code null} if unknown. This column
         * is required.
         * <p>
         * Type: INTEGER (long)
         */
        public static final String COLUMN_SIZE = OpenableColumns.SIZE;

        /**
         * MIME type of a document which is a directory that may contain
         * additional documents.
         *
         * @see #COLUMN_MIME_TYPE
         */
        public static final String MIME_TYPE_DIR = "vnd.android.document/directory";

        /**
         * Flag indicating that a document can be represented as a thumbnail.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsContract#getDocumentThumbnail(ContentResolver, Uri,
         *      Point, CancellationSignal)
         * @see DocumentsProvider#openDocumentThumbnail(String, Point,
         *      android.os.CancellationSignal)
         */
        public static final int FLAG_SUPPORTS_THUMBNAIL = 1;

        /**
         * Flag indicating that a document supports writing.
         * <p>
         * When a document is opened with {@link Intent#ACTION_OPEN_DOCUMENT},
         * the calling application is granted both
         * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION} and
         * {@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION}. However, the actual
         * writability of a document may change over time, for example due to
         * remote access changes. This flag indicates that a document client can
         * expect {@link ContentResolver#openOutputStream(Uri)} to succeed.
         * 
         * @see #COLUMN_FLAGS
         */
        public static final int FLAG_SUPPORTS_WRITE = 1 << 1;

        /**
         * Flag indicating that a document is deletable.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsContract#deleteDocument(ContentResolver, Uri)
         * @see DocumentsProvider#deleteDocument(String)
         */
        public static final int FLAG_SUPPORTS_DELETE = 1 << 2;

        /**
         * Flag indicating that a document is a directory that supports creation
         * of new files within it. Only valid when {@link #COLUMN_MIME_TYPE} is
         * {@link #MIME_TYPE_DIR}.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsProvider#createDocument(String, String, String)
         */
        public static final int FLAG_DIR_SUPPORTS_CREATE = 1 << 3;

        /**
         * Flag indicating that a directory prefers its contents be shown in a
         * larger format grid. Usually suitable when a directory contains mostly
         * pictures. Only valid when {@link #COLUMN_MIME_TYPE} is
         * {@link #MIME_TYPE_DIR}.
         *
         * @see #COLUMN_FLAGS
         */
        public static final int FLAG_DIR_PREFERS_GRID = 1 << 4;

        /**
         * Flag indicating that a directory prefers its contents be sorted by
         * {@link #COLUMN_LAST_MODIFIED}. Only valid when
         * {@link #COLUMN_MIME_TYPE} is {@link #MIME_TYPE_DIR}.
         *
         * @see #COLUMN_FLAGS
         */
        public static final int FLAG_DIR_PREFERS_LAST_MODIFIED = 1 << 5;

        /**
         * Flag indicating that a document can be renamed.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsContract#renameDocument(ContentProviderClient, Uri,
         *      String)
         * @see DocumentsProvider#renameDocument(String, String)
         */
        public static final int FLAG_SUPPORTS_RENAME = 1 << 6;

        /**
         * Flag indicating that document titles should be hidden when viewing
         * this directory in a larger format grid. For example, a directory
         * containing only images may want the image thumbnails to speak for
         * themselves. Only valid when {@link #COLUMN_MIME_TYPE} is
         * {@link #MIME_TYPE_DIR}.
         *
         * @see #COLUMN_FLAGS
         * @see #FLAG_DIR_PREFERS_GRID
         * @hide
         */
        public static final int FLAG_DIR_HIDE_GRID_TITLES = 1 << 16;
    }

    /**
     * Constants related to a root of documents, including {@link Cursor} column
     * names and flags. A root is the start of a tree of documents, such as a
     * physical storage device, or an account. Each root starts at the directory
     * referenced by {@link Root#COLUMN_DOCUMENT_ID}, which can recursively
     * contain both documents and directories.
     * <p>
     * All columns are <em>read-only</em> to client applications.
     */
    public final static class Root {
        private Root() {
        }

        /**
         * Unique ID of a root. This ID is both provided by and interpreted by a
         * {@link DocumentsProvider}, and should be treated as an opaque value
         * by client applications. This column is required.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_ROOT_ID = "root_id";

        /**
         * Flags that apply to a root. This column is required.
         * <p>
         * Type: INTEGER (int)
         *
         * @see #FLAG_LOCAL_ONLY
         * @see #FLAG_SUPPORTS_CREATE
         * @see #FLAG_SUPPORTS_RECENTS
         * @see #FLAG_SUPPORTS_SEARCH
         */
        public static final String COLUMN_FLAGS = "flags";

        /**
         * Icon resource ID for a root. This column is required.
         * <p>
         * Type: INTEGER (int)
         */
        public static final String COLUMN_ICON = "icon";

        /**
         * Title for a root, which will be shown to a user. This column is
         * required. For a single storage service surfacing multiple accounts as
         * different roots, this title should be the name of the service.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * Summary for this root, which may be shown to a user. This column is
         * optional, and may be {@code null}. For a single storage service
         * surfacing multiple accounts as different roots, this summary should
         * be the name of the account.
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_SUMMARY = "summary";

        /**
         * Document which is a directory that represents the top directory of
         * this root. This column is required.
         * <p>
         * Type: STRING
         *
         * @see Document#COLUMN_DOCUMENT_ID
         */
        public static final String COLUMN_DOCUMENT_ID = "document_id";

        /**
         * Number of bytes available in this root. This column is optional, and
         * may be {@code null} if unknown or unbounded.
         * <p>
         * Type: INTEGER (long)
         */
        public static final String COLUMN_AVAILABLE_BYTES = "available_bytes";

        /**
         * MIME types supported by this root. This column is optional, and if
         * {@code null} the root is assumed to support all MIME types. Multiple
         * MIME types can be separated by a newline. For example, a root
         * supporting audio might return "audio/*\napplication/x-flac".
         * <p>
         * Type: STRING
         */
        public static final String COLUMN_MIME_TYPES = "mime_types";

        /** {@hide} */
        public static final String MIME_TYPE_ITEM = "vnd.android.document/root";

        /**
         * Flag indicating that at least one directory under this root supports
         * creating content. Roots with this flag will be shown when an
         * application interacts with {@link Intent#ACTION_CREATE_DOCUMENT}.
         *
         * @see #COLUMN_FLAGS
         */
        public static final int FLAG_SUPPORTS_CREATE = 1;

        /**
         * Flag indicating that this root offers content that is strictly local
         * on the device. That is, no network requests are made for the content.
         *
         * @see #COLUMN_FLAGS
         * @see Intent#EXTRA_LOCAL_ONLY
         */
        public static final int FLAG_LOCAL_ONLY = 1 << 1;

        /**
         * Flag indicating that this root can be queried to provide recently
         * modified documents.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsContract#buildRecentDocumentsUri(String, String)
         * @see DocumentsProvider#queryRecentDocuments(String, String[])
         */
        public static final int FLAG_SUPPORTS_RECENTS = 1 << 2;

        /**
         * Flag indicating that this root supports search.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsContract#buildSearchDocumentsUri(String, String,
         *      String)
         * @see DocumentsProvider#querySearchDocuments(String, String,
         *      String[])
         */
        public static final int FLAG_SUPPORTS_SEARCH = 1 << 3;

        /**
         * Flag indicating that this root supports testing parent child
         * relationships.
         *
         * @see #COLUMN_FLAGS
         * @see DocumentsProvider#isChildDocument(String, String)
         */
        public static final int FLAG_SUPPORTS_IS_CHILD = 1 << 4;

        /**
         * Flag indicating that this root is currently empty. This may be used
         * to hide the root when opening documents, but the root will still be
         * shown when creating documents and {@link #FLAG_SUPPORTS_CREATE} is
         * also set. If the value of this flag changes, such as when a root
         * becomes non-empty, you must send a content changed notification for
         * {@link DocumentsContract#buildRootsUri(String)}.
         *
         * @see #COLUMN_FLAGS
         * @see ContentResolver#notifyChange(Uri,
         *      android.database.ContentObserver, boolean)
         * @hide
         */
        public static final int FLAG_EMPTY = 1 << 16;

        /**
         * Flag indicating that this root should only be visible to advanced
         * users.
         *
         * @see #COLUMN_FLAGS
         * @hide
         */
        public static final int FLAG_ADVANCED = 1 << 17;
    }

    /**
     * Optional boolean flag included in a directory {@link Cursor#getExtras()}
     * indicating that a document provider is still loading data. For example, a
     * provider has returned some results, but is still waiting on an
     * outstanding network request. The provider must send a content changed
     * notification when loading is finished.
     *
     * @see ContentResolver#notifyChange(Uri, android.database.ContentObserver,
     *      boolean)
     */
    public static final String EXTRA_LOADING = "loading";

    /**
     * Optional string included in a directory {@link Cursor#getExtras()}
     * providing an informational message that should be shown to a user. For
     * example, a provider may wish to indicate that not all documents are
     * available.
     */
    public static final String EXTRA_INFO = "info";

    /**
     * Optional string included in a directory {@link Cursor#getExtras()}
     * providing an error message that should be shown to a user. For example, a
     * provider may wish to indicate that a network error occurred. The user may
     * choose to retry, resulting in a new query.
     */
    public static final String EXTRA_ERROR = "error";

    /** {@hide} */
    public static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
    /** {@hide} */
    public static final String METHOD_RENAME_DOCUMENT = "android:renameDocument";
    /** {@hide} */
    public static final String METHOD_DELETE_DOCUMENT = "android:deleteDocument";

    /** {@hide} */
    public static final String EXTRA_URI = "uri";

    private static final String PATH_ROOT = "root";
    private static final String PATH_RECENT = "recent";
    private static final String PATH_DOCUMENT = "document";
    private static final String PATH_CHILDREN = "children";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_TREE = "tree";

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_MANAGE = "manage";

    /**
     * Build URI representing the roots of a document provider. When queried, a
     * provider will return one or more rows with columns defined by
     * {@link Root}.
     *
     * @see android.provider.DocumentsProvider#queryRoots(String[])
     */
    public static Uri buildRootsUri(String authority) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority).appendPath(PATH_ROOT).build();
    }

    /**
     * Build URI representing the given {@link Root#COLUMN_ROOT_ID} in a
     * document provider.
     *
     * @see #getRootId(Uri)
     */
    public static Uri buildRootUri(String authority, String rootId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority).appendPath(PATH_ROOT).appendPath(rootId).build();
    }

    /**
     * Build URI representing the recently modified documents of a specific root
     * in a document provider. When queried, a provider will return zero or more
     * rows with columns defined by {@link Document}.
     *
     * @see android.provider.DocumentsProvider#queryRecentDocuments(String, String[])
     * @see #getRootId(Uri)
     */
    public static Uri buildRecentDocumentsUri(String authority, String rootId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority).appendPath(PATH_ROOT).appendPath(rootId)
                .appendPath(PATH_RECENT).build();
    }

    /**
     * Build URI representing access to descendant documents of the given
     * {@link Document#COLUMN_DOCUMENT_ID}.
     *
     * @see #getTreeDocumentId(Uri)
     */
    public static Uri buildTreeDocumentUri(String authority, String documentId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority)
                .appendPath(PATH_TREE).appendPath(documentId).build();
    }

    /**
     * Build URI representing the target {@link Document#COLUMN_DOCUMENT_ID} in
     * a document provider. When queried, a provider will return a single row
     * with columns defined by {@link Document}.
     *
     * @see android.provider.DocumentsProvider#queryDocument(String, String[])
     * @see #getDocumentId(Uri)
     */
    public static Uri buildDocumentUri(String authority, String documentId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority).appendPath(PATH_DOCUMENT).appendPath(documentId).build();
    }

    /**
     * Build URI representing the target {@link Document#COLUMN_DOCUMENT_ID} in
     * a document provider. When queried, a provider will return a single row
     * with columns defined by {@link Document}.
     * <p>
     * However, instead of directly accessing the target document, the returned
     * URI will leverage access granted through a subtree URI, typically
     * returned by {@link Intent#ACTION_OPEN_DOCUMENT_TREE}. The target document
     * must be a descendant (child, grandchild, etc) of the subtree.
     * <p>
     * This is typically used to access documents under a user-selected
     * directory tree, since it doesn't require the user to separately confirm
     * each new document access.
     *
     * @param treeUri the subtree to leverage to gain access to the target
     *            document. The target directory must be a descendant of this
     *            subtree.
     * @param documentId the target document, which the caller may not have
     *            direct access to.
     * @see Intent#ACTION_OPEN_DOCUMENT_TREE
     * @see android.provider.DocumentsProvider#isChildDocument(String, String)
     * @see #buildDocumentUri(String, String)
     */
    public static Uri buildDocumentUriUsingTree(Uri treeUri, String documentId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(treeUri.getAuthority()).appendPath(PATH_TREE)
                .appendPath(getTreeDocumentId(treeUri)).appendPath(PATH_DOCUMENT)
                .appendPath(documentId).build();
    }

    /** {@hide} */
    public static Uri buildDocumentUriMaybeUsingTree(Uri baseUri, String documentId) {
        if (isTreeUri(baseUri)) {
            return buildDocumentUriUsingTree(baseUri, documentId);
        } else {
            return buildDocumentUri(baseUri.getAuthority(), documentId);
        }
    }

    /**
     * Build URI representing the children of the target directory in a document
     * provider. When queried, a provider will return zero or more rows with
     * columns defined by {@link Document}.
     *
     * @param parentDocumentId the document to return children for, which must
     *            be a directory with MIME type of
     *            {@link Document#MIME_TYPE_DIR}.
     * @see android.provider.DocumentsProvider#queryChildDocuments(String, String[], String)
     * @see #getDocumentId(Uri)
     */
    public static Uri buildChildDocumentsUri(String authority, String parentDocumentId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority)
                .appendPath(PATH_DOCUMENT).appendPath(parentDocumentId).appendPath(PATH_CHILDREN)
                .build();
    }

    /**
     * Build URI representing the children of the target directory in a document
     * provider. When queried, a provider will return zero or more rows with
     * columns defined by {@link Document}.
     * <p>
     * However, instead of directly accessing the target directory, the returned
     * URI will leverage access granted through a subtree URI, typically
     * returned by {@link Intent#ACTION_OPEN_DOCUMENT_TREE}. The target
     * directory must be a descendant (child, grandchild, etc) of the subtree.
     * <p>
     * This is typically used to access documents under a user-selected
     * directory tree, since it doesn't require the user to separately confirm
     * each new document access.
     *
     * @param treeUri the subtree to leverage to gain access to the target
     *            document. The target directory must be a descendant of this
     *            subtree.
     * @param parentDocumentId the document to return children for, which the
     *            caller may not have direct access to, and which must be a
     *            directory with MIME type of {@link Document#MIME_TYPE_DIR}.
     * @see Intent#ACTION_OPEN_DOCUMENT_TREE
     * @see android.provider.DocumentsProvider#isChildDocument(String, String)
     * @see #buildChildDocumentsUri(String, String)
     */
    public static Uri buildChildDocumentsUriUsingTree(Uri treeUri, String parentDocumentId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(treeUri.getAuthority()).appendPath(PATH_TREE)
                .appendPath(getTreeDocumentId(treeUri)).appendPath(PATH_DOCUMENT)
                .appendPath(parentDocumentId).appendPath(PATH_CHILDREN).build();
    }

    /**
     * Build URI representing a search for matching documents under a specific
     * root in a document provider. When queried, a provider will return zero or
     * more rows with columns defined by {@link Document}.
     *
     * @see android.provider.DocumentsProvider#querySearchDocuments(String, String, String[])
     * @see #getRootId(Uri)
     * @see #getSearchDocumentsQuery(Uri)
     */
    public static Uri buildSearchDocumentsUri(
            String authority, String rootId, String query) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority)
                .appendPath(PATH_ROOT).appendPath(rootId).appendPath(PATH_SEARCH)
                .appendQueryParameter(PARAM_QUERY, query).build();
    }

    /** {@hide} */
    public static boolean isTreeUri(Uri uri) {
        final List<String> paths = uri.getPathSegments();
        return (paths.size() >= 2 && PATH_TREE.equals(paths.get(0)));
    }

    /**
     * Extract the {@link Root#COLUMN_ROOT_ID} from the given URI.
     */
    public static String getRootId(Uri rootUri) {
        final List<String> paths = rootUri.getPathSegments();
        if (paths.size() >= 2 && PATH_ROOT.equals(paths.get(0))) {
            return paths.get(1);
        }
        throw new IllegalArgumentException("Invalid URI: " + rootUri);
    }

    /**
     * Extract the {@link Document#COLUMN_DOCUMENT_ID} from the given URI.
     *
     * @see #isDocumentUri(Context, Uri)
     */
    public static String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_DOCUMENT.equals(paths.get(0))) {
            return paths.get(1);
        }
        if (paths.size() >= 4 && PATH_TREE.equals(paths.get(0))
                && PATH_DOCUMENT.equals(paths.get(2))) {
            return paths.get(3);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    /**
     * Extract the via {@link Document#COLUMN_DOCUMENT_ID} from the given URI.
     */
    public static String getTreeDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_TREE.equals(paths.get(0))) {
            return paths.get(1);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    /**
     * Extract the search query from a URI built by
     * {@link #buildSearchDocumentsUri(String, String, String)}.
     */
    public static String getSearchDocumentsQuery(Uri searchDocumentsUri) {
        return searchDocumentsUri.getQueryParameter(PARAM_QUERY);
    }

    /** {@hide} */
    public static Uri setManageMode(Uri uri) {
        return uri.buildUpon().appendQueryParameter(PARAM_MANAGE, "true").build();
    }

    /** {@hide} */
    public static boolean isManageMode(Uri uri) {
        return uri.getBooleanQueryParameter(PARAM_MANAGE, false);
    }

}
