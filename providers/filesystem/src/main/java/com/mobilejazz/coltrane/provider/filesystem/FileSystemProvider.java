package com.mobilejazz.coltrane.provider.filesystem;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Base64;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.Root;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;
import com.mobilejazz.coltrane.library.utils.DocumentCursor;
import com.mobilejazz.coltrane.library.utils.ListCursor;
import com.mobilejazz.coltrane.library.utils.thumbnail.Thumbnail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class FileSystemProvider extends DocumentsProvider {

    public static final String ID = "com.mobilejazz.coltrane.provider.filesystem";

    /**
     * Default document projection: everything but Document.COLUMN_ICON and
     * Document.COLUMN_SUMMARY
     */
    private final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
            BaseColumns._ID,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
    };

    private FileAccessor mAccessor = new FileAccessor();

    public FileSystemProvider(Context context) {
        super(context);
    }

    @Override
    public List<Root> getRoots() throws FileNotFoundException {
        // Add Home directory
        File homeDir = Environment.getExternalStorageDirectory();

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            Root root = new Root(this,
                    homeDir.getAbsolutePath(),
                    homeDir.getAbsolutePath(),
                    getContext().getString(R.string.internal_storage),
                    R.drawable.ic_provider_filesystem,
                    homeDir.getFreeSpace(),
                    DocumentsContract.Root.FLAG_LOCAL_ONLY | DocumentsContract.Root.FLAG_SUPPORTS_CREATE);

            return Collections.singletonList(root);

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return getContext().getString(R.string.filesystem_name);
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_provider_filesystem;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        final File parent = new File(parentDocumentId);
        List<File> files = new ArrayList<File>();
        for (File file : parent.listFiles()) {
            // Don't show hidden files/folders
            if (!file.getName().startsWith(".")) {
                // Adds the file's display name, MIME type, size, and so on.
                files.add(file);
            }
        }
        return new ListCursor<File>(files, mAccessor, projection, sortOrder);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return new ListCursor<File>(Collections.singletonList(new File(documentId)), mAccessor, projection, null);
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File file = new File(documentId);
        return descriptorFromFile(file, mode);
    }

    protected String getDocumentThumbnailId(final String documentId, final Point sizeHint) {
        return Base64.encodeToString(documentId.getBytes(), Base64.URL_SAFE) + "_" + sizeHint.x + "_" + sizeHint.y;
    }

    protected File getDocumentThumbnailFile(final String documentId, final Point sizeHint) throws FileNotFoundException {
        FileOutputStream out = null;
        try {
            String mimeType = mAccessor.getMimeType(new File(documentId));
            Bitmap bitmap = Thumbnail.fromFile(documentId, sizeHint, mimeType);
            if (bitmap != null) {
                File cacheDir = getContext().getCacheDir();
                File thumbnail = File.createTempFile(getDocumentThumbnailId(documentId, sizeHint), "", cacheDir);
                out = new FileOutputStream(thumbnail);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                return thumbnail;
            } else {
                return null;
            }
        } catch (IOException e) {
            Timber.e("Error writing thumbnail", e);
            return null;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    Timber.e("Error closing thumbnail", e);
                }
        }
    }

    @Override
    public Uri getDocumentThumbnailUri(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException, UserRecoverableException {
        File thumbnailFile = getDocumentThumbnailFile(documentId, sizeHint);
        if (thumbnailFile != null) {
            return Uri.fromFile(thumbnailFile);
        } else {
            return null;
        }
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        File newFile = new File(parentDocumentId, displayName);
        try {
            newFile.createNewFile();
            return newFile.getAbsolutePath();
        } catch (IOException e) {
            Timber.e("Error creating new file " + newFile);
        }
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }

    public static void register(Context context) {
        DocumentsProviderRegistry.get().register(ID, new FileSystemProvider(context));
    }

//    private void includeFile(final MatrixCursor result, final File file, long id)
//            throws FileNotFoundException {
//        final MatrixCursor.RowBuilder row = result.newRow();
//        // These columns are required
//        row.add(BaseColumns._ID, id); // for supporting CursorAdapter
//        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.getAbsolutePath());
//        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.getName());
//        String mimeType = getDocumentType(file.getAbsolutePath());
//        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
//        int flags = file.canWrite() ? DocumentsContract.Document.FLAG_SUPPORTS_DELETE | DocumentsContract.Document.FLAG_SUPPORTS_WRITE
//                : 0;
//        // We only show thumbnails for image files - expect a call to
//        // openDocumentThumbnail for each file that has
//        // this flag set
//        if (mimeType.startsWith("image/"))
//            flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
//        row.add(DocumentsContract.Document.COLUMN_FLAGS, flags);
//        // COLUMN_SIZE is required, but can be null
//        row.add(DocumentsContract.Document.COLUMN_SIZE, file.length());
//        // These columns are optional
//        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
//        // Document.COLUMN_ICON can be a resource id identifying a custom icon.
//        // The system provides default icons
//        // based on mime type
//        // Document.COLUMN_SUMMARY is optional additional information about the
//        // file
//    }
}
