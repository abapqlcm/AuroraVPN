# AuroraVPN — Hermes Continuation

Phase 3: COMPLETE AURORA UI REBUILD + NETWORK ORBIT.

## Implementation summary

The Raw Functional UI is no longer the visual design. The whole user-facing
surface was rebuilt in the Network Orbit visual language — dark futuristic,
glassmorphic, neon mint and electric blue — while the WAM/Aether backend beneath
it is untouched.

**Architecture, unchanged and enforced:**

```
WAM / Aether backend
      ↓
existing services / repositories / native bridges
      ↓
thin Aurora ViewModel / adapter
      ↓
Aurora Design System
      ↓
Aurora Production UI
```

No second engine, no second transport, no second state machine, no fake state.

## Files changed

**New — design system** (`app/src/main/java/com/auroravpn/app/ui/design/`)
- `AuroraColors.kt` — the palette, and the translucent layers that stand in for
  blur (Compose has none).
- `AuroraTypography.kt` — the type scale and the shapes.
- `AuroraComponents.kt` — glass card, buttons, status pill, metrics, chips,
  radio, switch, text field, and the dimension constants.
- `AuroraTheme.kt` — the Material scheme the remaining Material components see.
- `AuroraLogo.kt`, `AuroraDetailScaffold.kt` — the wordmark and the scaffold
  every detail screen shares.

**New — Home**
- `home/NetworkOrbit.kt` — the orbital globe: glass sphere, dotted rings,
  tilted elliptical orbits, glowing nodes, pulsing core. Canvas and
  `DrawScope` only, no 3D engine; animations are `InfiniteTransition` driven
  and the geometry is computed once.
- `home/AuroraBackground.kt` — the quiet field behind everything.
- `home/RouteTopology.kt` — Device → transport → endpoint → Internet, drawn
  from live state.
- `home/NetworkOrbitHomeScreen.kt` — the dashboard.

**New — navigation**
- `navigation/AuroraNavigation.kt` — four tabs: Home, Routes, Activity,
  Settings. Every other capability is a detail screen off one of them.
- `navigation/AuroraBottomBar.kt` — glass bar, drawn icons.

**New — screens**
- `screens/AuroraActivityScreen.kt` — session, live traffic, exit address.
- `screens/AuroraAdvancedScreen.kt` — DNS, routing, proxy, obfuscation, engine.
- `screens/AuroraChainScreen.kt` — chain state, nodes, testing.
- `AuroraTelemetry.kt` — the waveform's memory: a bounded window of the samples
  the engine actually published.

**Rewritten**
- `AuroraApp.kt`, `AuroraRoutesScreen.kt`, `AuroraEndpointsScreen.kt`,
  `AuroraTransportScreen.kt`, `AuroraIdentityScreen.kt`,
  `AuroraDiagnosticsScreen.kt`, `AuroraLogsScreen.kt`,
  `AuroraSettingsScreen.kt`, `AuroraCarrierScreen.kt`.
- `MainActivity.kt` — wires telemetry, nothing else.

**Removed**
- `AuroraNavigation.kt` (the five-tab enum, replaced by the four-tab one),
  `AuroraHomeScreen.kt`, `AuroraMoreScreen.kt` — superseded.

## Capabilities preserved

Every one of these is still wired to the real backend and reachable:
VPN/TUN, AetherVpnService, NativeAetherBridge, endpoint discovery/scanning/
testing/clean validation, MASQUE H3/H2, WireGuard, WIW, MIM, automatic
transport and fallback, route memory, AutoPlanner, IPv4/IPv6/dual stack, DNS,
direct and block routing, split tunneling, SOCKS5/proxy, provisioning,
identity import/export, connection profiles, reconnect, traffic telemetry,
RTT, logs, diagnostics, network identity, exit address, network-change
handling, kill switch (and strict), notifications, tile, Carrier, Psiphon,
Tor, Chain/mihomo, lyrebird, snowflake, JNI bridges, native assets.

`git diff 33e714e..HEAD -- native/ app/src/main/java/com/whitedns/ app/src/main/jni`
is empty: not one line of WAM or native code was touched.

## No fake production data

Nothing is hard-coded. "Frankfurt", "MASQUE H3", "12 ms", "86 Mbps", "18.2 GB"
and "SECURE" appear nowhere as constants. Metrics read
`TrafficMeter`, RTT reads the engine's own publication, endpoints read
`endpointScannerState`, and every unavailable value renders as `—`. The
waveform's history is cleared when a session ends, so it never replays a shape
that did not happen.

## Branded

The visible identity is AURORA / NETWORK ORBIT. "Aether" survives only where it
names the engine — carrier labels, diagnostics, the AGPL notice — which the
spec permits.

## Tests

- Kotlin compilation: PASS (main + androidTest)
- Unit tests: PASS
- Lint: PASS
- Native build: unchanged from Phase 2.3; no native code was touched
- Rust host tests (Phase A): 357 passed, including the two scan tests added to
  `native/aether/aether/src/lib.rs`

## APK architecture

arm64-v8a only, as before. The preview build here reuses the native libraries
already built by CI; the CI build rebuilds them from source.

## Endpoint scan issue — status

**ROOT CAUSE: NOT YET CONFIRMED. Physical-device verification: NOT VERIFIED.**

What the Phase A investigation established:
- JNI symbols match 14/14 between the Kotlin declarations and the built library.
- The DEBUG build the CI ships unwinds panics (`_Unwind_Resume` present), so
  `[profile.release] panic = "abort"` is a release-build risk, not the cause of
  the current failure.
- The Rust scan path does not panic under test on the host; it times out
  instead. That is a hypothesis about a blocking call, and a host timeout does
  not prove an Android ANR.
- The instrumented tests (`EndpointScanTest.kt`, `AuroraScanPathTest.kt`)
  compile and are committed. They have not been run, because no physical device
  is available and the emulator on this host is unusable (2 cores, no KVM).

The new Endpoints screen preserves the real scanner, keeps cancellation and
lifecycle safe, does not block the main thread, and reports the engine's own
errors rather than substituting results.

## Device verification

NOT PERFORMED. No physical device was available. The emulator on this host
cannot run the app (2 CPU cores, no `/dev/kvm`).

## Commit

See `git log` for the Phase 3 commit.
