package com.reddittop

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers


class TopListViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val statLiveData = MutableLiveData<State>()
    private var loadState = LoadStatus.LOADING
    private val items = ArrayList<Item>()
    private var endOfListReached = false
    private val redditApi: RedditApi = (application as RedditApplication).redditApi
    val state: State
        get() = State(loadState, items, !endOfListReached && items.size < MAX_ITEMS)


    init {
        performNextPageLoad()
    }

    fun getTop(): LiveData<State> = statLiveData

    fun loadNext() {
        if (!state.hasMoreToLoad || state.loadStatus == LoadStatus.LOADING) return
        performNextPageLoad()
    }

    private fun performNextPageLoad() {
        loadState = LoadStatus.LOADING
        statLiveData.postValue(state)
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
                            statLiveData.postValue(state)
                        },
                        { e ->
                            Log.e("error while loading", "", e)
                            loadState = LoadStatus.LOAD_ERROR
                            statLiveData.postValue(state)
                        })
    }

    enum class LoadStatus {
        LOADING, LOADED, LOAD_ERROR
    }

    data class State(val loadStatus: TopListViewModel.LoadStatus, val items: List<Item>, val hasMoreToLoad: Boolean)

    companion object {
        const val MAX_ITEMS = 50
        const val PAGE_SIZE = 10
    }
}
