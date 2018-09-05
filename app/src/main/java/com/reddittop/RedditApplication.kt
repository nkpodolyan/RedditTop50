package com.reddittop

import android.app.Application
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RedditApplication : Application() {

    lateinit var redditApi : RedditApi

    override fun onCreate() {
        super.onCreate()
        redditApi = createRedditApi()
    }


    fun createRedditApi(): RedditApi {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("https://www.reddit.com/")
                .build()
        return retrofit.create(RedditApi::class.java)
    }

}