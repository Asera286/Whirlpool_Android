package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

/**
 * Created by christianwhite on 11/12/15.
 */
public class GoogleApiBaseClient extends AppCompatActivity implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = GoogleApiBaseClient.class.getSimpleName();

    private static final int REQUEST_CODE_SIGN_IN = 0;

    protected GoogleApiClient mGoogleApiClient;
    protected boolean mIsResolving = false;
    protected boolean mShouldResolve = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
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

    public void signIn() {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void signOut() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Start home page activity on successful authentication
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.putExtra("accountName", Plus.AccountApi.getAccountName(mGoogleApiClient));
        startActivity(homeIntent);
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
}
