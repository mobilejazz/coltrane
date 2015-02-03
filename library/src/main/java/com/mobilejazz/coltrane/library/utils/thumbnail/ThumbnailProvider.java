package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * A generic interface for classes that generate a thumbnail provided with a
 * {@link android.os.ParcelFileDescriptor}. Multiple instances of this interface
 * can be combined in {@link Thumbnail} to collectively provide thumbanil support
 * for various file types.
 */
public interface ThumbnailProvider {

    /**
     * Returns a bitmap representing the thumbnail of the given file.
     * @param input A parcel file descriptor.
     * @param sizeHint The ideal with and height of the generated thumbnail.
     * @param mimeType The mime type of the data contained in the file descriptor.
     * @return A {@link Bitmap} representing the thumbnail.
     * @throws IOException
     * @throws java.lang.IllegalArgumentException if the mime type is not supported
     */
    public Bitmap getThumbnail(final ParcelFileDescriptor input, final Point sizeHint, final String mimeType) throws IOException;

    public boolean supportsMimeType(final String mimeType);

}
