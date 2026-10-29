package com.claudecodemobile.linux

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.util.EnumSet
import java.util.zip.GZIPInputStream

private const val BUFFER_SIZE = 64 * 1024

data class ExtractProgress(val entries: Int, val bytesRead: Long, val totalBytes: Long) {
    val fraction: Float
        get() = if (totalBytes > 0) (bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
}

/**
 * Unpacks a .tar.gz the way a Linux system needs it on app storage: symlinks stay symlinks,
 * hard links become copies (Android rejects hard links), and permission bits are kept, always
 * with read and write for the app itself. Device nodes and FIFOs are skipped because proot
 * binds the real /dev. An entry that would land outside the destination fails the extraction.
 */
object TarExtractor {
    suspend fun extractTarGz(
        archive: File,
        destination: File,
        onProgress: (ExtractProgress) -> Unit = {},
    ): Unit = withContext(Dispatchers.IO) {
        Files.createDirectories(destination.toPath())
        val writer = EntryWriter(destination.toPath().toRealPath())
        val totalBytes = archive.length()
        CountingInputStream(archive.inputStream().buffered(BUFFER_SIZE)).use { counting ->
            TarArchiveInputStream(GZIPInputStream(counting, BUFFER_SIZE), "UTF-8").use { tar ->
                var entries = 0
                while (true) {
                    ensureActive()
                    val entry = tar.nextEntry ?: break
                    writer.write(entry, tar)
                    entries++
                    onProgress(ExtractProgress(entries, counting.count, totalBytes))
                }
            }
        }
    }
}

private class EntryWriter(private val root: Path) {
    private var lastSafeDir: Path? = null

    fun write(entry: TarArchiveEntry, data: InputStream) {
        val target = resolve(entry.name) ?: return
        when {
            entry.isDirectory -> {
                ensureSafeDirectory(target)
                setMode(target, entry.mode or OWNER_RWX)
            }
            entry.isSymbolicLink -> {
                ensureSafeDirectory(target.parent)
                Files.deleteIfExists(target)
                Files.createSymbolicLink(target, target.fileSystem.getPath(entry.linkName))
            }
            entry.isLink -> {
                val source = resolve(entry.linkName)
                    ?: throw IOException("Archive has a hard link to nothing: ${entry.name}")
                ensureSafeDirectory(target.parent)
                Files.deleteIfExists(target)
                Files.copy(source, target, LinkOption.NOFOLLOW_LINKS)
                if (!Files.isSymbolicLink(target)) setMode(target, entry.mode or OWNER_RW)
            }
            entry.isCharacterDevice || entry.isBlockDevice || entry.isFIFO -> Unit
            entry.isFile -> {
                ensureSafeDirectory(target.parent)
                Files.deleteIfExists(target)
                Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                    data.copyTo(it, BUFFER_SIZE)
                }
                setMode(target, entry.mode or OWNER_RW)
            }
        }
    }

    /** Maps an archive path into [root]; null for the root itself. */
    private fun resolve(name: String): Path? {
        val relative = name.trimStart('/')
        if (relative.isEmpty()) return null
        val path = root.resolve(relative).normalize()
        if (path == root) return null
        if (!path.startsWith(root)) throw IOException("Archive entry points outside its folder: $name")
        return path
    }

    /** Creates [dir], refusing to create or write anything through a symlink that leads out of [root]. */
    private fun ensureSafeDirectory(dir: Path) {
        if (dir == lastSafeDir) return
        var existing = dir
        while (!Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
            existing = existing.parent ?: throw IOException("Folder has no parent: $dir")
        }
        if (!existing.toRealPath().startsWith(root)) {
            throw IOException("Archive entry points outside its folder: $dir")
        }
        Files.createDirectories(dir)
        lastSafeDir = dir
    }

    private fun setMode(path: Path, mode: Int) {
        Files.setPosixFilePermissions(path, permissionsOf(mode))
    }

    private companion object {
        const val OWNER_RW = 0x180 // 0600
        const val OWNER_RWX = 0x1C0 // 0700

        /** setuid, setgid, and sticky bits are dropped: only the 9 permission bits apply. */
        fun permissionsOf(mode: Int): Set<PosixFilePermission> {
            val permissions = EnumSet.noneOf(PosixFilePermission::class.java)
            // Declared OWNER_READ first through OTHERS_EXECUTE last, matching mode bits 0400 down to 0001.
            PosixFilePermission.values().forEachIndexed { index, permission ->
                if (mode and (1 shl (8 - index)) != 0) permissions += permission
            }
            return permissions
        }
    }
}

private class CountingInputStream(input: InputStream) : FilterInputStream(input) {
    var count = 0L
        private set

    override fun read(): Int = super.read().also { if (it >= 0) count++ }

    override fun read(b: ByteArray, off: Int, len: Int): Int =
        super.read(b, off, len).also { if (it > 0) count += it }

    override fun skip(n: Long): Long = super.skip(n).also { count += it }
}
