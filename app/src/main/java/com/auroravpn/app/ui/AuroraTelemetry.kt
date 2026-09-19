package com.auroravpn.app.ui

import androidx.lifecycle.viewModelScope
import com.whitedns.whiteaesther.MainViewModel
import com.whitedns.whiteaesther.service.EngineStage
import com.whitedns.whiteaesther.service.EngineStatus
import com.whitedns.whiteaesther.service.TrafficMeter
import com.whitedns.whiteaesther.service.TrafficSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * The telemetry the Home screen draws, derived from WAM's own publications.
 *
 * The waveform needs a *history*, and the backend publishes an *instant*. This
 * holds the bounded window between the two: every sample WAM emits is appended,
 * the oldest is dropped, and nothing here is generated when nothing arrives.
 * The window is empty until real traffic is reported, and it is cleared the
 * moment a session ends.
 *
 * This is UI-only state. It invents no measurement; it remembers the ones the
 * engine already made.
 */
class AuroraTelemetry(private val wam: AuroraViewModel) {

    /** How many samples the waveform keeps. Bounded, so it cannot grow. */
    private val window = 60

    private val mutableDownHistory = MutableStateFlow<List<Float>>(emptyList())
    private val mutableUpHistory = MutableStateFlow<List<Float>>(emptyList())

    /** Downstream samples in [0, 1], oldest first. Empty until traffic arrives. */
    val downHistory: StateFlow<List<Float>> = mutableDownHistory.asStateFlow()

    /** Upstream samples in [0, 1], oldest first. */
    val upHistory: StateFlow<List<Float>> = mutableUpHistory.asStateFlow()

    /** The peak this session has seen, which is what the scale is. */
    private val mutablePeakBps = MutableStateFlow(1L)
    val peakBps: StateFlow<Long> = mutablePeakBps.asStateFlow()

    init {
        // Sampling the flow is what turns the engine's instant into a series.
        wam.viewModelScope.launch {
            wam.traffic.collect { sample ->
                if (!sample.supported) return@collect
                val down = sample.downloadPerSecond
                val up = sample.uploadPerSecond
                if (down > mutablePeakBps.value) mutablePeakBps.value = down
                if (up > mutablePeakBps.value) mutablePeakBps.value = up
                val scale = mutablePeakBps.value.coerceAtLeast(1_000L).toFloat()
                mutableDownHistory.value = (mutableDownHistory.value + (down / scale))
                    .takeLast(window)
                mutableUpHistory.value = (mutableUpHistory.value + (up / scale))
                    .takeLast(window)
            }
        }
    }

    /** Clears the window. Called when a session ends, so a new one starts empty. */
    fun reset() {
        mutableDownHistory.value = emptyList()
        mutableUpHistory.value = emptyList()
        mutablePeakBps.value = 1L
    }

    /** Bytes per second to a rate string. Bits are not bytes and never meet. */
    fun rateString(bytesPerSecond: Long): String = formatRate(bytesPerSecond)

    companion object {
        /** Bytes, as a total: 18.2 GB */
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var value = bytes.toDouble()
            var unit = 0
            while (value >= 1024.0 && unit < units.lastIndex) {
                value /= 1024.0
                unit++
            }
            return if (unit == 0) {
                String.format(Locale.ROOT, "%d B", bytes)
            } else {
                String.format(Locale.ROOT, "%.1f %s", value, units[unit])
            }
        }

        /** Bytes per second as a rate: 86 KB/s */
        fun formatRate(bytesPerSecond: Long): String {
            if (bytesPerSecond <= 0) return "0 B/s"
            val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
            var value = bytesPerSecond.toDouble()
            var unit = 0
            while (value >= 1024.0 && unit < units.lastIndex) {
                value /= 1024.0
                unit++
            }
            return if (unit == 0) {
                String.format(Locale.ROOT, "%d B/s", bytesPerSecond)
            } else {
                String.format(Locale.ROOT, "%.1f %s", value, units[unit])
            }
        }
    }
}
