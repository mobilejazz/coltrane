package com.mobilejazz.coltrane.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobilejazz.coltrane.library.DocumentsProvider;

public class ProviderAdapter extends ArrayAdapter<DocumentsProvider> {

        public ProviderAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.root, parent, false);
            }

            DocumentsProvider provider = getItem(position);
            TextView tv = (TextView) convertView;

            tv.setText(getContext().getString(R.string.account_name, provider.getName()));
            tv.setCompoundDrawablesWithIntrinsicBounds(provider.getIcon(), 0, 0, 0);

            return convertView;
        }
    }