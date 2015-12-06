package gmu.cs.cs477.courseproject;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;

// Application state; stores a reference to firebase and geofire
public class AppState extends Application{
    private static final String postsUrl = "https://fiery-fire-1976.firebaseio.com/Posts";
    private static final String locationsUrl = "https://fiery-fire-1976.firebaseio.com/PostLocations";
    private Firebase firebaseRef;
    private GeoFire geofireRef;

    @Override
    public void onCreate(){
        super.onCreate();
        //Instantiate the firebase and geofire references
        Firebase.setAndroidContext(getApplicationContext());
        firebaseRef = new Firebase(postsUrl);
        geofireRef  = new GeoFire(new Firebase(locationsUrl));
    }

    public Firebase getFireBaseRef(){
        return this.firebaseRef;
    }

    public GeoFire getGeoFireRef(){
        return this.geofireRef;
    }

}
