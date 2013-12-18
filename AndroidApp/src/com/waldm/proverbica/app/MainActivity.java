package com.waldm.proverbica.app;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.common.base.Stopwatch;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.waldm.proverbica.R;
import com.waldm.proverbica.Saying;
import com.waldm.proverbica.SayingDisplayer;
import com.waldm.proverbica.infrastructure.ImageHandler;
import com.waldm.proverbica.infrastructure.NetworkConnectivity;
import com.waldm.proverbica.infrastructure.SayingSource;
import com.waldm.proverbica.retriever.FileSayingRetriever;
import com.waldm.proverbica.retriever.SayingRetriever;
import com.waldm.proverbica.retriever.WebSayingRetriever;
import com.waldm.proverbica.settings.SettingsActivity;
import com.waldm.proverbica.settings.SettingsManager;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener, SayingDisplayer, Target {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SayingRetriever sayingRetriever;
    private ImageHandler imageHandler;
    private String text;
    private TextView textView;
    private ImageView imageView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ShareActionProvider shareActionProvider;
    private Runnable showActionBar;
    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.app_name));
        textView = (TextView) findViewById(R.id.text_box);
        imageView = (ImageView) findViewById(R.id.image);

        imageHandler = new ImageHandler(this);
        imageHandler.setTarget(this);

        if (SettingsManager.getPrefAlwaysUseFile(this)) {
            sayingRetriever = new FileSayingRetriever(this, this);
        } else {
            sayingRetriever = new WebSayingRetriever(this, this);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayingRetriever = sayingRetriever.loadSayingAndRefresh(SayingSource.EITHER);
                getActionBar().hide();
                stopwatch.reset();
                stopwatch.start();
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                getActionBar().show();
                stopwatch.reset();
                stopwatch.start();
                return true;
            }
        });

        sayingRetriever = sayingRetriever.loadSayingAndRefresh(SayingSource.EITHER);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopwatch.stop();
        handler.removeCallbacks(showActionBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopwatch.start();

        showActionBar = new Runnable() {
            @Override
            public void run() {
                if (stopwatch.elapsed(TimeUnit.SECONDS) > 10) {
                    MainActivity.this.getActionBar().show();
                    sayingRetriever = sayingRetriever.loadSayingAndRefresh(SayingSource.EITHER);
                    stopwatch.reset();
                    stopwatch.start();
                }

                handler.postDelayed(showActionBar, 10000);
            }
        };

        handler.postDelayed(showActionBar, 0);

        if (SettingsManager.getPrefKeepScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void setSaying(Saying saying) {
        text = saying.getText();
        if (shareActionProvider != null) {
            updateShareIntent();
        }

        imageHandler.loadNextImage(saying.getImageLocation());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        shareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();

        updateShareIntent();
        return true;
    }

    private void updateShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text + " - http://proverbica.com");
        shareIntent.setType("text/plain");
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_always_file_key))) {
            if (SettingsManager.getPrefAlwaysUseFile(this)) {
                sayingRetriever = new FileSayingRetriever(this, this);
            } else {
                sayingRetriever = new WebSayingRetriever(this, this);
            }
        }
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
        if (NetworkConnectivity.isNetworkAvailable(this) && !SettingsManager.getPrefAlwaysUseFile(this)) {
            Log.d(TAG, "Loading image");
            textView.setText(R.string.loading_proverb);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
        Log.d(TAG, "Image loaded");
        imageView.setImageBitmap(bitmap);
        textView.setText(text);
    }

    @Override
    public void onBitmapFailed(Drawable arg0) {
        Log.d(TAG, "Image failed to load");
        textView.setText(R.string.failed_to_load_proverb);
    }
}
