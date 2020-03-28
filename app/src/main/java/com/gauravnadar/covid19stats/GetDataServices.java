package com.gauravnadar.covid19stats;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataServices {

    @GET("top-headlines")                                 //changed
    Call<NewsModel> getTopHeadlines(
            @Query("country") String country,
            @Query("q") String keyword,
            @Query("apiKey") String apikey
    );

}
