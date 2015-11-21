package gmu.cs.cs477.courseproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends BaseAdapter implements ListAdapter {
    private final ArrayList<Post> posts;

    public PostAdapter(@NonNull final ArrayList<Post> posts) {
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return posts.get(position).getPost_ID();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.post_line, parent, false);
        }
        Post post = posts.get(position);
        final TextView postText = (TextView) view.findViewById(R.id.postText);
        final TextView postTime = (TextView) view.findViewById(R.id.postTime);
        postText.setText(post.getText());
        postTime.setText(DateUtils.getRelativeTimeSpanString(post.getTimestamp().getTime()));
        return view;
    }

    public String getPostText(int position){
        return posts.get(position).getText();
    }

    public Date getPostTime(int position){
        return posts.get(position).getTimestamp();
    }
}
