package com.gym_helper_android

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.os.StrictMode
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {
    private val cameraResult = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, cameraResult)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == cameraResult) {
            var bmp = data!!.extras.get("data") as Bitmap
            val base64Image: String? = encodeToBase64(bmp)!!.replace("\n", "")

            if(base64Image != null) {
                val SDK_INT = android.os.Build.VERSION.SDK_INT

                if (SDK_INT > 8) {
                    val policy = StrictMode.ThreadPolicy.Builder()
                            .permitAll().build()
                    StrictMode.setThreadPolicy(policy)
                    sendPostRequest(base64Image)
                }
            }
        }
    }

    private fun encodeToBase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun sendPostRequest(base64Image: String) {
        val postData = "{\"base64Image\": \"$base64Image\"}"

        val request = Fuel.post("https://pacific-reef-79944.herokuapp.com")

        request.headers.remove("Content-Type")
        request.headers.put("Content-Type", "application/json")

        request.body(postData)
            .responseString { request, response, result ->
                run {
                    val jsonResult = result.get()
                    val arrayResult = Klaxon().parseArray<String>(jsonResult)
                    watchYoutubeVideo(arrayResult!!.first())
                }
            }
    }

    private fun watchYoutubeVideo(id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id))

        try {
            startActivity(appIntent);
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent);
        }
    }
}