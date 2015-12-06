package gmu.cs.cs477.courseproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static gmu.cs.cs477.courseproject.Constants.LOCATION;

import java.util.Comparator;
import java.util.Date;

import static gmu.cs.cs477.courseproject.Constants.NEW_POST;
import static gmu.cs.cs477.courseproject.Utils.isLoctionStale;
// Allows user to compose a post
public class CreatePostActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Activity components and state
    private EditText post;
    private TextView counter;
    private Button postButton;
    private RelativeLayout input_wrapper;
    private boolean posting = false;
    private String postText;
    private GoogleApiClient client;
    private Location lastLocation;
    private AppState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        //Get the UI elements
        post = (EditText) findViewById(R.id.post_details);
        counter = (TextView) findViewById(R.id.chars_left);
        postButton = (Button) findViewById(R.id.post_button);
        input_wrapper = (RelativeLayout) findViewById(R.id.input_wrapper);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Keep track of how many characters the user has left and display the
        // number beneath the edittext field
        post.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                counter.setText(200 - post.getText().length() + " left");
            }
        });
        // Create post on post button click if input is
        // not empty
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postText = post.getText().toString();
                if (!postText.equals("")) {
                    postText = postText.replace('\n', ' ');
                    checkGPS();
                }
            }
        });
        // The edittext field should gain focus once the activity is created
        post.requestFocus();
        // Bring up the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        // Bring up keyboard whenever the editext wrapper is clicked on
        input_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });
        // Get last known location and app state
        lastLocation = getIntent().getParcelableExtra(LOCATION);
        state = (AppState) getApplication();
    }

    @Override
    public void onBackPressed() {
        // Confirm that user wants to discard post if post is not
        // empty
        if (!post.getText().toString().equals("") && !posting) {
            new AlertDialog.Builder(CreatePostActivity.this)
                    .setMessage("Do you want to discard post?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CreatePostActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back to main activity when user hits actioon bar
            // back button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkGPS() {
        // Check that GPS and internet are enabled, if they are, listen for location
        // and go back to main activity
        if (!Utils.isGPSEnabled(this)) {
            Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT).show();
        } else if (!Utils.isInternetEnabled(this)) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        } else {
            connectToGoogleAPI();
            sendPostBack();
        }
    }

    private void sendPostBack() {
        // Back to main activity and pass back the fact that a new
        // post is being created
        hideKeyboard();
        Intent intent = new Intent();
        intent.putExtra(NEW_POST, true);
        setResult(0, intent);
        finish();
    }

    public void showKeyboard() {
        // Show the soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(input_wrapper.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void hideKeyboard() {
        // Hide keyboard
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // Connects to the Google API's location service
    protected synchronized void connectToGoogleAPI() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // If current location is stale listen for location updates
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);
        lastLocation = (location == null) ? lastLocation : location;
        if (lastLocation == null || isLoctionStale(lastLocation)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, getRequest(), this);
        } else {
            createPost();
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Once location update is received remove listener and create post
        lastLocation = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        createPost();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void createPost() {
        // Publish the post to Firebase and if that succeeds, store the post's location
        // If location storage fails, remove post
        final Firebase newPostRef = state.getFireBaseRef().push();
        QueryPosts newPost = new QueryPosts(postText, new Date().getTime());
        newPostRef.setValue(newPost, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, final Firebase firebase) {
                if (firebaseError == null) {
                    String key = newPostRef.getKey();
                    state.getGeoFireRef().setLocation(key, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, FirebaseError error) {
                                    if (error == null) {
                                        Toast.makeText(getApplicationContext(), "Post created", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e("firebase_error", "Could not create post: " + error.getMessage());
                                        Toast.makeText(getApplicationContext(), "Failed to create post", Toast.LENGTH_SHORT).show();
                                        firebase.child(key).removeValue();
                                    }
                                }
                            });
                } else {
                    Log.e("firebase_error", "Could not create post: " + firebaseError.getMessage());
                }
            }
        });
    }
}
