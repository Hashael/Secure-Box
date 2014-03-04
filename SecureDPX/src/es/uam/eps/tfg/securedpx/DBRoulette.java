/*
 * Copyright (c) 2013-14 Ignacio del Pozo Martínez & 2010-11 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package es.uam.eps.tfg.securedpx;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dropbox.chooser.android.DbxChooser;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

@SuppressLint("SimpleDateFormat")
public class DBRoulette extends Activity {
    private static final String TAG = "DBRoulette";
    private static final int PICKFILE_RESULT_CODE = 1;
    static final int DBX_CHOOSER_REQUEST = 2;
    
    ///////////////////////////////////////////////////////////////////////////
    //                      Your app-specific settings.                      //
    ///////////////////////////////////////////////////////////////////////////

    // Replace this with your app key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    final static private String APP_KEY = "**********";
    final static private String APP_SECRET = "**********";
    final static private String CHOOSER_APP_KEY = "**********";
    
    // If you'd like to change the access type to the full Dropbox instead of
    // an app folder, change this value.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    ///////////////////////////////////////////////////////////////////////////
    //                      End app-specific settings.                       //
    ///////////////////////////////////////////////////////////////////////////

    // You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";


    DropboxAPI<AndroidAuthSession> mApi;

    private boolean mLoggedIn;

    // Android widgets
    private Button mSubmit;
    private LinearLayout mDisplay;
    private Button mUpload;
    private Button mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        // Basic Android widgets
        setContentView(R.layout.activity_dropbox);

        checkAppKeySetup();

        mSubmit = (Button)findViewById(R.id.auth_button);

        mSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // This logs you out if you're logged in, or vice versa
                if (mLoggedIn) {
                    logOut();
                } else {
                    // Start the remote authentication
                    mApi.getSession().startAuthentication(DBRoulette.this);
                }
            }
        });

        mDisplay = (LinearLayout)findViewById(R.id.logged_in_display);

        // This is the button to take a photo
        // THIS HAS TO THROW THE FILE PICKER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        mUpload = (Button)findViewById(R.id.photo_button);

        mUpload.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		Intent intent = new Intent(mUpload.getContext(), 
        				FilePickerActivity.class);
        		startActivityForResult(intent, PICKFILE_RESULT_CODE);
            }
        });


        // This is the button to throw the chooser
        final DbxChooser mChooser = new DbxChooser(CHOOSER_APP_KEY);
        
        mDownload = (Button)findViewById(R.id.roulette_button);
        mDownload.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mChooser.forResultType(DbxChooser.ResultType.FILE_CONTENT)
                .launch(DBRoulette.this, DBX_CHOOSER_REQUEST);
            }
        });

        // Display the proper UI state if logged in or not
        setLoggedIn(mApi.getSession().isLinked());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
            	String str = 
						new String(getString(R.string.error_auth));
                showToast(str + 
                		e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }

    // This is what gets called on finishing a media piece to import
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == RESULT_OK) {
    		
    		// Get the user's password
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			String pass = settings.getString("password", null);
			
			switch(requestCode) {
			case PICKFILE_RESULT_CODE:
				if(data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
					// Get the file path
					File f = new File(data.getStringExtra(
							FilePickerActivity.EXTRA_FILE_PATH));

					/*******************************/
					/*** 		ENCRYPT MODE 	 ***/
					/*******************************/
					File encodedFile = CipherActivity.encondeFile(f, pass);
					if (encodedFile == null){
						String str = 
								new String(getString(R.string.error_enc));
						Toast.makeText(getApplicationContext(),
								str,
								Toast.LENGTH_SHORT).show();
					} else {
						String str = 
								new String(getString(R.string.file_encrypted));
						Toast.makeText(getApplicationContext(),
								str,
								Toast.LENGTH_SHORT).show();
						
						Calendar cal = Calendar.getInstance();
						cal.getTime();
						SimpleDateFormat sdf = 
								new SimpleDateFormat("MM_dd_hh:mm");

						String path = "/SecureDPX/" + sdf.format(cal.getTime()) 
																		+ "/";
						
					    UploadFileDropbox upload = new UploadFileDropbox(this, 
					    		mApi, path, encodedFile);
					    upload.execute();
					}
				}
				break;
			case DBX_CHOOSER_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
	                DbxChooser.Result result = new DbxChooser.Result(data);
	                Log.d("main", "Link to selected file: " + result.getLink());

	                // Get the file path
	                File f2 = new File(result.getLink().getEncodedPath());
	                
	                /*******************************/
					/*** 		DECRYPT MODE 	 ***/
					/*******************************/
					int decodeResult = CipherActivity.decodeFile(f2, pass);
					if (decodeResult == 0){
						String str = 
								new String(getString(
										R.string.file_decrypted_downloaded));
						Toast.makeText(getApplicationContext(),
								str,
								Toast.LENGTH_SHORT).show();
						
						String docPath = Environment.
								getExternalStorageDirectory().getPath() +
								File.separator + "Download" + File.separator +
			                    result.getName();
						openDocument(docPath);
					} else {
						String str = 
								new String(getString(R.string.error_dec));
						Toast.makeText(getApplicationContext(),
								str,
								Toast.LENGTH_SHORT).show();
					}
	            } else {
	                // Failed or was cancelled by the user.
	            	Log.e("ERROR", "Failed or cancelled by the user");
	            }
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
			}
		}
    }
    
    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    	if (loggedIn) {
    		mSubmit.setText("Unlink from Dropbox");
            mDisplay.setVisibility(View.VISIBLE);
    	} else {
    		mSubmit.setText("Link with Dropbox");
            mDisplay.setVisibility(View.GONE);
    	}
    }

    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            Log.w("error", "You must apply for an app key and secret from " +
            		"developers.dropbox.com, and add them to the DBRoulette" +
            		"app before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            Log.w("error", "URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a
     * local store, rather than storing user name & password, and
     * re-authenticating each time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a
     * local store, rather than storing user name & password, and
     * re-authenticating each time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0],
            		stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
            		accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    public void openDocument(String documentName) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        File file = new File(documentName);
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl
        		(Uri.fromFile(file).toString());
        String mimetype = android.webkit.MimeTypeMap.getSingleton().
        		getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null)
        {
        	// if there is no extension or there is no definite mimetype, 
        	// still try to open the file
            intent.setDataAndType(Uri.fromFile(file), "text/*");
        }
        else
        {
            intent.setDataAndType(Uri.fromFile(file), mimetype);            
        }
        // custom message for the intent
        startActivity(Intent.createChooser(intent, "Choose an Application:"));
    }
}
