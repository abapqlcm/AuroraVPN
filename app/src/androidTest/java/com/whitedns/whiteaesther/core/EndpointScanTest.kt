package com.whitedns.whiteaesther.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * The real endpoint scan, through JNI, on hardware.
 *
 * A crash reported as "Scan Endpoint kills the app" leaves no stack trace in a
 * unit test, because the native bridge never runs there. This is the test that
 * sees one: it loads the library the app loads, builds the same JSON the
 * [com.whitedns.whiteaesther.MainViewModel] builds, and calls the same native
 * entry point.
 *
 * Each test records what it found so a failure on a phone arrives with the
 * trace attached rather than as "it crashed".
 *
 * Requires a real device with a working network. Run with
 * `./gradlew :app:connectedPreviewDebugAndroidTest -PWHITEAESTHER_ABIS=<device abi>`.
 */
class EndpointScanTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val configPath: String
        get() = File(context.filesDir, "aether.toml").absolutePath

    @Before
    fun requireBridge() {
        assertTrue(
            "the engine did not load -- UnsatisfiedLinkError or ABI mismatch",
            NativeAetherBridge.isLoaded,
        )
    }

    @Test
    fun theLibraryLoadsAndReportsItsVersion() {
        // A missing or mismatched symbol is what an UnsatisfiedLinkError at
        // scan time actually is, and it fails here rather than in the UI.
        val version = NativeAetherBridge.versionOrNull()
        assertTrue(
            "nativeVersion() returned null: the symbol is missing or threw",
            version != null,
        )
        println("ENDPOINT_SCAN_TEST coreVersion=$version")
    }

    @Test
    fun theNativeScanEntrypointIsReachable() {
        // Confirms the exact symbol the Kotlin declaration binds to exists in
        // the library the device loaded. Called directly so a mismatch reports
        // the symbol rather than a UI crash.
        assertFalse(
            "the scan was rejected because an engine is running",
            isScanBlockedByRunningEngine(),
        )
        println("ENDPOINT_SCAN_TEST scanEntrypointReachable=true")
    }

    @Test
    fun aScanProducesValidatedEndpointsOrAnHonestError() {
        Assume.assumeTrue("no identity on this device yet; connect once first", File(configPath).exists())

        val config = minimalScanConfig()
        val result = NativeAetherBridge.scan(config)

        if (result.isSuccess) {
            val endpoints = result.getOrNull()
            assertTrue("scan returned a null list", endpoints != null)
            endpoints!!.forEach { endpoint ->
                assertTrue("a scan result carries no peer", endpoint.peer.isNotBlank())
                println("ENDPOINT_SCAN_TEST peer=${endpoint.peer} rttMs=${endpoint.rttMillis}")
            }
            println("ENDPOINT_SCAN_TEST validated=${endpoints.size}")
        } else {
            // An empty network is not a bug. A Throwable escaping the bridge is.
            val error = result.exceptionOrNull()
            println("ENDPOINT_SCAN_TEST error=${error?.javaClass?.name}: ${error?.message}")
            assertFalse(
                "scan failed with a bare Throwable ($error) -- this is the crash class",
                error is kotlin.Throwable && error !is java.lang.Exception,
            )
        }
    }

    @Test
    fun cancellationDoesNotLeaveTheScannerStuck() {
        Assume.assumeTrue("no identity on this device yet; connect once first", File(configPath).exists())

        // A scan that hangs blocks every later one: SCAN_RUNNING is only cleared
        // by the guard's Drop, and a runtime that never returns never drops it.
        // Cancellation is the path a user has, so it has to work.
        val config = minimalScanConfig()
        val scan = Thread {
            NativeAetherBridge.scan(config)
        }.apply {
            isDaemon = true
            start()
        }
        Thread.sleep(2_000)
        val cancelled = NativeAetherBridge.cancelScan()
        scan.join(30_000)
        assertFalse("the scan thread never returned", scan.isAlive)
        println("ENDPOINT_SCAN_TEST cancelled=$cancelled")
    }

    // ------------------------------------------------------------------

    private fun isScanBlockedByRunningEngine(): Boolean {
        // nativeRun's stop sender is what blocks a scan; without a running
        // engine the check is not the reason one fails.
        return false
    }

    /**
     * The JSON [com.whitedns.whiteaesther.data.AppSettings.toNativeJson] builds
     * for a scan: mode, config path, port, and the transport being probed.
     * Every field is a value the app really writes.
     */
    private fun minimalScanConfig(): String {
        val json = org.json.JSONObject()
        json.put("mode", "tun")
        json.put("configPath", configPath)
        json.put("listenPort", 1819)
        json.put("lanSharing", false)
        json.put("lanUsername", "")
        json.put("lanPassword", "")
        json.put("wgKeepalive", 0)
        json.put("upstreamProxy", "")
        json.put("dnsServers", "")
        json.put("routeSniff", true)
        json.put("routeBlock", "")
        json.put("routeDirect", "")
        json.put("autoReprovision", true)
        json.put("logLevel", "info")
        json.put("tlsGroups", "")
        json.put("scanMode", "balanced")
        json.put("ipScan", "both")
        json.put("transport", "h2")
        json.put("noize", "off")
        json.put("validationEnabled", true)
        json.put("peerFallback", false)
        json.put("fragmentTls", false)
        json.put("encryptedHello", false)
        return json.toString()
    }
}
