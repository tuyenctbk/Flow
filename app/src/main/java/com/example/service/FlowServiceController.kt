package com.example.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FlowServiceController {
    private val _cancelSessionRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cancelSessionRequests = _cancelSessionRequests.asSharedFlow()

    fun requestCancelSession() {
        _cancelSessionRequests.tryEmit(Unit)
    }
}
