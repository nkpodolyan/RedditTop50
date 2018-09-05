package com.reddittop

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditApi {

    @GET("top.json")
    fun top(@Query("after") after: String? = null,
            @Query("limit") limit: Int? = null)
            : Observable<TopResponse>

}