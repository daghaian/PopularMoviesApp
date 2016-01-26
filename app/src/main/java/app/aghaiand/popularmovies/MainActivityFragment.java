package app.aghaiand.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    public View rootView;
    public static String[] moviePosters;
    ImageListAdapter adapter;



    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)

    {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        updatePosters();
        GridView gv = (GridView) rootView.findViewById(R.id.listview_movies);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {

                Log.d("MainActivityFragment","Position of View: " + i + "\tRow ID of Item Clicked: " + l);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updatePosters()
    {
        FetchMovieData myTask = new FetchMovieData();
        myTask.execute();
    }

    public class FetchMovieData extends AsyncTask<Void, Void, String[]> {



        @Override
        protected String[] doInBackground(Void... params) {
            final String SCHEME = "http";
            final String AUTHORITY = "api.themoviedb.org";
            final String PATH = "3/discover/movie";
            final String SORT_QUERY = "sort_by";
            final String API_KEY_QUERY = "api_key";

            Uri.Builder myBuilder = new Uri.Builder();
            myBuilder.scheme(SCHEME);
            myBuilder.authority(AUTHORITY);
            myBuilder.path(PATH);
            myBuilder.appendQueryParameter(SORT_QUERY, "popularity.desc");
            myBuilder.appendQueryParameter(API_KEY_QUERY, "14cb79ed3293203b59845c42a0c3b309");
            myBuilder.build();
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJSONstr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(myBuilder.toString());
                Log.d("FetchMovieData","URL is " + url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    movieJSONstr = null;
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
                    movieJSONstr = null;
                }
                movieJSONstr = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                movieJSONstr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                Log.d("GetMovieData","JSON Object is " + movieJSONstr);
                return getMovieDataFromJSON(movieJSONstr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Keep in the mind that onPostExecute is what method is run in order to merge the
        //background thread back to the Main UI thread

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings != null) {
                adapter = new ImageListAdapter(getActivity(),strings);
                GridView lv = (GridView) rootView.findViewById(R.id.listview_movies);
                lv.setAdapter(adapter);
            }
            else
            {
                Log.d("onPostExecute","Strings are NULL!!!");
            }
        }
//
//        /* The date/time conversion code is going to be moved outside the asynctask later,
//                 * so for convenience we're breaking it out into its own method now.
//                */
//        private String getReadableDateString(long time) {
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//            String highLowStr = "";
//
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String units = preferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
//            Log.d("ForecastFragment", "Value for Units is " + units);
//
//            if (units.equals("metric")) {
//                highLowStr = roundedHigh + "/" + roundedLow;
//            } else if (units.equals("imperial")) {
//                roundedHigh = (roundedHigh * 9 / 5) + 32;
//                roundedLow = (roundedLow * 9 / 5) + 32;
//                highLowStr = roundedHigh + "/" + roundedLow;
//            }
//
//            return highLowStr;
//        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMovieDataFromJSON(String movieJSONstr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String RESULTS = "results";
            final String POSTER_PATH = "poster_path";

            JSONObject movieJSON = new JSONObject(movieJSONstr);
            JSONArray movieArray = movieJSON.getJSONArray(RESULTS);

            ArrayList<String> movies = new ArrayList<String>();

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing the relative path
                JSONObject relativePath = movieArray.getJSONObject(i);


                //Get the Relative Path
                movies.add(relativePath.getString(POSTER_PATH));

            }
            return formatURL(movies);

        }

        private String[] formatURL(ArrayList<String> movieURLs)
        {

            String[] movies = new String[movieURLs.size()];
            for (int i = 0; i < movies.length; i++)
            {
                movies[i] = "http://image.tmdb.org/t/p/w185//" + movieURLs.get(i).substring(1);
                Log.d("FormatURL","Formatted URL is " + movies[i]);

            }

            for (int i = 0; i < movies.length; i++)
            {
                Log.d("FormatURL","About to Return URL #" + i + ":" + movies[i]);
            }

            return movies;
        }
    }
}
