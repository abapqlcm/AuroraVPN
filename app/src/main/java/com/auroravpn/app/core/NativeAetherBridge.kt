package com.auroravpn.app.core

/**
 * The JNI surface of the Aether engine.
 *
 * Every name here must match a `Java_com_auroravpn_app_core_NativeAetherBridge_*`
 * function in native/android-bridge/src/lib.rs. The two are a single interface
 * split across a language boundary; changing one without the other fails to
 * link, which is the useful kind of failure — it shows up at build time.
 *
 * The engine is not a subprocess. It lives in libauroravpn_core.so inside the
 * APK and speaks JNI directly, which is what keeps it working on Android 14+
 * where executing a binary out of app-private storage is no longer allowed.
 */
object NativeAetherBridge {

    val loadResult: Result<Unit> by lazy {
        runCatching { System.loadLibrary("auroravpn_core") }
    }

    val isLoaded: Boolean
        get() = loadResult.isSuccess

    fun versionOrNull(): String? =
        loadResult.mapCatching { nativeVersion() }.getOrNull()

    fun drainLog(): List<String> {
        if (!isLoaded) return emptyList()
        return runCatching { nativeDrainLog()?.filterNotNull().orEmpty() }
            .getOrDefault(emptyList())
    }

    /**
     * Resolves the route: provisions an identity if none exists, scans for a
     * reachable endpoint, validates it. Blocks on the engine's tokio runtime,
     * so the caller must be off the main thread.
     *
     * Returns the engine's own report of the tunnel's addresses and the peer it
     * chose, or the engine's error message when it could not resolve one.
     */
    fun prepare(configJson: String): Result<PreparedEngine> = call {
        nativePrepare(configJson)
    }.fold(
        onSuccess = { raw ->
            runCatching {
                val json = org.json.JSONObject(raw)
                check(json.optBoolean("ok")) {
                    json.optString("error", "Preparation failed")
                }
                PreparedEngine(
                    ipv4 = json.getString("ipv4"),
                    ipv6 = json.getString("ipv6"),
                    peer = json.getString("peer"),
                )
            }
        },
        onFailure = { Result.failure(it) },
    )

    /**
     * Runs the tunnel for the lifetime of the session. Blocks until the session
     * ends — either because [stop] was called or the tunnel failed — so the
     * caller must never be on the main thread.
     *
     * [listener] receives [NativeEngineListener.onNativeReady] once the tunnel
     * is established; that callback is the only signal that the connection is
     * actually live.
     */
    fun run(
        configJson: String,
        preparedPeer: String,
        tunFd: Int,
        listener: NativeEngineListener?,
    ): Result<String> = call {
        nativeRun(configJson, preparedPeer, tunFd, listener)
    }.fold(
        onSuccess = { raw ->
            runCatching {
                val json = org.json.JSONObject(raw)
                check(json.optBoolean("ok")) {
                    json.optString("error", "Engine run failed")
                }
                raw
            }
        },
        onFailure = { Result.failure(it) },
    )

    fun setSocketProtector(protector: NativeSocketProtector?) {
        if (!isLoaded) return
        nativeSetSocketProtector(protector)
    }

    fun stop(): Boolean = isLoaded && nativeStop()

    /**
     * The exception a call to the native side reports when it cannot even run.
     * Distinct from [NativeResult.error], which is the engine's own message for
     * a call that ran and came back with something to say.
     */
    private inline fun <T> call(block: () -> T): Result<T> = runCatching(block)

    private fun <T> Result<T>.toNativeResult(): NativeResult = fold(
        onSuccess = { NativeResult(ok = true) },
        onFailure = { NativeResult(ok = false, error = it.message) },
    )

    private fun <T> Result<T>.toResult(): NativeResult = toNativeResult()

    @JvmStatic
    private external fun nativeVersion(): String

    @JvmStatic
    private external fun validateConfig(configJson: String): String

    @JvmStatic
    private external fun nativeDrainLog(): Array<String?>?

    @JvmStatic
    private external fun nativeExportIdentity(configPath: String): String

    @JvmStatic
    private external fun nativeImportIdentity(configPath: String, payload: String): String

    @JvmStatic
    private external fun nativePrepare(configJson: String): String

    @JvmStatic
    private external fun nativeProvision(configJson: String): String

    @JvmStatic
    private external fun nativeScan(configJson: String): String

    @JvmStatic
    private external fun nativeTestEndpoint(configJson: String): String

    @JvmStatic
    private external fun nativeCancelScan(): Boolean

    @JvmStatic
    private external fun nativeCancelPrepare(): Boolean

    @JvmStatic
    private external fun nativeRun(
        configJson: String,
        preparedPeer: String,
        tunFd: Int,
        listener: NativeEngineListener?,
    ): String

    @JvmStatic
    private external fun nativeStop(): Boolean

    @JvmStatic
    private external fun nativeSetSocketProtector(protector: NativeSocketProtector?)
}

fun interface NativeSocketProtector {
    fun protectSocket(fd: Int): Boolean
}

fun interface NativeEngineListener {
    fun onNativeReady()
}

data class PreparedEngine(
    val ipv4: String,
    val ipv6: String,
    val peer: String,
)

data class NativeResult(
    val ok: Boolean,
    val error: String? = null,
)
