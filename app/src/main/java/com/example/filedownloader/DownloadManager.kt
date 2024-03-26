package com.example.filedownloader

/**
 * Created by Suraj Kumar
 * Date: 21.03.2024
 * Email: suraj.kumar@sharechat.co
 *
 * Description: https://chat.openai.com/share/dccd0b89-fb75-4ea3-a0da-6a75d9e8da0a
 * Jira:
 */

import android.content.Context
import android.widget.Toast
import com.example.filedownloader.Downloader.DownloadCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

import java.io.File
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request


class FileDownloadManager(private val downloader: Downloader = Downloader(), val context: Context) {

    private val downloadChannel = Channel<DownloadTask>()
    private val downloadTasks = mutableMapOf<String, DownloadTask>()


    init {
        startDownloadLoop()
    }

    private fun startDownloadLoop() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                for (task in downloadChannel) {
                    if (task.status == TaskStatus.PAUSED) {
                        continue
                    }
                    downloader.downloadFile(task, downloadCallback = object : DownloadCallback {
                        override fun downloadCompleted(taskId: String) {
                            task.downloadCallback.invoke(true, "Successful")
                        }

                        override fun downloadFailed(taskId: String, errorMessage: String) {
                            task.downloadCallback.invoke(
                                true, errorMessage
                            )
                        }

                        override fun removeTask(taskId: String) {
                            downloadTasks.remove(taskId)
                        }

                    })
                }
            }
        }
    }


    fun enqueueDownload(
        url: String,
        destinationFile: File,
        progressCallback: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
        downloadStateCallback: (success: Boolean, message: String?) -> Unit
    ): String {
        val taskId = url.hashCode().toString()
        val task = DownloadTask(taskId, url, destinationFile, progressCallback, downloadStateCallback)
        downloadTasks[taskId] = task
        CoroutineScope(Dispatchers.IO).launch {
            downloadChannel.send(task)
        }
        return taskId
    }

    fun pauseDownload(taskId: String) {
        downloadTasks[taskId]?.status = TaskStatus.PAUSED
    }

    fun resumeDownload(taskId: String) {
        downloadTasks[taskId]?.status = TaskStatus.RUNNING
        // Offer the task back to the channel to resume downloading
        downloadChannel.trySend(downloadTasks[taskId]!!).isSuccess
    }

    data class DownloadTask(
        val taskId: String,
        val url: String,
        val destinationFile: File,
        val progressCallback: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
        val downloadCallback: (success: Boolean, errorMessage: String?) -> Unit,
        var status: TaskStatus = TaskStatus.RUNNING,
        val bytesDownloaded: Long = 0L,
    )

    enum class TaskStatus {
        RUNNING, PAUSED
    }
}
