package com.auroravpn.app.ui

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.whitedns.whiteaesther.MainViewModel
import com.whitedns.whiteaesther.core.EndpointScanResult
import com.whitedns.whiteaesther.AddressPair
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.service.EngineLog
import com.whitedns.whiteaesther.service.EngineStage
import com.whitedns.whiteaesther.service.EngineStatus
import com.whitedns.whiteaesther.service.EngineStatusStore
import com.whitedns.whiteaesther.service.LogEntry
import com.whitedns.whiteaesther.service.TrafficMeter
import com.whitedns.whiteaesther.service.TrafficSample
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The whole of Aurora's own state logic.
 *
 * Every field here is a mapping of something WAM already publishes. Nothing is held
 * locally, nothing is defaulted to a nice value, nothing is invented: if the engine has
 * not said it, the UI shows it has not said it. See docs/CAPABILITY_CONTRACT.md for the
 * trace of each one.
 *
 * This class deliberately does not touch [MainViewModel] internals beyond its public
 * state and actions, so WAM stays the source of truth and stays unmodified.
 */
class AuroraViewModel(private val wam: MainViewModel) : ViewModel() {

    val settings: StateFlow<AppSettings> = wam.settings
    val settingsLoaded: StateFlow<Boolean> = wam.settingsLoaded
    val traffic: StateFlow<TrafficSample> = wam.traffic
    val addresses: StateFlow<AddressPair> = wam.addresses
    val engineStatus: StateFlow<EngineStatus> = wam.engineStatus
    val endpointScannerState = wam.endpointScannerState
    val chainState = wam.chainState
    val psiphonRegions = wam.psiphonRegions
    val bridgesFetching = wam.bridgesFetching
    val bridgesMessage = wam.bridgesMessage
    val identityMessage = wam.identityMessage
    val update = wam.update

    /** The engine's own version string, or null when the library is not loaded. */
    val engineVersion: StateFlow<String?> = wam.settings
        .map { com.whitedns.whiteaesther.core.NativeAetherBridge.versionOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** The live log buffer the engine and the service both write into. */
    val logs: StateFlow<List<LogEntry>> = EngineLog.entries

    /**
     * The connection state as the UI draws it.
     *
     * Derived from [EngineStatus.stage] and nothing else: CONNECTED is only reached when
     * the engine's own [com.whitedns.whiteaesther.core.NativeEngineListener] fired, so
     * this can never report a connection the backend did not make.
     */
    val connection: StateFlow<AuroraConnectionState> = wam.engineStatus
        .map { it.toAurora() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = wam.engineStatus.value.toAurora(),
        )

    /** Whether the big button does anything right now. */
    val canToggleConnection: Boolean
        get() = connection.value.let { state ->
            state is AuroraConnectionState.Idle || state is AuroraConnectionState.Failed
        }

    // ---- Actions. Each one forwards to WAM; none implements its own backend. ----

    fun save(settings: AppSettings) = wam.save(settings)
    fun captureRealAddressIfIdle() = wam.captureRealAddressIfIdle()
    fun scanEndpoints(settings: AppSettings) = wam.scanEndpoints(settings)
    fun testEndpoint(settings: AppSettings) = wam.testEndpoint(settings)
    fun cancelEndpointScan() = wam.cancelEndpointScan()
    fun resetEndpoint(settings: AppSettings) = wam.resetEndpoint(settings)
    fun exportIdentity(): String? = wam.exportIdentity()
    fun importIdentity(payload: String) = wam.importIdentity(payload)
    fun reportIdentityWrite(error: String?) = wam.reportIdentityWrite(error)
    fun clearIdentityMessage() = wam.clearIdentityMessage()
    fun detectedCountry(): String = wam.detectedCountry()
    fun fetchBridges(settings: AppSettings, country: String) = wam.fetchBridges(settings, country)
    fun clearBridgesMessage() = wam.clearBridgesMessage()
    fun refreshChainNodes(settings: AppSettings) = wam.refreshChainNodes(settings)
    fun selectChainNode(settings: AppSettings, node: String) = wam.selectChainNode(settings, node)
    fun testChainNodes() = wam.testChainNodes()
    fun testChainNodes(only: List<String>) = wam.testChainNodes(only)
    fun cancelChainTests() = wam.cancelChainTests()
    fun dismissUpdate() = wam.dismissUpdate()

    /** The scan results, newest RTT first. Empty until a scan has been run. */
    val endpointResults: List<EndpointScanResult>
        get() = endpointScannerState.value.results.sortedBy { it.rttMillis }

    private fun EngineStatus.toAurora(): AuroraConnectionState = when (stage) {
        EngineStage.IDLE -> AuroraConnectionState.Idle
        EngineStage.PREPARING -> AuroraConnectionState.Preparing(message)
        EngineStage.CONNECTING -> AuroraConnectionState.Connecting(message)
        EngineStage.CONNECTED -> AuroraConnectionState.Connected(
            peer = peer.orEmpty(),
            transport = settings.value.transport.wireName,
            startedAt = connectedAtMillis ?: 0L,
        )
        EngineStage.STOPPING -> AuroraConnectionState.Stopping(message)
        EngineStage.ERROR -> AuroraConnectionState.Failed(message)
    }

    /**
     * AndroidViewModel factory that hands the application to WAM's [MainViewModel] and
     * wraps it. One instance per activity; the WAM flows underneath survive recreation.
     */
    class Factory(private val app: android.app.Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val wam = MainViewModel(app)
            return AuroraViewModel(wam) as T
        }
    }
}
