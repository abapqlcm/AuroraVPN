package com.auroravpn.app.ui

import androidx.test.platform.app.InstrumentationRegistry
import com.whitedns.whiteaesther.MainViewModel
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.service.EngineStage
import com.whitedns.whiteaesther.service.EngineStatusStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The whole scan path, not just its last hop.
 *
 * [com.whitedns.whiteaesther.core.EndpointScanTest] calls the bridge directly.
 * This one goes through [MainViewModel.scanEndpoints] -- the coroutine, the
 * JSON build, the first-framing decision, the state machine -- because that is
 * the path the Scan button actually takes, and a crash there does not have to
 * come from native code at all.
 *
 * Runs the scan on the calling thread (runBlocking) so a failure cannot vanish
 * into a coroutine that was cancelled when the test tore down. Whatever it is
 * -- an exception, a link error, a hang -- surfaces here.
 */
class AuroraScanPathTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val viewModel by lazy { MainViewModel(context.applicationContext as android.app.Application) }

    @Test
    fun theViewModelScanPathStartsAndReportsAnOutcome() {
        // A scan refused by state (an engine is running) is not a crash, so the
        // precondition is stated rather than assumed.
        val stage = EngineStatusStore.status.value.stage
        Assert.assertEquals(
            "the engine must be idle to scan; it was $stage",
            EngineStage.IDLE,
            stage,
        )

        val settings = runBlocking { viewModel.settings.first() }

        val outcome = runCatching {
            // The coroutine is awaited rather than fired, so the test owns the
            // lifetime and a throw arrives here instead of disappearing.
            runBlocking {
                viewModel.scanEndpoints(settings)
                // scanEndpoints launches its own job and returns immediately;
                // wait for the scanner to leave the SCANNING state before
                // asserting.
                viewModel.endpointScannerState.first { state ->
                    state.operation == null || state.error != null || state.results.isNotEmpty()
                }
            }
        }

        assertTrue(
            "scanEndpoints threw: ${outcome.exceptionOrNull()}",
            outcome.isSuccess,
        )

        val state = viewModel.endpointScannerState.value
        // An empty result set on a blocked network is honest. An exception that
        // escaped the coroutine is what we are looking for.
        println("AURORA_SCAN_TEST operation=${state.operation} results=${state.results.size}")
        println("AURORA_SCAN_TEST message=${state.message}")
        state.error?.let { println("AURORA_SCAN_TEST error=$it") }
        state.results.forEach { result ->
            println("AURORA_SCAN_TEST peer=${result.peer} rttMs=${result.rttMillis}")
            assertTrue("a reported peer is blank", result.peer.isNotBlank())
        }
    }
}
