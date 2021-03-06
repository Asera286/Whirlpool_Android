package edu.msu.elhazzat.wim.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.msu.elhazzat.wim.R;

/**
 * Created by christianwhite on 9/20/15.
 */
public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String PREF_FILE_NAME = "ACCOUNT_PREFS";
    public static final String PREF_FILE_ACCOUNT_NAME_KEY = "accountName";

    private static final int REQUEST_CODE_SIGN_IN = 0;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private SignInButton mSignInButton;

    /**
     * Main activity - sign in user with google auth.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        if(!mGoogleApiClient.isConnected()) {
            setContentView(R.layout.activity_main);
            mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
            mSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.sign_in_button) {
                        onSignInClicked();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Build google api client - use to authenticate via Plus Profile scope
     * and to acquire the signed in users email address
     */
    public void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }

    public void onSignInClicked() {
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    /**
     * Start home page activity on successful authentication
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        // ensure that email address is from whirlpool domain
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

        Pattern pattern = Pattern.compile("\\S+?@whirlpool\\.com");
        Matcher matcher = pattern.matcher(email);

        if (matcher.matches()) {
            Intent homeIntent = new Intent(this, HomeActivity.class);
            SharedPreferences myPrefs = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor e = myPrefs.edit();
            e.putString(PREF_FILE_ACCOUNT_NAME_KEY,  Plus.AccountApi.getAccountName(mGoogleApiClient));
            e.commit();
            startActivity(homeIntent);
        }
        else {
            Toast.makeText(this, "Whirlpool email required.", Toast.LENGTH_LONG).show();
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }
            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int code) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}