package com.reddittop

import android.arch.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
import com.reddittop.TopListViewModel.LoadStatus.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit


@RunWith(RobolectricTestRunner::class)
class TopListViewModelTest {

    @Mock
    lateinit var app: RedditApplication

    @Mock
    lateinit var redditApi: RedditApi

    lateinit var topListViewModel: TopListViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        app.redditApi = redditApi

        //empty response that should be treated as an error
        whenever(redditApi.top(limit = anyOrNull(), after = anyOrNull())).doReturn(Observable.just(TopResponse()))
        topListViewModel = TopListViewModel(app)

        // reseting mock invocations after view model created
        reset(redditApi)
        whenever(redditApi.top(limit = anyOrNull(), after = anyOrNull())).doReturn(Observable.just(TopResponse()))
    }

    @Test
    fun viewModelCreated_pageLoadedRequested() {
        // When
        topListViewModel = TopListViewModel(app)

        // Then
        verify(redditApi).top(after = null, limit = TopListViewModel.PAGE_SIZE)
    }

    @Test
    fun loadPage_dataRequestedFromApi() {
        // When
        topListViewModel.loadNextPage()

        // Then
        verify(redditApi).top(after = null, limit = TopListViewModel.PAGE_SIZE)
    }

    @Test
    fun modelHasItems_nextPageRequest_requestFormedWithLastItemId() {
        // Given
        topListViewModel.items.add(Item(kind = "t3", data = ItemData(id = "id")))

        // When
        topListViewModel.loadNextPage()

        // Then
        verify(redditApi).top(after = "t3_id", limit = TopListViewModel.PAGE_SIZE)
    }


    @Test
    fun multiplyLoadRequests_dataOnlyRequestedOnce() {
        // Given
        whenever(redditApi.top(limit = anyOrNull(), after = anyOrNull())).doReturn(Observable.just(TopResponse())
                .delay(10, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()))

        // When
        topListViewModel.loadNextPage()
        topListViewModel.loadNextPage()
        topListViewModel.loadNextPage()

        // Then
        verify(redditApi, times(1)).top(after = null, limit = TopListViewModel.PAGE_SIZE)
    }

    @Test
    fun liveDataObserverSubscribed_latestResultReceived() {
        // Given
        val liveDataObserver = mock<Observer<TopListViewModel.State>>()
        topListViewModel.getTop().observeForever(liveDataObserver)

        // When
        topListViewModel.getTop().observeForever(liveDataObserver)

        // Then
        verify(liveDataObserver).onChanged(TopListViewModel.State(TopListViewModel.LoadStatus.LOAD_ERROR, ArrayList(), true))
    }

    @Test
    fun itemsLoadError_liveDataObserverNotified() {
        // Given
        whenever(redditApi.top(limit = anyOrNull(), after = anyOrNull())).doReturn(Observable.just(TopResponse()))
        val liveDataObserver = mock<Observer<TopListViewModel.State>>()
        topListViewModel.getTop().observeForever(liveDataObserver)
        reset(liveDataObserver)

        // When
        topListViewModel.loadNextPage()

        // Then
        val statusCaptor = argumentCaptor<TopListViewModel.State>()
        verify(liveDataObserver, atLeastOnce()).onChanged(statusCaptor.capture())
        statusCaptor.allValues.size shouldEqual 2
        statusCaptor.allValues[0].loadStatus shouldEqual LOADING
        statusCaptor.allValues[1].loadStatus shouldEqual LOAD_ERROR
    }

    @Test
    fun itemsLoaded_liveDataObserverNotified() {
        // Given

        val item = Item(data = ItemData(id = "id"))
        val items = mutableListOf(item)
        val response = TopResponse(ResponseData(children = items))
        whenever(redditApi.top(limit = anyOrNull(), after = anyOrNull())).doReturn(Observable.just(response))
        val liveDataObserver = mock<Observer<TopListViewModel.State>>()
        topListViewModel.getTop().observeForever(liveDataObserver)
        reset(liveDataObserver)

        // When
        topListViewModel.loadNextPage()

        // Then
        val statusCaptor = argumentCaptor<TopListViewModel.State>()
        verify(liveDataObserver, atLeastOnce()).onChanged(statusCaptor.capture())
        statusCaptor.allValues.size shouldEqual 2
        statusCaptor.allValues[0].loadStatus shouldEqual LOADING
        statusCaptor.allValues[1].loadStatus shouldEqual LOADED
        statusCaptor.allValues[1].items.size shouldEqual 1
        statusCaptor.allValues[1].hasMoreToLoad shouldEqual false
    }

    private infix fun Any.shouldEqual(theOther: Any) = assertEquals(theOther, this)
}
