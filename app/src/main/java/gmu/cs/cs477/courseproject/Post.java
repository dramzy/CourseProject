package gmu.cs.cs477.courseproject;

import android.support.annotation.NonNull;

import java.util.Date;

public class Post {
    private long post_ID;
    private String text;
    private Date timestamp;

    public Post(final long post_ID, @NonNull final String text, @NonNull final Date timestamp) {
        this.post_ID = post_ID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull final Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getPost_ID() {
        return post_ID;
    }

    public void setPost_ID(final long post_ID) {
        this.post_ID = post_ID;
    }

    public String getText() {
        return text;
    }

    public void setText(@NonNull final String text) {
        this.text = text;
    }

}
