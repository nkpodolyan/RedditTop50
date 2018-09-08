package com.reddittop

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.text.format.DateUtils
import android.util.Log
import android.webkit.URLUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import java.net.URI


class TopListViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val stateLiveData = MutableLiveData<State>()
    private var loadState = LoadStatus.LOADING
    private val items = ArrayList<Item>()
    private var endOfListReached = false
    private val redditApi: RedditApi = (application as RedditApplication).redditApi
    val state: State
        get() = State(loadState, items.map { redditFrom(it) }, !endOfListReached && items.size < MAX_ITEMS)


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
                          val thumbnail: String,
                          val fullSizeImageUrl: String)

    companion object {
        const val MAX_ITEMS = 50
        const val PAGE_SIZE = 10

        private val supportedImageExtensions = listOf("jpg", "png", "jpeg")

        fun redditFrom(item: Item): RedditItem {
            val thumbnail = item.data.thumbnail
            val fullSizeImageUrl = if (item.data.preview.images.isEmpty()) "" else item.data.preview.images[0].source.url

            return RedditItem(item.data.title,
                    DateUtils.getRelativeTimeSpanString(item.data.createdUtc * 1000).toString(),
                    item.data.author,
                    item.data.numComments,
                    if (URLUtil.isValidUrl(thumbnail)) thumbnail else "",
                    if (isImageUrl(fullSizeImageUrl)) fullSizeImageUrl else "")
        }

        private fun isImageUrl(url: String?): Boolean {
            if (url == null || !URLUtil.isValidUrl(url)) return false
            val uri = URI(url)
            val path = uri.path
            val fileName = path.substring(path.lastIndexOf('/') + 1)
            return supportedImageExtensions.any { extension -> fileName.endsWith(extension) }
        }
    }
}
