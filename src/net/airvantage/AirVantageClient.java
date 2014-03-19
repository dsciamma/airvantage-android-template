package net.airvantage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.airvantage.model.AccessToken;
import net.airvantage.model.User;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

public class AirVantageClient {

	private static final String APIS = "/api/v1";

	private final String access_token;

	private final String server;

	public static String buildAuthorizationURL(String server, String clientId) {
		return "https://" + server + "/api/oauth/authorize?client_id="
				+ clientId
				+ "&response_type=code&redirect_uri=oauth://airvantage";
	}

	public static String buildImplicitFlowURL(String server, String clientId) {

		return "https://" + server + "/api/oauth/authorize?client_id="
				+ clientId
				+ "&response_type=token&redirect_uri=oauth://airvantage";
	}

	private String buildEndpoint(String api) {
		return "https://" + server + APIS + api + "?access_token="
				+ access_token;
	}

	public AirVantageClient(String server, String token) {
		this.server = server;
		this.access_token = token;
	}

	public User getCurrentUser() throws IOException {
		OkHttpClient client = new OkHttpClient();

		// Create request for remote resource.
		HttpURLConnection connection = client.open(new URL(
				buildEndpoint("/users/current")));
		InputStream is = connection.getInputStream();
		Log.d(AirVantageClient.class.getName(), "User URL: "
				+ buildEndpoint("/users/current"));

		InputStreamReader isr = new InputStreamReader(is);

		// Deserialize HTTP response to concrete type.
		Gson gson = new Gson();
		return gson.fromJson(isr, User.class);
	}

	public static AccessToken login(final String server, final String clientId,
			final String clientSecret, final String code) throws IOException {
		OkHttpClient client = new OkHttpClient();

		// Create request for remote resource.
		HttpURLConnection connection = client.open(new URL("https://" + server
				+ "/api/oauth/token?grant_type=authorization_code&code=" + code
				+ "&client_id=" + clientId + "&client_secret=" + clientSecret
				+ "&redirect_uri=oauth://airvantage"));
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);

		// Deserialize HTTP response to concrete type.
		Gson gson = new Gson();
		return gson.fromJson(isr, AccessToken.class);
	}

	public static void expire(final String server, String token)
			throws IOException {
		InputStream is = null;
		try {
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(server
					+ "/api/oauth/expire?access_token=" + token));
			is = connection.getInputStream();
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public static AccessToken refresh(final String server,
			final String clientId, final String clientSecret,
			final String refreshToken) throws IOException {
		OkHttpClient client = new OkHttpClient();

		// Create request for remote resource.
		HttpURLConnection connection = client.open(new URL("https://" + server
				+ "/api/oauth/token?grant_type=refresh_token&refresh_token="
				+ refreshToken + "&client_id=" + clientId + "&client_secret="
				+ clientSecret));
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);

		// Deserialize HTTP response to concrete type.
		Gson gson = new Gson();
		return gson.fromJson(isr, AccessToken.class);
	}

	public void expire() throws IOException {
		expire(server, access_token);
	}
}
