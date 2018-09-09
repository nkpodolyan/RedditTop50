package com.reddittop

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_full_sized_image.*
import java.io.File
import java.util.*


class FullSizedImageActivity : AppCompatActivity() {

    private val pictureUrl: String
        get() = intent.getStringExtra(IMAGE_URL_KEY)
    private var imageLoadingState: ImageLoadingState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_sized_image)
        ViewCompat.setTransitionName(image, SHARED_IMAGE_NAME)
        supportActionBar?.also {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        retryButton.setOnClickListener { loadFullSizeImage() }
        loadFullSizeImage()
    }

    override fun onResume() {
        super.onResume()
        //fix of transition animation changes progress bar visibility to visible
        adjustUiToImageLoadingState()
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
            R.id.save_to_gallery -> saveClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    downloadImageToGallery()
                } else {
                    Snackbar.make(image, getString(R.string.missing_permission_explanation), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.grant)) { _ ->
                                startActivity(Intent().also {
                                    it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    it.data = Uri.fromParts("package", packageName, null)
                                })
                            }
                            .show()
                }
            }
        }
    }

    private fun setImageLoadStateAdjustUi(state: ImageLoadingState) {
        imageLoadingState = state
        adjustUiToImageLoadingState()
    }

    private fun adjustUiToImageLoadingState() {
        retryButton.visibility = if (imageLoadingState == ImageLoadingState.LOADING) View.VISIBLE else View.GONE
        retryButton.visibility = if (imageLoadingState == ImageLoadingState.LOAD_ERROR) View.VISIBLE else View.GONE
    }

    private fun loadFullSizeImage() {
        setImageLoadStateAdjustUi(ImageLoadingState.LOADING)
        GlideApp.with(this)
                .load(pictureUrl)
                .fitCenter()
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        setImageLoadStateAdjustUi(ImageLoadingState.LOAD_ERROR)
                        Snackbar.make(image, getString(R.string.could_not_load_image), Snackbar.LENGTH_SHORT).show()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        setImageLoadStateAdjustUi(ImageLoadingState.LOADED)
                        return false
                    }
                })
                .into(image)
    }


    private fun saveClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRationale()
            } else {
                requestWritePermission()
            }
        } else {
            downloadImageToGallery()
        }
    }

    private fun showRationale() {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.write_storage_permision_retionale))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    requestWritePermission()
                }
                .show()
    }

    private fun downloadImageToGallery() {
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(pictureUrl)
        val request = DownloadManager.Request(downloadUri)
        val fileName = if (downloadUri.pathSegments.isEmpty()) Calendar.getInstance().time.toString() else downloadUri.pathSegments?.last()
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(pictureUrl)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                        File.separator + fileName)
        dm.enqueue(request)
        Snackbar.make(image, getString(R.string.loading_started), Snackbar.LENGTH_SHORT).show()
    }

    private fun requestWritePermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS)
    }


    companion object {

        private enum class ImageLoadingState {
            LOADING, LOADED, LOAD_ERROR
        }

        const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0
        const val IMAGE_URL_KEY = "url"
        const val SHARED_IMAGE_NAME = "image"

        fun start(context: Activity,
                  fullImageUrl: String,
                  image: ImageView) {
            val options = ActivityOptions.makeSceneTransitionAnimation(context, image, SHARED_IMAGE_NAME)
            val intent = Intent(context,
                    FullSizedImageActivity::class.java).also { it.putExtra(IMAGE_URL_KEY, fullImageUrl) }
            context.startActivity(intent, options.toBundle())
        }
    }
}



