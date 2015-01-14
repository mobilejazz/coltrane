package com.mobilejazz.coltrane.library.utils.thumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.File;
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
        // TODO: PDFBox
        try {
            PDDocument document = PDDocument.loadLegacy(new File(path));
            document.getPage(1).getCropBox().getWidth();
            PDFRenderer renderer = new PDFRenderer(document);
            return renderer.renderImage(1);
        } catch (IOException e) {
            e.printStackTrace();
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
