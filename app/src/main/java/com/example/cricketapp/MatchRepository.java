package com.example.cricketapp;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MatchRepository {

    private MatchDAO matchDAO;
    private LiveData<List<Match>> matches;

    public MatchRepository(Application application)
    {
        AppDatabase appDatabase = AppDatabase.getInstance(application);
        matchDAO = appDatabase.getMatchDAO();
    }

    public LiveData<List<Match>> getMatches() {
        matches = matchDAO.getMatches();
        if (matches.getValue() == null) {
            OkHttpClient unsafeOkHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://api.crickssix.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(unsafeOkHttpClient)
                    .build();
            CricketService cricketService = retrofit.create(CricketService.class);
            Call<MatchList> call = cricketService.getAllMatches();
            call.enqueue(new Callback<MatchList>() {
                @Override
                public void onResponse(Call<MatchList> call, Response<MatchList> response) {
                    for (Match match : response.body().matchList) {
                        insert(match);
                    }
                    matches = matchDAO.getMatches();
                }

                @Override
                public void onFailure(Call<MatchList> call, Throwable t) {

                }
            });
        }
        return matches;
    }


    public void insert (Match match)
    {
        matchDAO.insert(match);
    }
}