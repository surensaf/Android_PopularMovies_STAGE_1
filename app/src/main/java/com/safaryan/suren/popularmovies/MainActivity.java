package com.safaryan.suren.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GridView mGridView;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private ProgressBar mProgressBar;
    static int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.
                        putExtra("title", item.getTitle()).
                        putExtra("release_date", item.getRelease_date()).
                        putExtra("overview", item.getOverview()).
                        putExtra("vote_average", item.getVote_average()).
                        putExtra("image", item.getImage()).
                        putExtra("backdrop_path", item.getBackdrop_path());

                //Start details activity
                startActivity(intent);
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", mGridData);
        super.onSaveInstanceState(outState);
    }
    private void updateMovies() {
        mGridData.clear();
        //mGridAdapter.clear();

        CollectMoviesTask collectMovies = new CollectMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String sort = prefs.getString(getString(R.string.pref_sort_key), "popularity.desc");

        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                String pageNum = "1";
                pageNum = Integer.toString(page);
                new CollectMoviesTask().execute(sort, pageNum);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        collectMovies.execute(sort, "1");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onResume(){

        mGridView.setSelection(index);
        super.onResume();
    }

    @Override
    public void onPause(){
        index = mGridView.getFirstVisiblePosition();
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Downloading data asynchronously
    public class CollectMoviesTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = CollectMoviesTask.class.getSimpleName();

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 1;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null;

            String api_key = getString(R.string.api_key);
            try {
                final String BASE_URL = getResources().getString(R.string.base_url);
                final String API_KEY = "api_key";
                final String SORT_BY = "sort_by";
                final String PAGE = "page";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, params[0])
                        .appendQueryParameter(API_KEY, api_key)
                        .appendQueryParameter(PAGE, params[1])
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return 0;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return 0;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return 0;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return 0;
            }
            return result;
}

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void getDataFromJson(String moviesJsonStr) throws JSONException {

        final String OWM_RESULTS = "results";
        final String OWM_POSTER = "poster_path";
        final String OWM_TITLE = "original_title";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_OVERVIEW = "overview";
        final String OWM_IMAGE_ROOT = getResources().getString(R.string.poster_url);
        final String OWM_BACK_IMAGE_ROOT = getResources().getString(R.string.cover_photo_url);
        final String OWM_BACK_IMAGE = "backdrop_path";
        final String NO_BACK_IMAGE = "NO_PHOTO";
        JSONObject response = new JSONObject(moviesJsonStr);
        JSONArray posts = response.optJSONArray(OWM_RESULTS);
        GridItem item;
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.optJSONObject(i);
            String title = post.optString(OWM_TITLE);
            item = new GridItem();
            String xxx = post.optString(OWM_BACK_IMAGE);
            if(!xxx.equals(null) && !xxx.equals("null")) {
                item.setBackdrop_path(OWM_BACK_IMAGE_ROOT + post.optString(OWM_BACK_IMAGE));
            } else
            {
                item.setBackdrop_path(NO_BACK_IMAGE);
            }
            item.setTitle(title);
            item.setImage(OWM_IMAGE_ROOT + post.optString(OWM_POSTER));
            item.setOverview(post.getString(OWM_OVERVIEW));
            item.setRelease_date(post.getString(OWM_RELEASE_DATE));
            item.setVote_average(post.getString(OWM_VOTE_AVERAGE));
            mGridData.add(item);
        }
    }
}