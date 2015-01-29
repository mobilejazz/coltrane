package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.library.utils.MimeIcon;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import net.sf.andpdf.nio.ByteBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class Thumbnail {

    public static Bitmap fromImage(final ParcelFileDescriptor input, final Point sizeHint) throws IOException {
        try {

            // Assume documentId points to an image file. Build a thumbnail no
            // larger than twice the sizeHint
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(input.getFileDescriptor(), null, options);
            final int targetHeight = 2 * sizeHint.y;
            final int targetWidth = 2 * sizeHint.x;
            final int height = options.outHeight;
            final int width = options.outWidth;
            options.inSampleSize = 1;
            if (height > targetHeight || width > targetWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                // Calculate the largest inSampleSize value that is a power of 2 and
                // keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / options.inSampleSize) > targetHeight
                        || (halfWidth / options.inSampleSize) > targetWidth) {
                    options.inSampleSize *= 2;
                }
            }
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFileDescriptor(input.getFileDescriptor(), null, options);
        } finally {
            input.close();
        }
    }

    public static Bitmap fromPDF(final ParcelFileDescriptor input, final Point sizeHint) throws IOException {
        InputStream is = null;
        try {
            // Get the size of the file
            is = new ParcelFileDescriptor.AutoCloseInputStream(input);

            // Get the size of the file
            long length = input.getStatSize();
            byte[] bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            PDFFile pdfFile = new PDFFile(buffer);
            PDFPage page = pdfFile.getPage(1, true);

            float pageWidth = page.getWidth();
            float pageHeight = page.getHeight();

            float aspectRatio = pageWidth / pageHeight;
            float desiredAspectRatio = (float) sizeHint.x / (float) sizeHint.y;

            int w, h;
            if (aspectRatio > desiredAspectRatio) {
                w = sizeHint.x;
                h = (int) (w / aspectRatio);
            } else {
                h = sizeHint.y;
                w = (int) (h * aspectRatio);
            }

            return page.getImage(w, h, null, true, true);

        } catch (Exception e) {
            // something went wrong:
            e.printStackTrace();
            return null;
        } finally {
            is.close();
        }
    }

    public static Bitmap fromFileDescriptor(final ParcelFileDescriptor input, final Point sizeHint, final String mimeType) throws IOException {
        if ("application/pdf".equals(mimeType)) {
            return fromPDF(input, sizeHint);
        } else if (mimeType.startsWith("image")) {
            return fromImage(input, sizeHint);
        } else {
            throw new IllegalArgumentException("Can only create a thumbnail from images and pdf.");
        }
    }

    public static Bitmap fromFile(final File file, final Point sizeHint, final String mimeType) throws IOException {
        return fromFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), sizeHint, mimeType);
    }

    public static Bitmap fromFile(final String file, final Point sizeHint, final String mimeType) throws IOException {
        return fromFile(new File(file), sizeHint, mimeType);
    }

    public static boolean isSupported(String mimeType) {
        return "application/pdf".equals(mimeType) || mimeType.startsWith("image");
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
