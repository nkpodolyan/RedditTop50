package com.reddittop

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.text.format.DateUtils
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers


class TopListViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val stateLiveData = MutableLiveData<State>()
    private var loadState = LoadStatus.LOADING
    private val items = ArrayList<Item>()
    private var endOfListReached = false
    private val redditApi: RedditApi = (application as RedditApplication).redditApi
    val state: State
        get() = State(loadState, items.map { RedditItem(it) }, !endOfListReached && items.size < MAX_ITEMS)


    init {
        performNextPageLoad()
    }

    fun getTop(): LiveData<State> = stateLiveData

    fun loadNext() {
        if (!state.hasMoreToLoad || state.loadStatus == LoadStatus.LOADING) return
        performNextPageLoad()
    }

    private fun performNextPageLoad() {
        loadState = LoadStatus.LOADING
        stateLiveData.postValue(state)
        val after = if (items.isEmpty()) null else {
            val lastItem = items[items.size - 1]
            "${lastItem.kind}_${lastItem.data.id}"
        }
        redditApi.top(limit = PAGE_SIZE, after = after)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { response ->
                            loadState = LoadStatus.LOADED
                            items.addAll(response.data.children)
                            endOfListReached = items.size < PAGE_SIZE
                            stateLiveData.postValue(state)
                        },
                        { e ->
                            Log.e("error while loading", "", e)
                            loadState = LoadStatus.LOAD_ERROR
                            stateLiveData.postValue(state)
                        })
    }

    enum class LoadStatus {
        LOADING, LOADED, LOAD_ERROR
    }

    data class State(val loadStatus: TopListViewModel.LoadStatus, val items: List<RedditItem>, val hasMoreToLoad: Boolean)

    data class RedditItem(val title: String,
                          val date: String,
                          val author: String,
                          val comments: Long,
                          val thumbnail: String) {
        constructor(item: Item) : this(item.data.title,
                DateUtils.getRelativeTimeSpanString(item.data.createdUtc * 1000).toString(),
                item.data.author,
                item.data.numComments,
                item.data.thumbnail)
    }

    companion object {
        const val MAX_ITEMS = 50
        const val PAGE_SIZE = 10
    }
}
