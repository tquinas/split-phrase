package com.waldm.proverbica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FileSayingRetriever implements SayingRetriever {

	private static final String TAG = FileSayingRetriever.class.getSimpleName();
	private final String filename = "sayings.txt";
	private List<String> sayings;
	private final MainActivity mainActivity;
	private final ImageView imageView;

	public FileSayingRetriever(MainActivity mainActivity, ImageView imageView) {
		this.mainActivity = mainActivity;
		this.imageView = imageView;
	}

	@Override
	public void loadImage(String imageName) {
		Picasso.with(mainActivity)
				.load(mainActivity.getResources().getIdentifier(
						imageName.replace(".jpg", ""), "drawable",
						mainActivity.getPackageName())).into(imageView);
	}

	@Override
	public SayingRetriever loadSayingAndRefresh(String sayingPage) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		boolean alwaysUseFile = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_ALWAYS_USE_FILE, false);

		if (NetworkConnectivity.isNetworkAvailable(mainActivity)
				&& !alwaysUseFile) {
			return new WebSayingRetriever(mainActivity, imageView)
					.loadSayingAndRefresh(sayingPage);
		} else {
			Log.d(TAG, "Loading saying from file");
			if (sayings == null) {
				sayings = new ArrayList<String>();

				InputStream stream = null;
				try {
					stream = mainActivity.getAssets().open(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream));
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
			String beginning = sayings.get(random.nextInt(sayings.size()))
					.split("\\|")[0];
			String end = sayings.get(random.nextInt(sayings.size())).split(
					"\\|")[1];

			mainActivity.setText(beginning + " " + end);
			return this;
		}
	}
}