package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.utils.MimeIcon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;

public class Thumbnail {

    private static List<ThumbnailProvider> providers = new ArrayList<ThumbnailProvider>() {{
       // by default add Image Thumbnail Provider:
       add(new ImageThumbnailProvider());
    }};

    public static void addProvider(ThumbnailProvider provider) {
        providers.add(provider);
    }

    public static Bitmap fromFileDescriptor(final ParcelFileDescriptor input, final Point sizeHint, final String mimeType) throws IOException {
        ListIterator<ThumbnailProvider> li = providers.listIterator(providers.size());
        while (li.hasPrevious()) {
            ThumbnailProvider p = li.previous();
            if (p.supportsMimeType(mimeType)) {
                return p.getThumbnail(input, sizeHint, mimeType);
            }
        }
        // if we arrive here:
        throw new IllegalArgumentException("Unsupported mime type: " + mimeType);
    }

    public static Bitmap fromFile(final File file, final Point sizeHint, final String mimeType) throws IOException {
        return fromFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), sizeHint, mimeType);
    }

    public static Bitmap fromFile(final String file, final Point sizeHint, final String mimeType) throws IOException {
        return fromFile(new File(file), sizeHint, mimeType);
    }

    public static boolean isSupported(String mimeType) {
        ListIterator<ThumbnailProvider> li = providers.listIterator(providers.size());
        while (li.hasPrevious()) {
            ThumbnailProvider p = li.previous();
            if (p.supportsMimeType(mimeType)) {
                return true;
            }
        }
        // if we arrive here:
        return false;
    }

    protected static String getDocumentThumbnailId(final String documentId, final Point sizeHint) {
        return Base64.encodeToString(documentId.getBytes(), Base64.URL_SAFE) + "_" + sizeHint.x + "_" + sizeHint.y;
    }

    public static File getDocumentThumbnailFile(Context context, File file, final String documentId, String mimeType, final Point sizeHint) throws FileNotFoundException {
        FileOutputStream out = null;
        try {
            Bitmap bitmap = Thumbnail.fromFile(file, sizeHint, mimeType);
            if (bitmap != null) {
                File cacheDir = context.getCacheDir();
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

    public static Uri getDocumentThumbnailUri(Context context, File file, String documentId, String mimeType, Point sizeHint) throws FileNotFoundException, UserRecoverableException {
        if (isSupported(mimeType)) {
            File thumbnailFile = Thumbnail.getDocumentThumbnailFile(context, file, documentId, mimeType, sizeHint);
            if (thumbnailFile != null) {
                return Uri.fromFile(thumbnailFile);
            } else {
                return MimeIcon.getUri(context, mimeType);
            }
        } else {
            return MimeIcon.getUri(context, mimeType);
        }
    }

}
