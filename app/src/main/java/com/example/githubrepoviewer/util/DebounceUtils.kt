package com.example.githubrepoviewer.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun debounce(
    scope: CoroutineScope,
    delayMillis: Long = 500L,
    currentJob: Job?,
    action: suspend () -> Unit
): Job {
    currentJob?.cancel()
    return scope.launch {
        delay(delayMillis)
        action()
    }
}
