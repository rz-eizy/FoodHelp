package com.example.foodhelp.data.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // en esta linea se cambia la url de el servidor a cual se conecta
    // esta es la ip de el emulador , si mas adelante se utiliza en aplicacion movil
    // hay que poner la ip de el computador para poder llamar correctamente

    private const val BASE_URL = "http://10.0.2.2:8080/"

    val apiService: RecetaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecetaApiService::class.java)
    }
}