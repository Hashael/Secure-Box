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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Account extends Activity implements OnClickListener {
	private EditText editTextUsername;
	private EditText editTextPassword;
	private EditText editTextPasswordAgain;
	private DatabaseAdapter db;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account);
		editTextUsername = (EditText) findViewById(R.id.username);
		editTextPassword = (EditText) findViewById(R.id.password);
		editTextPasswordAgain = (EditText) findViewById(R.id.passwordAgain);
		Button buttonAccept = (Button) findViewById(R.id.acceptButton);
		buttonAccept.setOnClickListener(this);
		Button buttonCancel = (Button) findViewById(R.id.cancelButton);
		buttonCancel.setOnClickListener(this);
	}

	private void newAccount() {
		String name = editTextUsername.getText().toString();
		String pass1 = editTextPassword.getText().toString();
		String pass2 = editTextPasswordAgain.getText().toString();
		String passHashed = null;
		if (!pass1.equals("") && !name.equals("") && pass1.equals(pass2)) {
			try {
				passHashed = 
						es.uam.eps.tfg.securedpx.CipherActivity.getMD5(pass1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			db = new DatabaseAdapter(this);
			db.open();
			db.insertUser(name, passHashed);
			db.close();
			String newUser = new String(getString(R.string.new_user_added));
			Toast.makeText(Account.this, newUser,
					Toast.LENGTH_SHORT).show();
			finish();
		} else if (pass1.equals("") || pass2.equals("") || name.equals("")) {
			String missingData = new String(getString(R.string.missing_data));
			Toast.makeText(Account.this, missingData, Toast.LENGTH_SHORT)
					.show();
		} else if (!pass1.equals(pass2)) {
			String passwordMissmatch = new String(getString(R.string.password_missmatch));
			Toast.makeText(Account.this, passwordMissmatch,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.acceptButton:
			newAccount();
			break;
		case R.id.cancelButton:
			finish();
			break;
		}
	}
}
