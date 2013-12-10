package com.waldm.proverbica;

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

public class RetrieveSayingFromWeb extends RetrieveSaying {

	public RetrieveSayingFromWeb(MainActivity mainActivity) {
		super(mainActivity);
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
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
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
}
