package com.claudecodemobile.linux

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission

/**
 * Deletes [root] and everything under it. Symlinks are deleted, never followed: a Linux
 * system is full of absolute links like `/usr/bin/awk`, which on Android would point at
 * the phone's own folders.
 */
fun deleteTree(root: File) {
    val start = root.toPath()
    if (!Files.exists(start, LinkOption.NOFOLLOW_LINKS)) return
    Files.walkFileTree(
        start,
        object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                // Linux tools may leave folders read-only; the app owns them and can open them up.
                val permissions = Files.getPosixFilePermissions(dir, LinkOption.NOFOLLOW_LINKS)
                if (!permissions.containsAll(OWNER_ALL)) {
                    Files.setPosixFilePermissions(dir, permissions + OWNER_ALL)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (exc != null) throw exc
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        },
    )
}

private val OWNER_ALL = setOf(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
)
