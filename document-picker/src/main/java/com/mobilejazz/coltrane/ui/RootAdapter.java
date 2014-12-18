package com.mobilejazz.coltrane.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobilejazz.coltrane.library.Root;

import java.util.List;

public class RootAdapter extends ArrayAdapter<Root> {

    public RootAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.root, parent, false);
        }

        Root r = getItem(position);
        TextView tv = (TextView)convertView;

        tv.setText(r.getTitle());
        tv.setCompoundDrawablesWithIntrinsicBounds(r.getIcon(), 0, 0, 0);

        return convertView;
    }
}
