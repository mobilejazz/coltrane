package com.mobilejazz.coltrane.example;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobilejazz.coltrane.library.DocumentsProvider;
import com.mobilejazz.coltrane.library.DocumentsProviderRegistry;
import com.mobilejazz.coltrane.library.UserRecoverableException;
import com.mobilejazz.coltrane.ui.DocumentBrowserActivity;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;


public class FileDetailActivity extends Activity {

    private DocumentsProvider mProvider;
    private String mDocumentId;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_detail);

        // get the documents thumbnail:

        mProvider = DocumentsProviderRegistry.get().getProvider(getIntent().getStringExtra(DocumentBrowserActivity.EXTRA_PROVIDER));
        mDocumentId = getIntent().getStringExtra(DocumentBrowserActivity.EXTRA_DOCUMENT_ID);

        mImageView = (ImageView)findViewById(R.id.thumbnail);

    }

    @Override
    protected void onResume() {
        super.onResume();


            new AsyncTask<Void, Void, Uri>() {

                @Override
                protected Uri doInBackground(Void... params) {
                    try {
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        return mProvider.getDocumentThumbnailUri(mDocumentId, size, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UserRecoverableException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Uri uri) {
                    super.onPostExecute(uri);
                    Toast.makeText(FileDetailActivity.this, uri.toString(), Toast.LENGTH_LONG).show();
                    Picasso.with(FileDetailActivity.this).load(uri).into(mImageView);}
            }.execute();
    }
}
