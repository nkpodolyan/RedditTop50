package com.reddittop

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_reddits_list.*
import kotlinx.android.synthetic.main.raw_reddit.*


class TopListActivity : AppCompatActivity() {

    val adapter = ItemsAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reddits_list)
        val model = ViewModelProviders.of(this).get(TopListViewModel::class.java)
        model.getTop().observe(this, Observer<TopListViewModel.State> { state -> adjustUi(state) })
        adjustUi(model.state)

        val itemDecoration = ItemSpacingDecoration(16F, applicationContext)
        itemsList.layoutManager = LinearLayoutManager(this)
        itemsList.setHasFixedSize(true)
        itemsList.adapter = adapter
        itemsList.addItemDecoration(itemDecoration)

    }


    private fun adjustUi(state: TopListViewModel.State?) {
        val loading = state?.loadState == TopListViewModel.LoadState.LOADING
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        adapter.setItems(state?.items ?: ArrayList())
    }

    class ItemsAdapter : RecyclerView.Adapter<ItemHolder>() {

        private val items = ArrayList<Item>()

        fun setItems(items: List<Item>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_reddit, parent, false)
            return ItemHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(items[position])
        }

    }

    class ItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: Item) {
            val context = itemTitle.context
            itemTitle.text = item.data.title
            author.text = context.getString(R.string.by, item.data.author)
            date.text = DateUtils.getRelativeTimeSpanString(item.data.createdUtc * 1000)
            comments.text = context.getString(R.string.comments, item.data.numComments)
            val thumbnailUrl = item.data.thumbnail
            itemImage.visibility = if (URLUtil.isValidUrl(item.data.thumbnail)) {
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
