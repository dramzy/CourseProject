package gmu.cs.cs477.courseproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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

import static gmu.cs.cs477.courseproject.Utils.isLoctionStale;

public class CreatePostActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
        post = (EditText) findViewById(R.id.post_details);
        counter = (TextView) findViewById(R.id.chars_left);
        postButton = (Button) findViewById(R.id.post_button);
        input_wrapper = (RelativeLayout) findViewById(R.id.input_wrapper);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        post.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        input_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }
        });
        lastLocation = getIntent().getParcelableExtra(LOCATION);
        state = (AppState) getApplication();
    }

    @Override
    public void onBackPressed() {
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
            case android.R.id.home:
                if (!post.getText().toString().equals("")) {
                    new AlertDialog.Builder(CreatePostActivity.this)
                            .setMessage("Do you want to discard post?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    NavUtils.navigateUpFromSameTask(CreatePostActivity.this);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkGPS() {
        if (!Utils.isGPSEnabled(this)) {
            Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT).show();
        } else if (!Utils.isInternetEnabled(this)) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        } else {
            connectToGoogleAPI();
            posting = true;
            onBackPressed();
        }
    }

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
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);
        lastLocation = (location == null) ? lastLocation : location;
        if (lastLocation == null || isLoctionStale(lastLocation)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, getRequest(), this);
        } else {
            createPost();
        }
    }

    protected LocationRequest getRequest() {
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
        lastLocation = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        createPost();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void createPost() {
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
