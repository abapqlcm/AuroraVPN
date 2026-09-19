package com.auroravpn.app.ui.navigation

/**
 * The four destinations the bottom bar holds.
 *
 * The product's own spec fixes these: Home, Routes, Activity, Settings.
 * Everything else is a detail screen reached from one of them, because a phone
 * holds four readable labels and the application has far more than four
 * capabilities that deserve a screen of their own.
 *
 * Icons are drawn marks, not Material Icons: that is a separate artifact whose
 * versions do not track Compose's, and the Network Orbit phase supplies its own
 * icon language.
 */
enum class AuroraTab(
    val route: String,
    val label: String,
    val iconKind: TabIconKind,
) {
    Home("aurora/home", "Home", TabIconKind.Home),
    Routes("aurora/routes", "Routes", TabIconKind.Routes),
    Activity("aurora/activity", "Activity", TabIconKind.Activity),
    Settings("aurora/settings", "Settings", TabIconKind.Settings);

    companion object {
        val startRoute = Home.route
    }
}

/**
 * Detail screens. None of these is a tab, and all of them are reachable:
 * endpoints and transport from Routes, identity from Routes and Settings,
 * diagnostics and logs from Settings and Activity.
 */
enum class AuroraDetail(
    val route: String,
    val label: String,
) {
    Endpoints("aurora/endpoints", "Endpoints"),
    Transport("aurora/transport", "Transport"),
    Identity("aurora/identity", "Identity"),
    Diagnostics("aurora/diagnostics", "Diagnostics"),
    Logs("aurora/logs", "Logs"),
    Carrier("aurora/carrier", "Carrier"),
    Chain("aurora/chain", "Chain"),
    Advanced("aurora/advanced", "Advanced"),
}
