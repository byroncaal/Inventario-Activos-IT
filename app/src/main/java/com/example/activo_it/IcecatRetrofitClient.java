package com.example.activo_it;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IcecatRetrofitClient {

    private static final String BASE_URL = "https://live.icecat.biz/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}