package com.reddittop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_full_sized_image.*

class FullSizedImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_sized_image)
        supportActionBar?.also {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        GlideApp.with(this)
                .load(intent.getStringExtra(URL_KEY))
                .into(image)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.full_size_picture_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.save_to_gallery -> {
                Toast.makeText(this, "Save image to gallery", Toast.LENGTH_SHORT).show()

            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {

        const val URL_KEY = "url"

        fun start(context: Activity, url: String) {
            context.startActivity(Intent(context, FullSizedImageActivity::class.java).also { it.putExtra(URL_KEY, url) })
        }
    }
}



