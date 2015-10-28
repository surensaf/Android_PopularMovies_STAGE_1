package com.safaryan.suren.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {
    private static final int ANIM_DURATION = 600;
    private TextView titleTextView;
    private ImageView imageViewPoster;
    private ImageView backImage;
    private TextView releaseDateView;
    private TextView voteAverageView;
    private TextView overviewView;
    private ImageView backButton;
    final String NO_BACK_IMAGE = "NO_PHOTO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting details screen layout
        setContentView(R.layout.activity_details_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //retrieves the thumbnail data
        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("title");
        String image = bundle.getString("image");
        String backdrop_path = bundle.getString("backdrop_path");
        String release_date = bundle.getString("release_date");
        String vote_average = bundle.getString("vote_average");
        String overview = bundle.getString("overview");

        titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(Html.fromHtml(title));

        releaseDateView = (TextView) findViewById(R.id.release_date);
        releaseDateView.setText(Html.fromHtml(release_date));

        voteAverageView = (TextView) findViewById(R.id.vote_average);
        voteAverageView.setText(Html.fromHtml(vote_average));

        overviewView = (TextView) findViewById(R.id.overview);
        overviewView.setText(Html.fromHtml(overview));

        //Set image url
        imageViewPoster = (ImageView) findViewById(R.id.grid_item_image);
        Picasso.with(this).load(image).into(imageViewPoster);

        if (!backdrop_path.equals(NO_BACK_IMAGE)) {

            backImage = (ImageView) findViewById(R.id.grid_item_back_image);
            Picasso.with(this).load(backdrop_path).into(backImage);
        }

        backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
