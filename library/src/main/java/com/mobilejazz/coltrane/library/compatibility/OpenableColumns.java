package com.mobilejazz.coltrane.library.compatibility;

/**
 * These are standard columns for openable URIs. Providers that serve openable
 * URIs <em>must</em> support at least these columns when queried.
 * <p>
 * To find the content type of a URI, use
 * {@link ContentResolver#getType(android.net.Uri)}.
 *
 * @see Intent#CATEGORY_OPENABLE
 */
public interface OpenableColumns {

    /**
     * The human-friendly name of file. If this is not provided then the name should default to the
     * the last segment of the file's URI.
     */
    public static final String DISPLAY_NAME = "_display_name";

    /**
     * The number of bytes in the file identified by the openable URI. Null if unknown.
     */
    public static final String SIZE = "_size";
}
