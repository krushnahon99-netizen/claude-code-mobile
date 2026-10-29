package com.claudecodemobile.linux

/** A process to start on the Android side: its full argument list and its complete environment. */
data class ProcessSpec(val argv: List<String>, val environment: Map<String, String>)

/** Makes [hostPath] visible inside Linux at [guestPath]. */
data class Bind(val hostPath: String, val guestPath: String = hostPath) {
    val argument: String
        get() = if (hostPath == guestPath) "--bind=$hostPath" else "--bind=$hostPath:$guestPath"
}

/**
 * Builds the proot command that runs a shell command inside Ubuntu. Callers' variables,
 * secrets included, travel in the process environment and never on the command line.
 */
object ProotCommand {
    const val GUEST_PATH = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

    /** proot needs these on the Android side; `env -u` removes them before the command starts. */
    val HOST_ONLY_VARIABLES = listOf(
        "LD_LIBRARY_PATH",
        "PROOT_LOADER",
        "PROOT_TMP_DIR",
        "PROOT_L2S_DIR",
        "PROOT_NO_SECCOMP",
    )

    /** The whole environment a Linux command starts with, before the caller's variables. */
    val GUEST_ENVIRONMENT = mapOf(
        "HOME" to "/root",
        "USER" to "root",
        "LOGNAME" to "root",
        "SHELL" to "/bin/bash",
        "PATH" to GUEST_PATH,
        "LANG" to "C.UTF-8",
        "TERM" to "dumb",
        "TMPDIR" to "/tmp",
        // libuv's io_uring file I/O would bypass proot's path translation.
        "UV_USE_IO_URING" to "0",
    )

    fun build(
        paths: LinuxPaths,
        command: String,
        workingDir: String = "/root",
        environment: Map<String, String> = emptyMap(),
        extraBinds: List<Bind> = emptyList(),
        disableSeccomp: Boolean = false,
    ): ProcessSpec {
        val binds = listOf(
            Bind("/dev"),
            Bind("/proc"),
            Bind("/sys"),
            Bind("/dev/urandom", "/dev/random"),
            Bind(paths.shm.path, "/dev/shm"),
        ) + extraBinds

        val argv = buildList {
            add(paths.proot.path)
            add("--kill-on-exit")
            add("--link2symlink")
            add("--sysvipc")
            add("-L")
            add("--root-id")
            add("--rootfs=${paths.rootfs.path}")
            add("--cwd=$workingDir")
            binds.forEach { add(it.argument) }
            add("/usr/bin/env")
            HOST_ONLY_VARIABLES.forEach {
                add("-u")
                add(it)
            }
            add("/bin/bash")
            add("-lc")
            add(command)
        }

        val env = buildMap {
            putAll(GUEST_ENVIRONMENT)
            putAll(environment)
            put("LD_LIBRARY_PATH", paths.libraryDir.path)
            put("PROOT_LOADER", paths.prootLoader.path)
            put("PROOT_TMP_DIR", paths.prootTmp.path)
            put("PROOT_L2S_DIR", paths.linkStore.path)
            if (disableSeccomp) put("PROOT_NO_SECCOMP", "1")
        }

        return ProcessSpec(argv, env)
    }
}
