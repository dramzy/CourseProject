package gmu.cs.cs477.courseproject;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import gmu.cs.cs477.courseproject.R;

import static gmu.cs.cs477.courseproject.Constants.*;

// Displays a single post
public class ViewPostActivity extends AppCompatActivity {

    TextView postDetails;
    TextView postTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Intent intent = getIntent();
        // Get UI elements
        postDetails = (TextView) findViewById(R.id.post_details);
        postTime = (TextView) findViewById(R.id.post_time);
        postDetails.setText(intent.getStringExtra(POST_TEXT));
        postTime.setText(intent.getStringExtra(POST_TIME ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
