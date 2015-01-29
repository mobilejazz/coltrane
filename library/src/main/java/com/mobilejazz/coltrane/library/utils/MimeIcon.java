package com.mobilejazz.coltrane.library.utils;

import android.content.Context;
import android.net.Uri;

import com.mobilejazz.coltrane.library.R;

import java.util.HashMap;
import java.util.Map;

public class MimeIcon {

    private static Map<String, Integer> mapping = null;

    private static void initialize() {
        mapping = new HashMap<String, Integer>();
        mapping.put("text", R.drawable.text_x_generic);
        mapping.put("image", R.drawable.image_x_generic);
        mapping.put("application/pdf", R.drawable.application_pdf);
        // TODO add all icons here
    }

    public static int getIcon(String mimeType) {
        if (mapping == null) {
            initialize();
        }
        Integer exactMatch = mapping.get(mimeType);
        if (exactMatch == null) {
            String[] splitted = mimeType.split("/");
            Integer categoryMatch = mapping.get(splitted[0]);
            if (categoryMatch == null) {
                return R.drawable.unknown;
            } else {
                return categoryMatch;
            }
        } else {
            return exactMatch;
        }
    }

    public static Uri getUri(Context context, String mimeType) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + getIcon(mimeType));
    }

}
