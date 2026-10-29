package com.claudecodemobile.linux

import java.io.File
import java.nio.file.Files

/** Files written into Ubuntu after unpacking, so networking and apt work under proot. */
object GuestConfig {
    const val RESOLV_CONF = "nameserver 1.1.1.1\nnameserver 8.8.8.8\n"
    const val HOSTS = "127.0.0.1 localhost\n::1 localhost ip6-localhost ip6-loopback\n"

    // proot fakes root but can't switch users, so apt must not drop to its _apt sandbox user.
    const val APT_CONF = "APT::Sandbox::User \"root\";\nAcquire::Retries \"3\";\n"

    fun apply(rootfs: File) {
        writeGuestFile(rootfs, "etc/resolv.conf", RESOLV_CONF)
        writeGuestFile(rootfs, "etc/hosts", HOSTS)
        writeGuestFile(rootfs, "etc/apt/apt.conf.d/99claude-code-mobile", APT_CONF)
        File(rootfs, "root/projects").mkdirs()
        File(rootfs, "tmp").mkdirs()
    }

    /** Replaces a file inside Linux. An existing symlink (Ubuntu links resolv.conf into /run) is replaced, not followed. */
    fun writeGuestFile(rootfs: File, relativePath: String, content: String) {
        val target = rootfs.toPath().resolve(relativePath)
        Files.createDirectories(target.parent)
        Files.deleteIfExists(target)
        Files.write(target, content.toByteArray())
    }
}
