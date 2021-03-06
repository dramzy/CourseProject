package gmu.cs.cs477.courseproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Utils {
    // Maximum acceptable location age in minutes
    private static final int maxLocationAgeInMinutes = 10;
    // Maximum acceptable post age in hours
    private static final int maxPostAgeInHours = 24;

    // Check for internet connection
    public static boolean isInternetEnabled(@NonNull final Context c){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Check for GPS
    public static boolean isGPSEnabled(@NonNull final Context c){
        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Check if location is too old
    public static  boolean isLoctionStale(@NonNull final Location location) {
        long howOldIsTooOld = maxLocationAgeInMinutes * DateUtils.MINUTE_IN_MILLIS;
        long howOld = Calendar.getInstance().getTimeInMillis() - location.getTime();
        return howOld > howOldIsTooOld;
    }

    // Check if post is too old
    public static boolean isPostStale(@NonNull final QueryPosts post){
        long howOldIsTooOld = maxPostAgeInHours * DateUtils.HOUR_IN_MILLIS;
        long howOld = Calendar.getInstance().getTimeInMillis() - post.getTimestamp();
        return howOld > howOldIsTooOld;
    }
}
