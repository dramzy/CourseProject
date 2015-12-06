package gmu.cs.cs477.courseproject;

import android.support.annotation.NonNull;

import java.util.Date;

// Represents a post for display purposes
public class Post {
    private String post_ID;
    private String text;
    private long timestamp;

    public Post(final String post_ID, @NonNull final String text, @NonNull final long timestamp) {
        this.post_ID = post_ID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull final long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPost_ID() {
        return post_ID;
    }

    public void setPost_ID(final String post_ID) {
        this.post_ID = post_ID;
    }

    public String getText() {
        return text;
    }

    public void setText(@NonNull final String text) {
        this.text = text;
    }

}
