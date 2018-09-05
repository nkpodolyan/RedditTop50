package com.reddittop

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


class TopListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reddits_list)
        val model = ViewModelProviders.of(this).get(TopListViewModel::class.java)
        model.getTop().observe(this, Observer<TopListViewModel.State> { state -> adjustUi(state) })
        adjustUi(model.state)
    }


    private fun adjustUi(state: TopListViewModel.State?) {
        Toast.makeText(this, state?.loadState?.toString(), Toast.LENGTH_SHORT).show()
    }
}
