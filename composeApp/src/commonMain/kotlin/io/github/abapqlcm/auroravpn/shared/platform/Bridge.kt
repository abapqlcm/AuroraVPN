package io.github.abapqlcm.auroravpn.shared.platform

import io.github.abapqlcm.auroravpn.shared.model.ConnectionStatus
import io.github.abapqlcm.auroravpn.shared.model.SessionTraffic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface BridgeContract {
    val statusOverride: StateFlow<ConnectionStatus?>
    val trafficOverride: StateFlow<SessionTraffic?>
    val elapsedOverride: StateFlow<Long?>
    val isWaitingForCode: StateFlow<Boolean?>
}

object Bridge : BridgeContract {
    var submitLoginCode: ((String) -> Unit)? = null

    private val _statusOverride = MutableStateFlow<ConnectionStatus?>(null)
    override val statusOverride: StateFlow<ConnectionStatus?> = _statusOverride
    private val _trafficOverride = MutableStateFlow<SessionTraffic?>(null)
    override val trafficOverride: StateFlow<SessionTraffic?> = _trafficOverride
    private val _elapsedOverride = MutableStateFlow<Long?>(null)
    override val elapsedOverride: StateFlow<Long?> = _elapsedOverride
    private val _isWaitingForCode = MutableStateFlow<Boolean?>(null)
    override val isWaitingForCode: StateFlow<Boolean?> = _isWaitingForCode

    // internal mutable access for platform layers
    fun mutableStatus(): MutableStateFlow<ConnectionStatus?> = _statusOverride
    fun mutableTraffic(): MutableStateFlow<SessionTraffic?> = _trafficOverride
    fun mutableElapsed(): MutableStateFlow<Long?> = _elapsedOverride
    fun mutableWaiting(): MutableStateFlow<Boolean?> = _isWaitingForCode

    var pickFile: ((onResult: (String?) -> Unit) -> Unit)? = null
    var saveFile: ((fileName: String, content: String, onResult: (Boolean) -> Unit) -> Unit)? = null
}
