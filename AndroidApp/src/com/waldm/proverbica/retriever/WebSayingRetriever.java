package com.waldm.proverbica.retriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.waldm.proverbica.MainActivity;
import com.waldm.proverbica.NetworkConnectivity;
import com.waldm.proverbica.settings.SettingsFragment;

public class WebSayingRetriever extends AsyncTask<String, Void, String> implements SayingRetriever {

    private static final String IMAGES_DIR = MainActivity.WEBSITE + "images/";
    private static final String TAG = WebSayingRetriever.class.getSimpleName();
    private final Context mainActivity;
    private final ImageView imageView;
    private final SayingDisplayer sayingDisplayer;

    public WebSayingRetriever(Context mainActivity, ImageView imageView, SayingDisplayer sayingDisplayer) {
        this.mainActivity = mainActivity;
        this.imageView = imageView;
        this.sayingDisplayer = sayingDisplayer;
    }

    @Override
    protected String doInBackground(String... urls) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(urls[0]);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet, localContext);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = "";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        sayingDisplayer.setText(result);
    }

    @Override
    public void loadImage(String imageName) {
        Picasso.with(mainActivity).load(IMAGES_DIR + imageName).into(imageView);
    }

    @Override
    public SayingRetriever loadSayingAndRefresh(String sayingPage) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        boolean alwaysUseFile = sharedPref.getBoolean(SettingsFragment.KEY_PREF_ALWAYS_USE_FILE, false);

        if (NetworkConnectivity.isNetworkAvailable(mainActivity) && !alwaysUseFile) {
            Log.d(TAG, "Loading saying from the internet");
            this.execute(sayingPage);
            return new WebSayingRetriever(mainActivity, imageView, sayingDisplayer);
        } else {
            return new FileSayingRetriever(mainActivity, imageView, sayingDisplayer).loadSayingAndRefresh(sayingPage);
        }
    }

    @Override
    public String loadSaying(String sayingPage) {
        // TODO Auto-generated method stub
        return new FileSayingRetriever(mainActivity, imageView, sayingDisplayer).loadSaying(sayingPage);
    }
}
