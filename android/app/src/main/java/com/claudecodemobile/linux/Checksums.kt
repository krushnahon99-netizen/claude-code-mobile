package com.claudecodemobile.linux

import java.io.File
import java.security.MessageDigest

/** A file listed in a published checksum file such as Ubuntu's SHA256SUMS. */
data class ChecksumEntry(val fileName: String, val sha256: String)

/** A file to download and the SHA-256 it must match. */
data class RemoteFile(val url: String, val fileName: String, val sha256: String)

object Checksums {
    private val LINE = Regex("""([0-9a-fA-F]{64})\s+\*?(\S.*)""")

    /** Parses `<sha256>  <name>` lines. A `*` before the name (binary mode) is ignored. */
    fun parse(text: String): List<ChecksumEntry> = text.lineSequence()
        .mapNotNull { LINE.matchEntire(it.trim()) }
        .map { ChecksumEntry(fileName = it.groupValues[2].trim(), sha256 = it.groupValues[1].lowercase()) }
        .toList()

    fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
    }

    /** The entry matching [pattern] whose first capture group is the highest dotted version. */
    internal fun newest(entries: List<ChecksumEntry>, pattern: Regex): ChecksumEntry? = entries
        .mapNotNull { entry -> pattern.matchEntire(entry.fileName)?.let { versionOf(it.groupValues[1]) to entry } }
        .maxWithOrNull { a, b -> compareVersions(a.first, b.first) }
        ?.second

    internal fun compareVersions(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until maxOf(a.size, b.size)) {
            val diff = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }

    private fun versionOf(text: String) = text.split('.').map { it.toIntOrNull() ?: 0 }
}

/** Ubuntu Base 24.04 LTS for arm64. The release folder lists several point releases. */
object UbuntuBase {
    const val RELEASE_URL = "https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/"
    const val CHECKSUMS_URL = RELEASE_URL + "SHA256SUMS"
    private val TARBALL = Regex("""ubuntu-base-(\d+(?:\.\d+)*)-base-arm64\.tar\.gz""")

    fun latest(checksums: List<ChecksumEntry>): RemoteFile? =
        Checksums.newest(checksums, TARBALL)?.let { RemoteFile(RELEASE_URL + it.fileName, it.fileName, it.sha256) }
}

/** The official Node.js 24 LTS build for linux-arm64 (Ubuntu's own nodejs package is too old). */
object NodeJs {
    const val DIST_URL = "https://nodejs.org/dist/latest-v24.x/"
    const val CHECKSUMS_URL = DIST_URL + "SHASUMS256.txt"
    private val TARBALL = Regex("""node-v(\d+\.\d+\.\d+)-linux-arm64\.tar\.gz""")

    fun latest(checksums: List<ChecksumEntry>): RemoteFile? =
        Checksums.newest(checksums, TARBALL)?.let { RemoteFile(DIST_URL + it.fileName, it.fileName, it.sha256) }
}
