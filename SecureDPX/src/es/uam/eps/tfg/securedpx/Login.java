/*
 * Copyright 2013-14 Ignacio del Pozo Martínez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.uam.eps.tfg.securedpx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity implements OnClickListener {
	private DatabaseAdapter db;
	private EditText userNameEditText;
	private EditText passwordEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		Button loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);
		Button newUserButton = (Button) findViewById(R.id.newUserButton);
		newUserButton.setOnClickListener(this);
	}

	private void check() {
		String username = userNameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		try {
			// HASH time!
			password = 
				es.uam.eps.tfg.securedpx.CipherActivity.getMD5(password);
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("password", password);
			editor.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* TOAST Strings */
		String title = new String(getString(R.string.error));
		String message = new String(getString(R.string.login_failed));
		String button = new String(getString(R.string.try_again));

		db = new DatabaseAdapter(this);
		db.open();
		boolean firstIn = db.isRegistered(username, password);
		db.close();
		if (firstIn) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("firstplayername", username);
			editor.commit();
			startActivity(new Intent(this, DBRoulette.class));
			finish();
		} else {
			new AlertDialog.Builder(this)
					.setTitle(title)
					.setMessage(message)
					.setNeutralButton(button,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			check();
			break;
		case R.id.cancelButton:
			finish();
			break;
		case R.id.newUserButton:
			startActivity(new Intent(this, Account.class));
			break;
		}
	}
}
