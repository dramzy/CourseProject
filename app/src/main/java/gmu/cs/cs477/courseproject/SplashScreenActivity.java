package gmu.cs.cs477.courseproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static gmu.cs.cs477.courseproject.Utils.isLoctionStale;
import static gmu.cs.cs477.courseproject.Constants.*;

// Splash screen used to mask the initial delay of getting a fix on the user's location
public class SplashScreenActivity extends AppCompatActivity implements LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    boolean enablingGPSManually = false;
    private GoogleApiClient client;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the splash screen animation
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);
        ImageView iv = (ImageView) findViewById(R.id.loadingAnimation);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        checkGPS();
    }

    // Connect to the Google API's location services
    protected synchronized void connectToGoogleAPI() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }


    private void checkGPS() {
        if (Utils.isGPSEnabled(this)) {
            connectToGoogleAPI();
        } else {
            // If GPS is disabled, ask user to enable it and
            // take them to the appropriate settings page
            new AlertDialog.Builder(this)
                    .setMessage("GPS is disabled. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            enablingGPSManually = true;
                            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Utils.isGPSEnabled(SplashScreenActivity.this)) {
                                // If the user enabled GPS themselves, get the location
                                checkGPS();
                            } else {
                                // Otherwise, give up and go to main activity
                                leave();
                            }
                        }
                    }).show();
        }
    }

    private void leave() {
        // Go to main activity
        Intent intent = new Intent(this, PostsActivity.class);
        intent.putExtra(LOCATION, lastLocation);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // If the user chose to enable GPS, check for GPS again
        if (enablingGPSManually) {
            checkGPS();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onConnected(Bundle bundle) {
        // If current location is stale listen for location updates
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (lastLocation == null || isLoctionStale(lastLocation)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, getRequest(), this);
        } else {
            leave();
        }

    }

    protected LocationRequest getRequest() {
        // Create the Google API location request
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        // Once location fix is obtained, remove listener and stop the animation
        leave();
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

}
