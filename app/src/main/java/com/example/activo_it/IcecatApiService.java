package com.example.activo_it;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface IcecatApiService {

    // Busca la ficha técnica completa de un producto por Marca + Modelo
    // (Brand + ProductCode). No usamos GTIN porque, en un contexto de
    // inventario de TI, casi nunca tienes a mano el código de barras
    // original del producto (eso solo viene en el empaque).
    @GET("api")
    Call<JsonObject> getProductByBrandAndModel(
            @Header("api_token") String apiToken,
            @Header("content_token") String contentToken,
            @Query("shopname") String shopname,
            @Query("Brand") String brand,
            @Query("ProductCode") String productCode,
            @Query("lang") String lang,
            @Query("content") String content // vacío = producto completo
    );
}