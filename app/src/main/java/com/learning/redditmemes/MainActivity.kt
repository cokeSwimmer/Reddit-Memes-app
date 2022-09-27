package com.learning.redditmemes

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File


class MainActivity : AppCompatActivity() {

    var url: String? = null
    private var permission = 0
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        permission = if(it){
            1
        }
        else{
            0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getMeme()

    }
    private fun getMeme()
    {
        val progressBar:ProgressBar = findViewById(R.id.progressBar)
        progressBar.visibility=View.VISIBLE
// Instantiate the RequestQueue.

        val APIurl = "https://meme-api.herokuapp.com/gimme"

// Request a JSON response from the provided URL.
        val JSONRequest = JsonObjectRequest(Request.Method.GET, APIurl, null,
            { response ->
                url = response.getString("url")
                val imageView:ImageView = findViewById(R.id.imageView)
                Glide.with(this).load(url).listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility=View.GONE
                        Toast.makeText(this@MainActivity,"Loading failed,try next meme",Toast.LENGTH_LONG).show()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                }).into(imageView)
            },
            {
                val imageView:ImageView = findViewById(R.id.imageView)
                imageView.setImageResource(R.drawable.connection_lost)
                Toast.makeText(this,"Check your connection",Toast.LENGTH_SHORT).show()
            })

// Add the request to the RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(JSONRequest)
    }

    fun shareMeme(view: View)
    {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Hey! checkout this 🔥 reddit meme $url")
        }
        val shareIntent = Intent.createChooser(sendIntent, "To the moon...")
        startActivity(shareIntent)
    }

    fun nextMeme(view: View)
    {
        getMeme()
    }

    fun checkPermissions(view: View) {
        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission==1){
            downloadMeme(url,URLUtil.guessFileName(url,null,null))
        }
    }

    private fun downloadMeme(url: String?, fileName: String) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val dLink = Uri.parse(url)
            val request = DownloadManager.Request(dLink)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setMimeType("text/plain")
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator+fileName+".gif")
            downloadManager.enqueue(request)
            Toast.makeText(this, "Downloading started", Toast.LENGTH_SHORT).show()
        }
        catch (e:Exception){
            Toast.makeText(this, "Error,try again !", Toast.LENGTH_SHORT).show()
        }
    }

}
