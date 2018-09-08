package com.reddittop

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_full_sized_image.*
import java.io.File
import java.util.*


class FullSizedImageActivity : AppCompatActivity() {

    val pictureUrl: String
        get() = intent.getStringExtra(URL_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_sized_image)
        supportActionBar?.also {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        GlideApp.with(this)
                .load(pictureUrl)
                .fitCenter()
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

        const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0
        const val URL_KEY = "url"

        fun start(context: Activity, url: String) {
            context.startActivity(Intent(context, FullSizedImageActivity::class.java).also { it.putExtra(URL_KEY, url) })
        }
    }
}



