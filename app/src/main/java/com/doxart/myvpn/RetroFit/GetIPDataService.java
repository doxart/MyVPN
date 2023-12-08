package com.doxart.myvpn.RetroFit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetIPDataService {
    @GET("/json")
    Call<MyIP> getMyIP();
}
