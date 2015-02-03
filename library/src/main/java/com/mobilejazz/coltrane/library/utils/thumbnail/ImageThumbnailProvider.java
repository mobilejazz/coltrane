package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class ImageThumbnailProvider implements ThumbnailProvider {

    @Override
    public Bitmap getThumbnail(ParcelFileDescriptor input, Point sizeHint, String mimeType) throws IOException {
        if (!supportsMimeType(mimeType))
            throw new IllegalArgumentException("Unsupported mime type: " + mimeType);
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

    @Override
    public boolean supportsMimeType(String mimeType) {
        return mimeType.startsWith("image/");
    }

}
