package com.waldm.proverbica.retriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.waldm.proverbica.SayingDisplayer;
import com.waldm.proverbica.infrastructure.NetworkConnectivity;
import com.waldm.proverbica.settings.SettingsFragment;

public class FileSayingRetriever implements SayingRetriever {

    private static final String TAG = FileSayingRetriever.class.getSimpleName();
    private static final String FILENAME = "sayings.txt";
    private List<String> sayings;
    private final Context context;
    private final SayingDisplayer sayingDisplayer;

    public FileSayingRetriever(Context context, SayingDisplayer sayingDisplayer) {
        this.context = context;
        this.sayingDisplayer = sayingDisplayer;
    }

    @Override
    public SayingRetriever loadSayingAndRefresh() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean alwaysUseFile = sharedPref.getBoolean(SettingsFragment.KEY_PREF_ALWAYS_USE_FILE, false);

        if (NetworkConnectivity.isNetworkAvailable(context) && !alwaysUseFile) {
            return new WebSayingRetriever(context, sayingDisplayer).loadSayingAndRefresh();
        } else {
            sayingDisplayer.setText(loadSaying());
            return this;
        }
    }

    @Override
    public String loadSaying() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean alwaysUseFile = sharedPref.getBoolean(SettingsFragment.KEY_PREF_ALWAYS_USE_FILE, false);

        if (NetworkConnectivity.isNetworkAvailable(context) && !alwaysUseFile) {
            return new WebSayingRetriever(context, sayingDisplayer).loadSaying();
        } else {
            Log.d(TAG, "Loading saying from file");
            if (sayings == null) {
                sayings = new ArrayList<String>();

                InputStream stream = null;
                try {
                    stream = context.getAssets().open(FILENAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String saying;
                try {
                    while ((saying = reader.readLine()) != null) {
                        sayings.add(saying);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Random random = new Random();
            String beginning = sayings.get(random.nextInt(sayings.size())).split("\\|")[0];
            String end = sayings.get(random.nextInt(sayings.size())).split("\\|")[1];

            return beginning + " " + end;
        }
    }
}
