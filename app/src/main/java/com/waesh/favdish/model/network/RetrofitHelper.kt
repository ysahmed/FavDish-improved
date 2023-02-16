package com.waesh.favdish.model.network

import com.waesh.favdish.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    @Volatile
    private var INSTANCE: Retrofit? = null

    fun getRetrofitInstance(): Retrofit {
        //DOUBLE CHECKED LOCKING
        if (INSTANCE == null) {
            synchronized(this)
            {
                if (INSTANCE == null) {

                    INSTANCE = Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return INSTANCE!!
        //END
    }
}