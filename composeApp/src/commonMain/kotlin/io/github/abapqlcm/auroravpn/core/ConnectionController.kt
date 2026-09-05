package io.github.abapqlcm.auroravpn.shared.core

import io.github.abapqlcm.auroravpn.platform.PlatformContext
import io.github.abapqlcm.auroravpn.shared.model.ConnectionStatus
import io.github.abapqlcm.auroravpn.shared.model.SessionTraffic
import kotlinx.coroutines.flow.StateFlow

expect object ConnectionController {
    val status: StateFlow<ConnectionStatus>
    val elapsedSeconds: StateFlow<Long>
    val sessionTraffic: StateFlow<SessionTraffic>
    val isWaitingForCode: StateFlow<Boolean>
    fun getInstance(context: PlatformContext)
    fun markStatus(status: ConnectionStatus)
}
