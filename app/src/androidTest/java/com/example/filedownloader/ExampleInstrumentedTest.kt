package com.example.filedownloader

import android.widget.Toast
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.filedownloader", appContext.packageName)
        val downloader = FileDownloader()
        val file = File(appContext.filesDir, "newImagetest")
        downloader.enqueueDownload("https://file-examples.com/storage/fe7c2cbe4b65fa8179825d1/2017/10/file_example_JPG_1MB.jpg",
            file, progressCallback = { x, y ->

            },
            completionCallback = { x, y ->
              //  Toast.makeText(this, "Download COmpele", Toast.LENGTH_LONG).show()
            }
        )
    }
}