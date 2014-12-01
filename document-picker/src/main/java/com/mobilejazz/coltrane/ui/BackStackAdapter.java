package com.mobilejazz.coltrane.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BackStackAdapter extends BaseAdapter implements FragmentManager.OnBackStackChangedListener {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private FragmentManager mFragmentManager;
    private int mLayoutResId;
    private int mDropdownLayoutResId;

    public BackStackAdapter(Context context, FragmentManager fragmentManager, int layoutResId, int dropdownLayoutResId) {
        mContext = context;
        mFragmentManager = fragmentManager;
        mLayoutResId = layoutResId;
        mDropdownLayoutResId = dropdownLayoutResId;
        mLayoutInflater = LayoutInflater.from(context);

        mFragmentManager.addOnBackStackChangedListener(this);
    }

    @Override
    public int getCount() {
        return mFragmentManager.getBackStackEntryCount();
    }

    @Override
    public String getItem(int position) {
        return mFragmentManager.getBackStackEntryAt(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return mFragmentManager.getBackStackEntryAt(position).getId();
    }

    private View getView(int position, View convertView, ViewGroup parent, int layout) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(layout, parent, false);
        }

        bindView(mContext, convertView, mFragmentManager.getBackStackEntryAt(position));

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, mLayoutResId);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, mDropdownLayoutResId);
    }

    protected void bindView(Context context, View view, FragmentManager.BackStackEntry e) {
        TextView tv = (TextView)view;
        tv.setText(e.getName());
    }

    @Override
    public void onBackStackChanged() {
        notifyDataSetChanged();
    }
}
