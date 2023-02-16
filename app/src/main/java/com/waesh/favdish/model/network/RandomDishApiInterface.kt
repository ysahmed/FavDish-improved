package com.waesh.favdish.model.network

import com.waesh.favdish.model.entities.RandomDish
import com.waesh.favdish.util.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomDishApiInterface {

    //if you are downloading this project
    //you will need to provide your own API_KEY

    @GET(Constants.API_ENDPOINT)
    fun getRandomDish(
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
        @Query(Constants.TAGS) tags: String,
        @Query(Constants.NUMBER) number: Int
    ): Single<RandomDish.Recipes>


    // for coroutines
    @GET(Constants.API_ENDPOINT)
    suspend fun getRandomDishByCoroutine(
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
        @Query(Constants.TAGS) tags: String,
        @Query(Constants.NUMBER) number: Int
    ): Response<RandomDish.Recipes>
}