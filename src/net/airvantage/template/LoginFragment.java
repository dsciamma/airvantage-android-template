package net.airvantage.template;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import net.airvantage.AirVantageClient;
import net.airvantage.model.User;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {

	private View view;
	private ImageView loginImg;
	private TextView loginTxt;
	private Button loginBt;
	private User currentUser = null;

	private AirVantageApplication getAirVantageApplication() {
		return (AirVantageApplication) getActivity().getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_login, container, false);

		loginImg = (ImageView) view.findViewById(R.id.login_img);
		loginTxt = (TextView) view.findViewById(R.id.login_txt);

		loginBt = (Button) view.findViewById(R.id.login_bt);
		loginBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (currentUser == null) {
					login();
				} else {
					logout();
				}
			}
		});

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (Constants.REQUEST_AUTHORIZATION): {
			if (resultCode == Activity.RESULT_OK) {
				String code = data.getStringExtra(Constants.AUTHORIZATION_CODE);

				final AirVantageClient client = getAirVantageApplication()
						.getAirVantageClient();

				AsyncTask<Void, Void, User> getUser = new AsyncTask<Void, Void, User>() {
					protected User doInBackground(Void... params) {
						try {
							if (client != null) {
								return client.getCurrentUser();
							}
						} catch (IOException e) {
							Log.e(LoginFragment.class.getName(),
									"Error when trying to get current user", e);
						}
						return null;
					}
				};

				try {
					getAirVantageApplication().initializeAccessToken(code);
					getUser.execute();
					updateUser(getUser.get());
				} catch (InterruptedException e) {
					Toast.makeText(view.getContext(),
							"Error during authentication", Toast.LENGTH_SHORT)
							.show();
				} catch (ExecutionException e) {
					Toast.makeText(view.getContext(),
							"Error during authentication", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;
		}
		}
	}

	private void login() {
		// Open authorization activity
		Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
		startActivityForResult(intent, Constants.REQUEST_AUTHORIZATION);
	}

	private void logout() {

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				try {
					getAirVantageApplication().getAirVantageClient().expire();
				} catch (IOException e) {
					Log.e(LoginFragment.class.getName(),
							"Error when trying to logout", e);
				}
				return null;
			}
		};

		task.execute();
		updateUser(null);
	}

	private void updateUser(User user) {
		currentUser = user;
		if (user == null) {
			// TODO Reset image
			loginTxt.setText(getString(R.string.login_required));
			loginBt.setText(getString(R.string.login));
		} else {

			if (user.picture != null) {
				Log.d(LoginFragment.class.getName(), "User picture: "
						+ user.picture.icon);
				// TODO Set image
			}
			loginTxt.setText(currentUser.name);
			loginBt.setText(getString(R.string.logout));
		}
	}
}
