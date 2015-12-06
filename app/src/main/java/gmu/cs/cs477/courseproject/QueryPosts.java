package gmu.cs.cs477.courseproject;

public class QueryPosts {
    String message;
    long timestamp;

    QueryPosts(){
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    // Used to store and retieve posts in firebase
    public QueryPosts( String message, long timestamp){
        this.message = message;
        this.timestamp = timestamp;
    }


    public String getMessage(){
        return message;
    }

    public long getTimestamp(){
        return timestamp;
    }
}
