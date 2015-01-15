package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import net.sf.andpdf.nio.ByteBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Thumbnail {

    public static Bitmap fromImage(final String path, final Point sizeHint) {
        // Assume documentId points to an image file. Build a thumbnail no
        // larger than twice the sizeHint
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
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
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap fromPDF(final String path, final Point sizeHint) {
        FileInputStream is = null;
        try {

            File file = new File(path);
            is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            byte[] bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            PDFFile pdfFile = new PDFFile(buffer);
            PDFPage page = pdfFile.getPage(1, true);
            return page.getImage(sizeHint.x, sizeHint.y, null, true, true);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Bitmap fromFile(final String path, final Point sizeHint, final String mimeType) {
        if ("application/pdf".equals(mimeType)) {
            return fromPDF(path, sizeHint);
        } else if (mimeType.startsWith("image")) {
            return fromImage(path, sizeHint);
        } else {
            throw new IllegalArgumentException("Can only create a thumbnail from images and pdf.");
        }
    }

}
