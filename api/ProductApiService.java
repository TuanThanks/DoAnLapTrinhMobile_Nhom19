package com.example.exercise1_team.nhk.api;

import com.example.exercise1_team.nhk.model.ProductItem;
import com.example.exercise1_team.nhk.model.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductApiService {

    @GET("api.php?action=products")
    Call<ProductResponse> getProducts();

}