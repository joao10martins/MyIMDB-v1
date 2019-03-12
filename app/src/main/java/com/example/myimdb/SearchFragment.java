package com.example.myimdb;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private View mView;
    private RequestQueue mRequestQueue;
    private String mUrl;
    private RecyclerView mRecyclerView;
    private List<MovieGenre> mGenreList = new ArrayList<>();
    private HashMap<Integer , MovieGenre> mGenreMap = new HashMap<>();
    private SearchRecyclerAdapter mAdapter;
    private int mListCount;
    private String mSearch_query;
    private Movie mMovie;


    private String sharedPrefsFile = "com.example.myimdb";
    private SharedPreferences mPreferences;
    private List<Movie> mMovieList = new ArrayList<>();


    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_search, container, false);
        // Initialize the list of Genres.
        getGenreList();
        // Search movie
        searchQuery();


        return mView;
    }


    // Get List of all the existing Genres in the API.
    private void getGenreList() {
        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        mUrl = "https://api.themoviedb.org/3/genre/movie/list?api_key=07d93ad59393a99fe6bc8c1b8f0de23b&language=en-US";

        GsonRequest<MovieGenreResults> request = new GsonRequest<>(mUrl,
                MovieGenreResults.class,
                getGenreSuccessListener(),
                getErrorListener());

        mRequestQueue.add(request);
    }


    private Response.Listener<MovieGenreResults> getGenreSuccessListener() {
        return new Response.Listener<MovieGenreResults>() {
            @Override
            public void onResponse(MovieGenreResults response) {
                try {

                    for (MovieGenre movieGenre : response.genreList) {
                        mGenreMap.put(movieGenre.getId(), movieGenre);
                    }

                    //mGenreList.addAll(response.genreList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }



    private Response.ErrorListener getErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Do whatever you want to do with error.getMessage();
                error.printStackTrace();
            }
        };
    }



    // Test query
    private void searchQuery() {
        mListCount = 1;
        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        final EditText query_text = mView.findViewById(R.id.search_movie);

        ImageButton btnSearch = mView.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch_query = query_text.getText().toString();
                mUrl = "https://api.themoviedb.org/3/search/movie?api_key=07d93ad59393a99fe6bc8c1b8f0de23b&language=en-US&query=" + mSearch_query + "&page=1&include_adult=false";

                GsonRequest<MovieResults> request = new GsonRequest<>(mUrl,
                        MovieResults.class,
                        getMovieSuccessListener(),
                        getErrorListener());

                mRequestQueue.add(request);
                ++mListCount;
            }
        });


    }


    private Response.Listener<MovieResults> getMovieSuccessListener() {
        return new Response.Listener<MovieResults>() {
            @Override
            public void onResponse(MovieResults response) {
                mRecyclerView = mView.findViewById(R.id.rvSearch);
                try {
                    mMovieList.addAll(response.movieList);
                    //iterar mMoviesList
                    for(Movie movie : mMovieList){
                        // iterar genres_ids da API
                        // Set genres description
                        movie.setGenresDescription("");
                        for (int genreId : movie.getGenre_ids()){
                            movie.setGenresDescription(movie.getGenresDescription().concat(mGenreMap.get(genreId).getName()) + ", ");
                        }
                        // Remove white space if it exists.
                        if (movie.getGenresDescription().endsWith(" ")){
                            movie.setGenresDescription(movie.getGenresDescription().substring(0, movie.getGenresDescription().length()-1));
                        }
                        // Remove last comma, if it exists.
                        if (movie.getGenresDescription().endsWith(",")){
                            movie.setGenresDescription(movie.getGenresDescription().substring(0, movie.getGenresDescription().length()-1));
                        }
                    }

                    if (mAdapter == null) {
                        mAdapter = new SearchRecyclerAdapter(getContext(), mMovieList);
                        mRecyclerView.setAdapter(mAdapter);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }


                    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);

                            if (!recyclerView.canScrollVertically(1)) {
                                //do something
                                mUrl = "https://api.themoviedb.org/3/search/movie?api_key=07d93ad59393a99fe6bc8c1b8f0de23b&language=en-US&query=" + mSearch_query + "&page=" + mListCount + "&include_adult=false";

                                GsonRequest<MovieResults> request = new GsonRequest<>(mUrl,
                                        MovieResults.class,
                                        getMovieSuccessListener(),
                                        getErrorListener());

                                mRequestQueue.add(request);
                                ++mListCount;
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

}