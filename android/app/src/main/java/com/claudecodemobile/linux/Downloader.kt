package com.claudecodemobile.linux

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class DownloadProgress(val bytesDone: Long, val totalBytes: Long?) {
    val fraction: Float?
        get() = totalBytes?.takeIf { it > 0 }?.let { (bytesDone.toFloat() / it).coerceIn(0f, 1f) }
}

class ChecksumMismatchException(fileName: String) :
    IOException("$fileName was damaged while downloading: it doesn't match its published SHA-256 checksum.")

class HttpStatusException(val code: Int, url: String) :
    IOException("The server answered HTTP $code for $url")

class Downloader(private val userAgent: String) {

    suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
        val connection = open(url)
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw HttpStatusException(code, url)
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Downloads [file] to [target], resuming an earlier partial download when the server
     * allows it. The file only appears at [target] once its SHA-256 matches, and this
     * returns at once when [target] already holds the right bytes.
     */
    suspend fun download(
        file: RemoteFile,
        target: File,
        onProgress: (DownloadProgress) -> Unit = {},
    ): Unit = withContext(Dispatchers.IO) {
        if (target.isFile && Checksums.sha256Of(target) == file.sha256) return@withContext
        val dir = target.absoluteFile.parentFile ?: throw IOException("No folder for ${target.path}")
        dir.mkdirs()
        val partial = File(dir, target.name + ".part")

        // A damaged partial file fails the check on the first try; the second starts from byte 0.
        repeat(2) { attempt ->
            if (attempt > 0) partial.delete()
            transfer(file.url, partial, onProgress)
            if (Checksums.sha256Of(partial) == file.sha256) {
                target.delete()
                if (!partial.renameTo(target)) throw IOException("Couldn't save ${target.name}")
                return@withContext
            }
        }
        partial.delete()
        throw ChecksumMismatchException(file.fileName)
    }

    private suspend fun transfer(url: String, partial: File, onProgress: (DownloadProgress) -> Unit) {
        val offset = if (partial.isFile) partial.length() else 0L
        val connection = open(url)
        try {
            if (offset > 0) connection.setRequestProperty("Range", "bytes=$offset-")
            val append = when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_PARTIAL -> true
                HttpURLConnection.HTTP_OK -> false
                HTTP_RANGE_NOT_SATISFIABLE -> return // Nothing left to fetch: the partial file is complete.
                else -> throw HttpStatusException(code, url)
            }
            val start = if (append) offset else 0L
            val total = connection.contentLengthLong.takeIf { it >= 0 }?.let { it + start }
            var done = start
            onProgress(DownloadProgress(done, total))
            FileOutputStream(partial, append).use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        done += read
                        onProgress(DownloadProgress(done, total))
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", userAgent)
            // Byte offsets for resuming only work on the raw file, never on a re-compressed stream.
            setRequestProperty("Accept-Encoding", "identity")
        }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
        const val HTTP_RANGE_NOT_SATISFIABLE = 416
    }
}
