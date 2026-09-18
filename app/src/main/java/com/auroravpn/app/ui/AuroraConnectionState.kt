package com.auroravpn.app.ui

/**
 * What the connection screen shows.
 *
 * Every case maps 1:1 from an [com.whitedns.whiteaesther.service.EngineStage] the
 * engine itself reported. [Connected] is only reachable through the engine's own
 * onNativeReady callback, so the UI cannot draw a connection that did not happen.
 */
sealed interface AuroraConnectionState {
    /** Nothing running. The engine has not been asked anything. */
    data object Idle : AuroraConnectionState

    /** The engine is resolving an identity and hunting for an endpoint. */
    data class Preparing(val message: String) : AuroraConnectionState

    /** An endpoint was chosen; the handshake is in progress. */
    data class Connecting(val message: String) : AuroraConnectionState

    /**
     * The tunnel is carrying traffic.
     *
     * [transport] is read from the settings the session was started with, and
     * [startedAt] from the status's own timestamp, so duration survives the screen
     * being rotated or the process being recreated.
     */
    data class Connected(
        val peer: String,
        val transport: String,
        val startedAt: Long,
    ) : AuroraConnectionState

    /** The user asked to stop; the engine is unwinding the session. */
    data class Stopping(val message: String) : AuroraConnectionState

    /** The engine reported a failure. [message] is its own, verbatim. */
    data class Failed(val message: String) : AuroraConnectionState
}

/** True when the tunnel is up and carrying traffic. */
val AuroraConnectionState.isConnected: Boolean
    get() = this is AuroraConnectionState.Connected

/** True for any state where the engine is working and the user should not tap again. */
val AuroraConnectionState.isBusy: Boolean
    get() = this is AuroraConnectionState.Preparing ||
        this is AuroraConnectionState.Connecting ||
        this is AuroraConnectionState.Stopping
