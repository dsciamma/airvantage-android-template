package net.airvantage.template;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import net.airvantage.AirVantageClient;
import net.airvantage.model.AccessToken;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class AirVantageApplication extends Application {
	private AirVantageClient client;

	private SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	public String getClientSecret() {
		return getString(R.string.airvantage_client_secret);
	}

	public String getClientId() {
		return getString(R.string.airvantage_client_id);
	}

	public String getServer() {
		return getString(R.string.airvantage_server);
	}

	public String getAccessToken() {
		return prefs.getString(Constants.ACCESS_TOKEN_STORAGE, null);
	}

	public String getRefreshToken() {
		return prefs.getString(Constants.REFRESH_TOKEN_STORAGE, null);
	}

	public AirVantageClient getAirVantageClient() {
		if (client == null && getAccessToken() != null
				&& getRefreshToken() != null) {
			try {
				client = initializeAirVantageClient(getAccessToken(),
						getRefreshToken());
			} catch (InterruptedException e) {
				Log.e(AirVantageApplication.class.getName(),
						"Error when initializing AirVantage client", e);
			} catch (ExecutionException e) {
				Log.e(AirVantageApplication.class.getName(),
						"Error when initializing AirVantage client", e);
			}
		}
		return client;
	}

	private AirVantageClient initializeAirVantageClient(
			final String accessToken, final String refreshToken)
			throws InterruptedException, ExecutionException {

		AsyncTask<String, Void, AirVantageClient> task = new AsyncTask<String, Void, AirVantageClient>() {
			protected AirVantageClient doInBackground(String... params) {

				AirVantageClient client = new AirVantageClient(getServer(),
						accessToken);
				// Get the current user to check the token is valid
				try {
					client.getCurrentUser();
				} catch (IOException e) {
					// If not valid, regenerate an access token
					try {
						AccessToken token = AirVantageClient.refresh(
								getServer(), getClientId(), getClientSecret(),
								refreshToken);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString(Constants.ACCESS_TOKEN_STORAGE,
								token.access_token);
						editor.putString(Constants.REFRESH_TOKEN_STORAGE,
								token.refresh_token);
						editor.commit();
						client = new AirVantageClient(getServer(), accessToken);
					} catch (IOException e1) {
						throw new RuntimeException(
								"Error when initializing AirVantage client", e1);
					}
				}
				return client;
			}
		};

		task.execute();
		return task.get();
	}

	public void initializeAccessToken(String code) throws InterruptedException,
			ExecutionException {
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
			protected AccessToken doInBackground(String... params) {
				try {
					return AirVantageClient.login(getServer(), getClientId(),
							getClientSecret(), params[0]);
				} catch (IOException e) {
					throw new RuntimeException(
							"Error when trying to get token", e);
				}
			}
		};

		task.execute(code);
		AccessToken token = task.get();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.ACCESS_TOKEN_STORAGE, token.access_token);
		editor.putString(Constants.REFRESH_TOKEN_STORAGE, token.refresh_token);
		editor.commit();
	}
}
