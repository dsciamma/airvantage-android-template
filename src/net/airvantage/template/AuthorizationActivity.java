package net.airvantage.template;

import net.airvantage.AirVantageClient;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthorizationActivity extends Activity {

	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authorization);

		webview = (WebView) findViewById(R.id.authorization_webview);
		webview.getSettings().setJavaScriptEnabled(true);
		// attach WebViewClient to intercept the callback url
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				// check for our custom callback protocol otherwise use default
				// behavior
				if (url.startsWith("oauth")) {

					Log.d(AuthorizationActivity.class.getName(),
							"Callback URL: " + url);
					Uri uri = Uri.parse(url);
					String code = uri.getQueryParameter("code");

					Log.d(AuthorizationActivity.class.getName(), "OAuth code: "
							+ code);

					// host airvantage detected from callback
					// oauth://airvantage
					if (uri.getHost().equals("airvantage")) {
						sendAuthorizationCode(code);
					}

					return true;
				}

				return super.shouldOverrideUrlLoading(view, url);
			}
		});

		webview.loadUrl(AirVantageClient.buildAuthorizationURL(
				((AirVantageApplication) getApplication()).getServer(),
				((AirVantageApplication) getApplication()).getClientId()));
	}

	private void sendAuthorizationCode(String code) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(Constants.AUTHORIZATION_CODE, code);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
