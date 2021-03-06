package com.example.pocketbook.notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Client class for retrofit2
public class Client {

    private static Retrofit retrofit = null;

    /**
     * getter method for the retrofit client
     * @param url
     * @return retrofit object
     */
    public static Retrofit getClient(String url){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
