package com.auroravpn.app.ui

/**
 * The five destinations the bottom bar holds.
 *
 * Advanced screens hang off [More] rather than joining the bar: a phone holds five
 * readable labels, and Identity, Diagnostics, Logs, Settings and Carrier/Chain are
 * already five on their own.
 *
 * Text labels rather than icons. Material Icons is a separate artifact whose versions
 * do not track Compose's, and this phase is about wiring rather than a look — the
 * Network Orbit phase supplies the visual language.
 */
enum class AuroraDestination(
    val route: String,
    val label: String,
) {
    Home("aurora/home", "Home"),
    Routes("aurora/routes", "Routes"),
    Endpoints("aurora/endpoints", "Endpoints"),
    Transport("aurora/transport", "Transport"),
    More("aurora/more", "More");

    companion object {
        val startRoute = Home.route
    }
}

/** Advanced screens reached from [AuroraDestination.More]. */
enum class AuroraAdvancedDestination(val route: String, val label: String) {
    Identity("aurora/identity", "Identity"),
    Diagnostics("aurora/diagnostics", "Diagnostics"),
    Logs("aurora/logs", "Logs"),
    Settings("aurora/settings", "Settings"),
    Carrier("aurora/carrier", "Carrier & Chain"),
}
