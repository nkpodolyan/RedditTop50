package com.reddittop

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_reddits_list.*
import kotlinx.android.synthetic.main.raw_reddit.*
import kotlinx.android.synthetic.main.raw_retry.view.*


class TopListActivity : AppCompatActivity() {

    private val adapter = RedditsAdapter()
    lateinit var model: TopListViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reddits_list)
        model = ViewModelProviders.of(this).get(TopListViewModel::class.java)
        model.getTop().observe(this, Observer<TopListViewModel.State> { state -> adjustUi(state) })
        setUpRecyclerView()
        adjustUi(model.state)
    }

    private fun setUpRecyclerView() {
        val itemDecoration = ItemSpacingDecoration(16F, applicationContext)
        val layoutManager = LinearLayoutManager(this)
        adapter.setRetryHandler { model.loadNext() }
        itemsList.layoutManager = layoutManager
        itemsList.setHasFixedSize(true)
        itemsList.adapter = adapter
        itemsList.addItemDecoration(itemDecoration)
        itemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (model.state.loadStatus == TopListViewModel.LoadStatus.LOADED && model.state.hasMoreToLoad) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && totalItemCount > 1) {
                        model.loadNext()
                    }
                }
            }
        })
    }


    private fun adjustUi(state: TopListViewModel.State?) {
        state?.let {
            val lastItem = when (it.loadStatus) {
                TopListViewModel.LoadStatus.LOADED -> RedditsAdapter.TailItem.NONE
                TopListViewModel.LoadStatus.LOADING -> RedditsAdapter.TailItem.PROGRESS
                TopListViewModel.LoadStatus.LOAD_ERROR -> RedditsAdapter.TailItem.RETRY
            }
            adapter.setData(it.items, lastItem)
        }
    }


    class RedditsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var retryHandler: (() -> Unit)? = null
        private val items = ArrayList<TopListViewModel.RedditItem>()
        private var lastItem = TailItem.NONE

        fun setRetryHandler(retryHandler: () -> Unit) {
            this.retryHandler = retryHandler
        }

        fun setData(items: List<TopListViewModel.RedditItem>, lastItem: TailItem) {
            this.items.clear()
            this.items.addAll(items)
            this.lastItem = lastItem
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder =
                when (type) {
                    TYPE_PROGRESS -> createProgressHolder(parent)
                    TYPE_RETRY -> createRetryHolder(parent)
                    else ->
                        createItemHolder(parent)
                }

        override fun getItemViewType(position: Int): Int =
                if (position == items.size) {
                    if (lastItem == TailItem.PROGRESS) {
                        TYPE_PROGRESS
                    } else {
                        TYPE_RETRY
                    }
                } else {
                    TYPE_ITEM
                }


        override fun getItemCount(): Int =
                if (lastItem == TailItem.NONE) {
                    items.size
                } else
                    items.size + 1


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                TYPE_ITEM -> (holder as RedditHolder).bind(items[position])
            }
        }

        private fun createItemHolder(parent: ViewGroup): RedditHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_reddit, parent, false)
            return RedditHolder(view)
        }

        private fun createProgressHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_progress, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        private fun createRetryHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_retry, parent, false)
            (view.retryButton as Button).setOnClickListener {
                retryHandler?.invoke()
            }
            return object : RecyclerView.ViewHolder(view) {}
        }

        enum class TailItem {
            NONE, PROGRESS, RETRY
        }

        companion object {
            const val TYPE_ITEM = R.layout.raw_reddit
            const val TYPE_PROGRESS = R.layout.raw_progress
            const val TYPE_RETRY = R.layout.raw_retry
        }
    }


    class RedditHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: TopListViewModel.RedditItem) {
            val context = itemTitle.context
            val thumbnailUrl = item.thumbnail
            itemTitle.text = item.title
            author.text = context.getString(R.string.by, item.author)
            date.text = item.date
            comments.text = context.getString(R.string.comments, item.comments)
            itemImage.visibility = if (URLUtil.isValidUrl(thumbnailUrl)) {
                GlideApp.with(containerView.context)
                        .load(thumbnailUrl)
                        .fitCenter()
                        .into(itemImage)
                View.VISIBLE
            } else
                View.GONE
        }
    }
}
