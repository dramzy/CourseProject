package gmu.cs.cs477.courseproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final int maxAcceptableLocationAgeInMinutes = 10;

    public static boolean isInternetEnabled(@NonNull final Context c){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isGPSEnabled(@NonNull final Context c){
        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    public static  boolean isLoctionStale(@NonNull final Location location) {
        long howOldIsTooOld = TimeUnit.MINUTES.toMillis(maxAcceptableLocationAgeInMinutes);
        long howOld = Calendar.getInstance().getTimeInMillis() - location.getTime();
        return howOld > howOldIsTooOld;
    }
}
