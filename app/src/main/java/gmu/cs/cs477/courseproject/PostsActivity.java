package gmu.cs.cs477.courseproject;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static gmu.cs.cs477.courseproject.Constants.*;
import static gmu.cs.cs477.courseproject.Utils.isLoctionStale;
import static gmu.cs.cs477.courseproject.Utils.isPostStale;

public class PostsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private SwipeRefreshLayout refreshLayout;
    private ListView postsList;
    private PostAdapter adapter;
    private FloatingActionButton fab;
    private Location lastLocation;
    private GoogleApiClient client;
    private ArrayList<Post> posts;
    private ArrayList<String> locationKeys;
    private Firebase firebaseRef;
    private GeoFire geofireRef;
    private final int postRangeInMiles = 5;
    private final double postRangeInKm = postRangeInMiles * 1.60934;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        postsList = (ListView) findViewById(R.id.postsList);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(Color.rgb(135, 206, 250), Color.rgb(135, 206, 235), Color.rgb(0, 191, 255));
        fab = (FloatingActionButton) findViewById(R.id.actionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostsActivity.this, CreatePostActivity.class);
                intent.putExtra(LOCATION, lastLocation);
                startActivityForResult(intent, 0);
            }
        });
        postsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PostsActivity.this, ViewPostActivity.class);
                intent.putExtra(POST_TEXT, adapter.getPostText(position));
                intent.putExtra(POST_TIME, DateUtils.getRelativeTimeSpanString(adapter.getPostTime(position)));
                startActivity(intent);
            }
        });
        lastLocation = getIntent().getParcelableExtra(LOCATION);
        final AppState state = (AppState) getApplication();
        firebaseRef = state.getFireBaseRef();
        geofireRef = state.getGeoFireRef();
        autoRefresh();
    }

    private void autoRefresh() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                loadData();
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && data != null && data.hasExtra(NEW_POST))
            autoRefresh();
    }

    private void loadData() {
        if (Utils.isGPSEnabled(this)) {
            if (lastLocation == null || isLoctionStale(lastLocation)) {
                connectToGoogleAPI();
            } else {
                getPosts();
            }
        } else {
            refreshLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT).show();
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
            getPosts();
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
        getPosts();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void getPosts() {
        if (!Utils.isInternetEnabled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        } else {
            locationKeys = new ArrayList<>();
            posts = new ArrayList<>();
            final GeoQuery query = geofireRef.queryAtLocation(
                    new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), postRangeInKm);
            query.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    locationKeys.add(key);
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    query.removeGeoQueryEventListener(this);
                    if (locationKeys.size() == 0) {
                        refreshLayout.setRefreshing(false);
                    }
                    // TODO: Refactor; too much work on the UI thread
                    for (final String key : locationKeys) {
                        firebaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                QueryPosts post = dataSnapshot.getValue(QueryPosts.class);
                                if (isPostStale(post)) {
                                    firebaseRef.child(key).removeValue();
                                    geofireRef.removeLocation(key);
                                } else {
                                    posts.add(new Post(key, post.getMessage(), post.getTimestamp()));
                                }
                                locationKeys.remove(key);
                                if (locationKeys.size() == 0) {
                                    onSuccess();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                onError(firebaseError);
                            }
                        });
                    }
                }

                @Override
                public void onGeoQueryError(FirebaseError error) {
                    onError(error);
                }
            });
        }
    }

    private void onSuccess() {
        Collections.sort(posts, new PostComparator());
        adapter = new PostAdapter(posts);
        postsList.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }

    private void onError(FirebaseError error) {
        Log.e("firebase_error", "Could not retreive firebase posts: " + error.getMessage());
        refreshLayout.setRefreshing(false);
        Toast.makeText(getApplicationContext(), "Could not retrieve posts", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
