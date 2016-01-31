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
public class MainActivityFragment extends Fragment
{


    private View rootView;
    private ImageListAdapter adapter;
    private String[] movieIDs;

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
                FetchMovieData movieRequested = new FetchMovieData();
                movieRequested.execute(movieIDs[i]);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updatePosters()
    {
        FetchMoviePoster myTask = new FetchMoviePoster();
        myTask.execute();
    }


   /*
   ===================================================================================
    CLASS NAME: FetchMoviePoster
    CLASS PURPOSE: Extract Movie Poster URL's as well as Movie ID's for future calls.
    Movie ID's are set in a global variable known as MovieID's
   ===================================================================================
     */

    public class FetchMoviePoster extends AsyncTask<Void, Void, String[]> {


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
                // Construct the URL for the MovieDB query
                URL url = new URL(myBuilder.toString());
                Log.d("FetchMovieData","URL is " + url);
                // Create the request to MovieDB, and open the connection
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
                // If the code didn't successfully get the movie data, there's no point in attempting
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

        private String[] getMovieDataFromJSON(String movieJSONstr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            final String ID_PATH = "id";


            JSONObject movieJSON = new JSONObject(movieJSONstr);
            JSONArray movieArray = movieJSON.getJSONArray(RESULTS);

            ArrayList<String> movies = new ArrayList<String>();
            ArrayList<String> movieID = new ArrayList<String>();


            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing the relative path
                JSONObject relativePath = movieArray.getJSONObject(i);
                // Get the JSON object representing the movie ID
                JSONObject ID = movieArray.getJSONObject(i);

                //Get the Movie ID for each movie
                movieID.add(ID.getString(ID_PATH));
                Log.d("getMovieData","ID found:\t" + movieID.get(i));

                //Get the Relative Path
                movies.add(relativePath.getString(POSTER_PATH));

            }
            //Store results of movie ID's in global String array for future use
            movieIDs = movieID.toArray(new String[movieID.size()]);
            if(movieIDs == null)
            {
                Log.d("MainActivitiyFragment","String transfer did not work!");
            }
            else
            {
                for(int i = 0; i < movieIDs.length;i++)
                {
                    Log.d("MainActivitiyFragment","ID Transferred:\t" + movieIDs[i]);
                }
            }
            //Call the formatURL function to create completed URL's
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

   /*
   ===================================================================================
    CLASS NAME: FetchMovieData
    CLASS PURPOSE: Extract information regarding a single MovieID upon selection of a
    poster in the MainActivity Fragment
   ===================================================================================
     */

    public class FetchMovieData extends AsyncTask<String, Void, Movie>
    {


        @Override
        protected Movie doInBackground(String... params)
        {
            final String SCHEME = "http";
            final String AUTHORITY = "api.themoviedb.org";
            final String PATH = "3/movie";
            final String API_KEY_QUERY = "api_key";

            Uri.Builder myBuilder = new Uri.Builder();
            myBuilder.scheme(SCHEME);
            myBuilder.authority(AUTHORITY);
            myBuilder.path(PATH);
            myBuilder.appendPath(params[0]);
            myBuilder.appendQueryParameter(API_KEY_QUERY, "14cb79ed3293203b59845c42a0c3b309");
            myBuilder.build();
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieDataJSONStr = null;

            try {
                // Construct the URL for the MovieDB query
                URL url = new URL(myBuilder.toString());
                Log.d("FetchMovieData","URL is " + url);
                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    movieDataJSONStr = null;
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
                    movieDataJSONStr = null;
                }
                movieDataJSONStr = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                movieDataJSONStr = null;
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
                Log.d("GetMovieData","JSON Object is " + movieDataJSONStr);
                return getMovieDataFromJSON(movieDataJSONStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Keep in the mind that onPostExecute is what method is run in order to merge the
        //background thread back to the Main UI thread

        @Override
        protected void onPostExecute(Movie myMovie) {
            super.onPostExecute(myMovie);
//
//            if (strings != null) {
//                adapter = new ImageListAdapter(getActivity(),strings);
//                GridView lv = (GridView) rootView.findViewById(R.id.listview_movies);
//                lv.setAdapter(adapter);
//            }
//            else
//            {
//                Log.d("onPostExecute","Strings are NULL!!!");
//            }
        }

        private Movie getMovieDataFromJSON(String movieJSONstr)
                throws JSONException
        {

            // These are the names of the JSON objects that need to be extracted.
            final String TITLE_ATTRIBUTE = "title";
            final String VOTE_ATTRIBUTE = "vote_average";
            final String OVERVIEW_ATTRIBUTE = "overview";
            final String DATE_ATTRIBUTE = "release_date";

            JSONObject movieJSON = new JSONObject(movieJSONstr);

            // Get the JSON object representing the relative path
            String title = movieJSON.getString(TITLE_ATTRIBUTE);
            String vote = movieJSON.getString(VOTE_ATTRIBUTE);
            String overview = movieJSON.getString(OVERVIEW_ATTRIBUTE);
            String releaseDate = movieJSON.getString(DATE_ATTRIBUTE);

            Movie myMovie = new Movie(title,vote,overview,releaseDate);
            Log.d("getMovieDataFromJSON","Object of Type \"Movie\" has been created with the following properties:\n");
            Log.d("getMovieDataFromJSON","Title:" + title + "\n");
            Log.d("getMovieDataFromJSON","Vote:" + vote + "/10\n");
            Log.d("getMovieDataFromJSON","Overview:" + overview + "\n");
            Log.d("getMovieDataFromJSON","Release Date:" + releaseDate + "\n");
            return myMovie;

        }
    }





}
