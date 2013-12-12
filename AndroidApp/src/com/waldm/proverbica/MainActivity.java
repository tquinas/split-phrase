package com.waldm.proverbica;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.waldm.proverbica.retriever.FileSayingRetriever;
import com.waldm.proverbica.retriever.SayingDisplayer;
import com.waldm.proverbica.retriever.SayingRetriever;
import com.waldm.proverbica.retriever.WebSayingRetriever;
import com.waldm.proverbica.settings.SettingsActivity;
import com.waldm.proverbica.settings.SettingsFragment;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener, SayingDisplayer {
    private SayingRetriever sayingRetriever;
    private ImageHandler imageHandler;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");
        final TextView textBox = (TextView) findViewById(R.id.text_box);
        final ImageView imageView = (ImageView) findViewById(R.id.image);

        Target target = new Target() {
            @Override
            public void onPrepareLoad(Drawable arg0) {
                textBox.setText(R.string.loading_proverb);
            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
                imageView.setImageBitmap(bitmap);
                textBox.setText(text);
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                textBox.setText(R.string.failed_to_load_proverb);
            }
        };

        imageHandler = new ImageHandler(this);
        imageHandler.setTarget(target);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean(SettingsFragment.KEY_PREF_ALWAYS_USE_FILE, false)) {
            sayingRetriever = new FileSayingRetriever(this, this);
        } else {
            sayingRetriever = new WebSayingRetriever(this, this);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayingRetriever = sayingRetriever.loadSayingAndRefresh();
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, textBox.getText() + " - www.proverbica.com");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_proverb)));
            }
        });

        sayingRetriever = sayingRetriever.loadSayingAndRefresh();
    }

    @Override
    public void setText(String result) {
        text = result;
        imageHandler.loadImage(imageHandler.getNextImage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
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
        if (key.equals(SettingsFragment.KEY_PREF_ALWAYS_USE_FILE)) {
            if (sharedPreferences.getBoolean(key, false)) {
                sayingRetriever = new FileSayingRetriever(this, this);
            } else {
                sayingRetriever = new WebSayingRetriever(this, this);
            }
        }
    }
}
