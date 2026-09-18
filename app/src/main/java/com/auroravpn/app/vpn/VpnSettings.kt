package com.auroravpn.app.vpn

import android.content.Context
import android.content.SharedPreferences

/**
 * The subset of the engine's [BridgeConfig] that this app exposes to the user.
 *
 * Defaults match the bridge's own defaults in native/android-bridge/src/lib.rs
 * (default_transport, default_scan_mode, default_ip_scan, default_noize) rather
 * than being chosen here: if the two drift the user picks a setting the engine
 * either rejects or silently interprets differently.
 *
 * This is the settings model only. It holds no engine state.
 */
data class VpnSettings(
  /** "h3", "h2", "wg", "wiw" or "mim" — the bridge validates this list. */
  val transport: String = "h3",
  /** "turbo", "balanced", "thorough", "stealth" or "ironclad". */
  val scanMode: String = "balanced",
  /** "v4", "v6" or "both". */
  val ipScan: String = "both",
  /** "off", "light", "balanced", "firewall" or "aggressive". */
  val obfuscation: String = "firewall",
  /** Empty leaves the engine's own resolvers. Comma-separated. */
  val dnsServers: String = "",
  /** Manual endpoint as IP:port, blank for automatic discovery. */
  val endpoint: String = "",
  /** Only meaningful with a manual endpoint. */
  val endpointFallback: Boolean = true,
  val validationEnabled: Boolean = true,
  val fragmentTls: Boolean = false,
  val encryptedHello: Boolean = true,
  val autoReprovision: Boolean = true,
  /** error, warn, info, debug or trace. Empty leaves the engine default. */
  val logLevel: String = "info",
  /** Local SOCKS5 port, only used in proxy mode. */
  val listenPort: Int = 1819,
  val wgKeepalive: Int = 25,
  val lanSharing: Boolean = false,
) {

  /**
   * The JSON the bridge parses. Every field here is one the bridge knows; the
   * bridge rejects anything else, so a typo becomes a connect error rather
   * than a silent wrong tunnel.
   */
  fun toBridgeJson(configPath: String, mode: String): String = buildString {
    append('{')
    append("\"mode\":\"").append(mode).append('"')
    append(",\"configPath\":\"").append(configPath).append('"')
    append(",\"listenPort\":").append(listenPort)
    append(",\"wgKeepalive\":").append(wgKeepalive)
    append(",\"transport\":\"").append(transport).append('"')
    append(",\"scanMode\":\"").append(scanMode).append('"')
    append(",\"ipScan\":\"").append(ipScan).append('"')
    append(",\"noize\":\"").append(obfuscation).append('"')
    if (dnsServers.isNotBlank()) {
      append(",\"dnsServers\":\"").append(dnsServers).append('"')
    }
    if (endpoint.isNotBlank()) {
      append(",\"peer\":\"").append(endpoint).append('"')
      append(",\"peerFallback\":").append(endpointFallback)
    }
    append(",\"validationEnabled\":").append(validationEnabled)
    append(",\"fragmentTls\":").append(fragmentTls)
    append(",\"encryptedHello\":").append(encryptedHello)
    append(",\"autoReprovision\":").append(autoReprovision)
    append(",\"logLevel\":\"").append(logLevel).append('"')
    append('}')
  }

  companion object {
    private const val PREFS = "auroravpn_settings"

    fun fromPrefs(context: Context): VpnSettings {
      val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      return VpnSettings(
        transport = p.getString(KEY_TRANSPORT, "h3") ?: "h3",
        scanMode = p.getString(KEY_SCAN, "balanced") ?: "balanced",
        ipScan = p.getString(KEY_IP, "both") ?: "both",
        obfuscation = p.getString(KEY_NOIZE, "firewall") ?: "firewall",
        dnsServers = p.getString(KEY_DNS, "") ?: "",
        endpoint = p.getString(KEY_PEER, "") ?: "",
        endpointFallback = p.getBoolean(KEY_PEER_FALLBACK, true),
        validationEnabled = p.getBoolean(KEY_VALIDATION, true),
        fragmentTls = p.getBoolean(KEY_FRAGMENT, false),
        encryptedHello = p.getBoolean(KEY_ECH, true),
        autoReprovision = p.getBoolean(KEY_REPROVISION, true),
        logLevel = p.getString(KEY_LOG, "info") ?: "info",
        listenPort = p.getInt(KEY_PORT, 1819),
        wgKeepalive = p.getInt(KEY_KEEPALIVE, 25),
        lanSharing = p.getBoolean(KEY_LAN, false),
      )
    }

    private const val KEY_TRANSPORT = "transport"
    private const val KEY_SCAN = "scan_mode"
    private const val KEY_IP = "ip_scan"
    private const val KEY_NOIZE = "noize"
    private const val KEY_DNS = "dns_servers"
    private const val KEY_PEER = "peer"
    private const val KEY_PEER_FALLBACK = "peer_fallback"
    private const val KEY_VALIDATION = "validation"
    private const val KEY_FRAGMENT = "fragment_tls"
    private const val KEY_ECH = "encrypted_hello"
    private const val KEY_REPROVISION = "auto_reprovision"
    private const val KEY_LOG = "log_level"
    private const val KEY_PORT = "listen_port"
    private const val KEY_KEEPALIVE = "wg_keepalive"
    private const val KEY_LAN = "lan_sharing"
  }
}
