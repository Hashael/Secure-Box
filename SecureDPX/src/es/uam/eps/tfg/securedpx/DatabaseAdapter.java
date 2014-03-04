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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAdapter {
	public static final String ID = "_id";
	public static final String NAME = "username";
	public static final String PASSWORD = "password";
	private static final String DATABASE_NAME = "securedpx.db";
	private static final String TABLE_NAME = "users";
	private static final int DATABASE_VERSION = 1;

	private final Context context;
	private DatabaseHelper helper;
	private SQLiteDatabase db;

	public DatabaseAdapter(Context context) {
		this.context = context;
		helper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			// TODO Auto-generated method stub
			createTable(arg0);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.w("DatabaseAdapter", "Upgrading database from version "
					+ oldVersion + " to version " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			createTable(db);
		}

		private void createTable(SQLiteDatabase db) {
			String str = "CREATE TABLE " + TABLE_NAME + " (" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
					+ " TEXT NOT NULL, " + PASSWORD + " TEXT NOT NULL);";
			try {
				db.execSQL(str);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public DatabaseAdapter open() throws SQLException {
		db = helper.getWritableDatabase();
		return this;
	}

	public void close() {
		helper.close();
	}

	public long insertUser(String username, String password) {
		ContentValues values = new ContentValues();
		values.put(NAME, username);
		values.put(PASSWORD, password);
		return db.insert(TABLE_NAME, null, values);
	}

	public boolean deleteUser(long id) {
		return db.delete(TABLE_NAME, ID + "=" + id, null) > 0;
	}

	public Cursor getAllUsers() {
		return db.query(TABLE_NAME, new String[] { ID, NAME, PASSWORD }, null,
				null, null, null, null);
	}

	public boolean isRegistered(String username, String password) {
		boolean in = false;
		Cursor cursor = db.query(TABLE_NAME, new String[] { NAME, PASSWORD },
				NAME + " = '" + username + "' AND " + PASSWORD + "= '"
						+ password + "'", null, null, null, NAME + " DESC");
		if (cursor.moveToFirst())
			in = true;
		if (!cursor.isClosed())
			cursor.close();
		return in;
	}

	public Context getContext() {
		return context;
	}
}
