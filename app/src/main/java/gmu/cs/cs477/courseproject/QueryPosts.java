package gmu.cs.cs477.courseproject;

public class QueryPosts {
    double latitude;
    double longitude;
    String message;
    long timestamp;

    QueryPosts(){
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public QueryPosts(double latitude, double longitude, String message, long timestamp){
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
        this.timestamp = timestamp;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public String getMessage(){
        return message;
    }

    public long getTimestamp(){
        return timestamp;
    }
}
