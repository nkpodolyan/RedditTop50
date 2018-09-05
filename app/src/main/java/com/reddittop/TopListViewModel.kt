package com.reddittop

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject


class TopListViewModel constructor(application: Application) : AndroidViewModel(application) {

    enum class LoadState {
        LOADING, LOADED, LOAD_ERROR
    }

    data class State(val loadState: TopListViewModel.LoadState, val items: List<Item>?)

    private var loadState = LoadState.LOADING

    private val items = ArrayList<Item>()

    val state: State
        get() = State(loadState, items)

    private val publisher = PublishSubject.create<State>()

    private val redditApi: RedditApi = (application as RedditApplication).redditApi

    init {
        publisher.onNext(TopListViewModel.State(LoadState.LOADING, null))
        redditApi.top(limit = 50)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { response ->
                            loadState = LoadState.LOADED
                            items.addAll(response.data.children)
                            publisher.onNext(state)
                        },
                        { e ->
                            loadState = LoadState.LOAD_ERROR
                            publisher.onNext(state)
                        })
    }

    fun getTop(): LiveData<State> = LiveDataReactiveStreams.fromPublisher(publisher.toFlowable(BackpressureStrategy.LATEST))

}
