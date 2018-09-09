package com.reddittop

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.VisibleForTesting
import android.text.format.DateUtils
import android.webkit.URLUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import java.net.URI


class TopListViewModel constructor(application: Application) : AndroidViewModel(application) {

    @VisibleForTesting val items = ArrayList<Item>()
    private val stateLiveData = MutableLiveData<State>()
    private var loadState = LoadStatus.LOADING
    private var endOfListReached = false
    private val redditApi: RedditApi = (application as RedditApplication).redditApi
    val state: State
        get() = State(loadState, items.map { redditFrom(it) }, !endOfListReached && items.size < MAX_ITEMS)


    init {
        performNextPageLoad()
    }

    fun getTop(): LiveData<State> = stateLiveData

    fun loadNextPage() {
        if (!state.hasMoreToLoad || state.loadStatus == LoadStatus.LOADING) return
        performNextPageLoad()
    }

    private fun performNextPageLoad() {
        loadState = LoadStatus.LOADING
        stateLiveData.postValue(state)
        val after = if (items.isEmpty()) null else {
            val lastItem = items[items.size - 1]
            "${lastItem.kind}_${lastItem.data!!.id}"
        }
        redditApi.top(limit = PAGE_SIZE, after = after)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextPageLoaded) { onLoadError() }
    }

    private fun onNextPageLoaded(response: TopResponse) {
        val loadedItems = response.data?.children
        if (loadedItems != null) {
            loadState = LoadStatus.LOADED
            endOfListReached = loadedItems.size < PAGE_SIZE
            items.addAll(loadedItems.filter { it.data?.id != null })
        } else {
            loadState = LoadStatus.LOAD_ERROR
        }
        stateLiveData.postValue(state)
    }

    private fun onLoadError() {
        loadState = LoadStatus.LOAD_ERROR
        stateLiveData.postValue(state)
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
            val itemData = item.data
            val thumbnail = itemData?.thumbnail ?: ""

            val previewImages = itemData?.preview?.images

            val fullSizeImageUrl = if (previewImages == null || previewImages.isEmpty()) ""
            else previewImages[0].source?.url

            return RedditItem(itemData?.title ?: "",
                    DateUtils.getRelativeTimeSpanString(itemData?.createdUtc ?: 0
                    * 1000).toString(),
                    itemData?.author ?: "",
                    itemData?.numComments ?: 0,
                    if (URLUtil.isValidUrl(thumbnail)) thumbnail else "",
                    if (isImageUrl(fullSizeImageUrl)) fullSizeImageUrl!! else "")
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
