package com.claudecodemobile.linux

import java.io.File
import java.io.FileInputStream

/**
 * Android hides some /proc entries from apps, but Linux programs expect them (Node.js reads
 * /proc/stat for `os.cpus()`). Readable stand-ins are bound over the hidden ones, the way
 * Termux's proot-distro does it.
 */
object FakeProc {
    val GUEST_PATHS = listOf(
        "/proc/loadavg",
        "/proc/stat",
        "/proc/uptime",
        "/proc/version",
        "/proc/vmstat",
        "/proc/sys/kernel/cap_last_cap",
        "/proc/sys/fs/inotify/max_user_watches",
        "/proc/sys/kernel/overflowuid",
        "/proc/sys/kernel/overflowgid",
    )

    fun fileFor(dir: File, guestPath: String) =
        File(dir, guestPath.removePrefix("/proc/").replace('/', '_'))

    fun write(dir: File, cpuCount: Int, kernelRelease: String) {
        dir.mkdirs()
        for ((guestPath, content) in contents(cpuCount, kernelRelease)) {
            fileFor(dir, guestPath).writeText(content)
        }
    }

    /** Binds a stand-in for every entry Android hides, once [write] has created them. */
    fun binds(dir: File, hiddenGuestPaths: List<String>): List<Bind> =
        hiddenGuestPaths.mapNotNull { guestPath ->
            fileFor(dir, guestPath).takeIf { it.isFile }?.let { Bind(it.path, guestPath) }
        }

    fun isReadable(path: String): Boolean = try {
        FileInputStream(path).use { it.read() }
        true
    } catch (_: Exception) {
        false
    }

    internal fun contents(cpuCount: Int, kernelRelease: String): Map<String, String> = mapOf(
        "/proc/loadavg" to "0.12 0.07 0.02 2/165 765\n",
        "/proc/stat" to stat(cpuCount.coerceAtLeast(1)),
        "/proc/uptime" to "124.08 932.80\n",
        "/proc/version" to "Linux version $kernelRelease (proot@claude-code-mobile) #1 SMP PREEMPT\n",
        "/proc/vmstat" to VMSTAT,
        "/proc/sys/kernel/cap_last_cap" to "40\n",
        "/proc/sys/fs/inotify/max_user_watches" to "8192\n",
        "/proc/sys/kernel/overflowuid" to "65534\n",
        "/proc/sys/kernel/overflowgid" to "65534\n",
    )

    /** One `cpuN` row per core: user nice system idle iowait irq softirq steal guest guest_nice. */
    private fun stat(cpuCount: Int) = buildString {
        appendLine("cpu  ${300 * cpuCount} 0 ${300 * cpuCount} ${12000 * cpuCount} 30 10 5 0 0 0")
        repeat(cpuCount) { appendLine("cpu$it 300 0 300 12000 30 10 5 0 0 0") }
        appendLine("intr 0")
        appendLine("ctxt 140223")
        appendLine("btime 1680020856")
        appendLine("processes 772")
        appendLine("procs_running 2")
        appendLine("procs_blocked 0")
        appendLine("softirq 0 0 0 0 0 0 0 0 0 0 0")
    }

    private val VMSTAT = listOf(
        "nr_free_pages 136032",
        "nr_zone_inactive_anon 0",
        "nr_zone_active_anon 0",
        "nr_zone_inactive_file 0",
        "nr_zone_active_file 0",
        "nr_mlock 0",
        "nr_dirty 0",
        "nr_writeback 0",
        "pgpgin 0",
        "pgpgout 0",
        "pswpin 0",
        "pswpout 0",
        "pgfault 0",
        "pgmajfault 0",
    ).joinToString(separator = "\n", postfix = "\n")
}
