package com.whitedns.whiteaesther.data

import androidx.annotation.StringRes
import com.whitedns.whiteaesther.R

/**
 * Preset combinations of scan depth and transport.
 *
 * Lives in the data package because it is a capability, not a drawing: the presets are
 * how the engine is told to search, and a screen that reads them is optional. It used
 * to sit inside a UI file, which made removing the old screens look like removing the
 * presets.
 */
enum class ConnectionProfile(
    @StringRes val label: Int,
    @StringRes val description: Int,
    @StringRes val tag: Int?,
    val scan: ScanStrategy?,
    val transport: TunnelProtocol?,
) {
    ADAPTIVE(
        R.string.profile_adaptive,
        R.string.works_on_most_networks_start_here,
        R.string.profile_recommended,
        ScanStrategy.BALANCED,
        TunnelProtocol.AUTO,
    ),
    PATCHY(
        R.string.patchy_signal,
        R.string.for_mobile_data_that_keeps_dropping,
        null,
        ScanStrategy.THOROUGH,
        TunnelProtocol.AUTO,
    ),
    STRICT(
        R.string.strict_network,
        R.string.for_wi_fi_that_blocks_a_lot,
        null,
        ScanStrategy.STEALTH,
        TunnelProtocol.H2,
    ),
    MANUAL(
        R.string.profile_manual,
        R.string.you_choose_every_setting_yourself,
        null,
        null,
        null,
    ),
    ;
}

/** The preset these settings currently match, or [ConnectionProfile.MANUAL]. */
fun AppSettings.activeProfile(): ConnectionProfile =
    ConnectionProfile.entries.firstOrNull {
        it.scan == scanStrategy && it.transport == transport
    } ?: ConnectionProfile.MANUAL

/** Apply a preset's scan and transport, leaving everything else alone. */
fun AppSettings.applyProfile(profile: ConnectionProfile): AppSettings =
    if (profile.scan == null || profile.transport == null) {
        this
    } else {
        copy(scanStrategy = profile.scan, transport = profile.transport)
    }
