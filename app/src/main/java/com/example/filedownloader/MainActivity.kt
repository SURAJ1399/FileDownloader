package com.example.filedownloader

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat

import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadManager = FileDownloadManager(context = this)
        val TAG = "Suraj"
        val file = File(applicationContext.filesDir, "newVideotest.mp4")
        setContent {
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "image")

            }

        }
        Log.d(TAG, "File exists: " + file.exists());
        Log.d(TAG, "File readable: " + file.canRead());
        Log.d(TAG, "File writable: " + file.canWrite());
        Log.d(TAG, "File path: " + file.absolutePath);
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {


            //  val file = File(filesDir, "newImagetest.jpg")

            downloadManager.enqueueDownload("https://download.samplelib.com/mp4/sample-15s.mp4",
                file, progressCallback = { x, y ->
                },
                downloadStateCallback = { x, y ->
                    Toast.makeText(this@MainActivity, y, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        }

    }
}
