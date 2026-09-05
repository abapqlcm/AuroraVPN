package io.github.abapqlcm.auroravpn.shared.core

import java.net.ServerSocket

object SingleInstanceLock {
    @Volatile
    var socket: ServerSocket? = null
    fun release() {
        runCatching { socket?.close() }
        socket = null
    }
}
