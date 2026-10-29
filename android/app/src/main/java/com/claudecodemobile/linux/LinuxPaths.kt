package com.claudecodemobile.linux

import java.io.File

/** Where the Linux environment lives on the phone. Everything is app-private. */
class LinuxPaths(nativeLibraryDir: File, filesDir: File, cacheDir: File) {
    /** Android extracts the APK's jniLibs here, the only place an app may execute files from. */
    val libraryDir: File = nativeLibraryDir
    val proot = File(nativeLibraryDir, "libproot.so")
    val prootLoader = File(nativeLibraryDir, "libproot-loader.so")

    val home = File(filesDir, "linux")
    val rootfs = File(home, "rootfs")

    /** Extraction happens here and is renamed to [rootfs] only when it finishes. */
    val rootfsPartial = File(home, "rootfs.partial")
    val state = File(home, "state")
    val downloads = File(home, "downloads")
    val fakeProc = File(home, "fake-proc")
    val shm = File(home, "shm")
    val prootTmp = File(cacheDir, "proot-tmp")

    /** `--link2symlink` turns hard links into symlinks to files kept here. */
    val linkStore = File(rootfs, ".l2s")
}
