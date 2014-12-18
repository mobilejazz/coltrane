package com.mobilejazz.coltrane.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BackStackAdapter extends BaseAdapter implements FragmentManager.OnBackStackChangedListener {

    private static final long HEADER_ID = -1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private FragmentManager mFragmentManager;
    private int mLayoutResId;
    private int mDropdownLayoutResId;

    private String mHeader;

    public BackStackAdapter(Context context, FragmentManager fragmentManager, int layoutResId, int dropdownLayoutResId) {
        mContext = context;
        mFragmentManager = fragmentManager;
        mLayoutResId = layoutResId;
        mDropdownLayoutResId = dropdownLayoutResId;
        mLayoutInflater = LayoutInflater.from(context);

        mFragmentManager.addOnBackStackChangedListener(this);

        mHeader = "Loading..."; // default header
    }

    public void setHeader(String header) {
        mHeader = header;
        notifyDataSetChanged();
    }

    public String getHeader() {
        return mHeader;
    }

    @Override
    public int getCount() {
        return mFragmentManager.getBackStackEntryCount() + 1;
    }

    @Override
    public String getItem(int position) {
        if (position == 0) {
            return mHeader;
        } else {
            return mFragmentManager.getBackStackEntryAt(position - 1).getName();
        }
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return HEADER_ID;
        } else {
            return mFragmentManager.getBackStackEntryAt(position - 1).getId();
        }
    }

    private View getView(int position, View convertView, ViewGroup parent, int layout) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(layout, parent, false);
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = getView(position, convertView, parent, mLayoutResId);
        bindView(mContext, position, v, getItem(position));
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = getView(position, convertView, parent, mDropdownLayoutResId);
        bindDropDownView(mContext, position, v, getItem(position));
        return v;
    }

    protected void bindView(Context context, int position, View view, String entry) {
        TextView tv = (TextView)view;
        tv.setText(entry);
    }

    protected void bindDropDownView(Context context, int position, View view, String entry) {
        TextView tv = (TextView)view;
        if (position > 0) {
            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.level_down, 0, 0, 0);
        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        tv.setText(entry);
    }

    @Override
    public void onBackStackChanged() {
        notifyDataSetChanged();
    }
}
