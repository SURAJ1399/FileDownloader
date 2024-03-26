package com.example.filedownloader

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by Suraj Kumar
 * Date: 25.03.2024
 * Email: suraj.kumar@sharechat.co
 *
 * Description:
 * Jira:
 */
class Downloader {

    private val client = OkHttpClient()
    private val progressMap = mutableMapOf<String, Long>()

    /**
     * @see this approach allows you to read and process data in chunks,
     * which can significantly improve the
     * performance of I/O operations compared to reading byte-by-byte using
     *   while (true) {
     *             bytesRead = input.read(buffer)
     *             if (bytesRead == -1) {
     *                 break
     *             }
     *
     **/
    suspend fun downloadFile(task: FileDownloadManager.DownloadTask, downloadCallback: DownloadCallback) {
        //   val request = Request.Builder().url(task.url).header("Range","bytes${task.bytesDownloaded}").build()
        val request = Request.Builder().url(task.url).build()

        val destinationFile = task.destinationFile
        val taskId = task.taskId

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                val totalBytes = response.body?.contentLength() ?: 0
                var bytesDownloaded: Long = progressMap[taskId] ?: 0

                response.body?.byteStream()?.use { input ->
                    destinationFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also {
                                bytesRead = it
                            } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            progressMap[taskId] = bytesDownloaded
                            withContext(Dispatchers.Main) {
                                task.progressCallback.invoke(bytesDownloaded, totalBytes)
                            }
                            if (task.status == FileDownloadManager.TaskStatus.PAUSED) {
                                break
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    task.downloadCallback.invoke(true, "Successful")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                task.downloadCallback.invoke(false, e.message)
            }
        } finally {
            // Remove task from download tasks and progress map when completed
            downloadCallback.removeTask(taskId)
            progressMap.remove(taskId)
        }
    }

    interface DownloadCallback {
        fun downloadCompleted(taskId: String)
        fun downloadFailed(taskId: String, errorMsg: String)
        fun removeTask(taskId: String)
    }
}