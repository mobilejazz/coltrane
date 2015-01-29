package com.mobilejazz.coltrane.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;

import java.util.ArrayList;

public class ProviderChooser extends DialogFragment {

    public interface OnProviderSelectedListener {

        public void onProviderSelected(DocumentsProvider provider);

    }

    private static final String ARG_PROVIDERS = "com.mobilejazz.coltrane.ui.ProviderChooser.ARG_PROVIDERS";

    public static ProviderChooser newInstance(ArrayList<String> providerIds) {
        ProviderChooser f = new ProviderChooser();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PROVIDERS, providerIds);
        f.setArguments(args);

        return f;
    }

    private ArrayList<String> mProviderIds;
    private ProviderAdapter mAdapter;
    private OnProviderSelectedListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            setArguments(savedInstanceState);
        }

        mProviderIds = getArguments().getStringArrayList(ARG_PROVIDERS);

        mAdapter = new ProviderAdapter(getActivity(), R.layout.root);
        for (String providerId : mProviderIds) {
            mAdapter.add(DocumentsProviderRegistry.get().getProvider(providerId));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnProviderSelectedListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnProviderSelectedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_account)
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DocumentsProvider provider = mAdapter.getItem(which);
                        mListener.onProviderSelected(provider);
                        dismissAllowingStateLoss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(ARG_PROVIDERS, mProviderIds);
    }
}
