package app.aghaiand.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcels;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private View rootView;


    public DetailActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Movie receivedMovie = (Movie) Parcels.unwrap(getActivity().getIntent().getParcelableExtra("movie"));
        Log.d("DetailOnCreate","Recieved Movie with the Title" + receivedMovie.getTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }
}
