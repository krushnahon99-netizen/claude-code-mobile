package com.claudecodemobile.linux

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** A running proot process. Stopping it also ends everything it started (`--kill-on-exit`). */
class LinuxProcess private constructor(private val process: Process) {
    val stdin: OutputStream get() = process.outputStream
    val stdout: InputStream get() = process.inputStream
    val stderr: InputStream get() = process.errorStream
    val isAlive: Boolean get() = process.isAlive

    suspend fun awaitExit(): Int = runInterruptible(Dispatchers.IO) { process.waitFor() }

    /** Sends SIGTERM to proot, which then kills every process inside. */
    fun stop() = process.destroy()

    companion object {
        fun start(spec: ProcessSpec): LinuxProcess {
            val builder = ProcessBuilder(spec.argv)
            builder.environment().apply {
                clear()
                putAll(spec.environment)
            }
            return LinuxProcess(builder.start())
        }
    }
}

/** How a finished command went: its exit code and the last lines it printed. */
data class CommandResult(val exitCode: Int, val outputTail: List<String>) {
    val succeeded: Boolean get() = exitCode == 0
}

/**
 * Runs [spec] to completion with stdin closed. Every stdout and stderr line goes to [onLine],
 * one call at a time. Cancelling the caller stops the process.
 */
suspend fun runToCompletion(
    spec: ProcessSpec,
    tailLines: Int = 200,
    onLine: (String) -> Unit = {},
): CommandResult = coroutineScope {
    val process = LinuxProcess.start(spec)
    val tail = ArrayDeque<String>(tailLines)
    val lock = Any()
    val record = { line: String ->
        synchronized(lock) {
            if (tail.size == tailLines) tail.removeFirst()
            tail.addLast(line)
            onLine(line)
        }
    }
    try {
        process.stdin.close()
        val readers = listOf(process.stdout, process.stderr).map { stream ->
            launch(Dispatchers.IO) { stream.forEachLineQuietly(record) }
        }
        val exitCode = process.awaitExit()
        readers.joinAll()
        CommandResult(exitCode, synchronized(lock) { tail.toList() })
    } finally {
        process.stop()
    }
}

private fun InputStream.forEachLineQuietly(action: (String) -> Unit) {
    try {
        bufferedReader().forEachLine(action)
    } catch (_: IOException) {
        // The pipe closes when the process is stopped.
    }
}
