# AuroraVPN — UI Architecture

Phase 2: Raw Functional UI. Network Orbit is deferred to a later phase.

---

## 1. WAM is the source of truth

Nothing in the Aurora layer creates connection state, traffic numbers, endpoint data,
log entries, or identity status. Every one of those comes from a WAM-owned source:

| Domain | Source | Type |
|---|---|---|
| Connection stage, peer, path, session start | `EngineStatusStore.status` | `StateFlow<EngineStatus>` |
| Bytes up/down, rate | `TrafficMeter.sample` | `StateFlow<TrafficSample>` |
| Engine + service logs | `EngineLog.entries` | `StateFlow<List<LogEntry>>` |
| All user settings | `SettingsRepository.settings` | `Flow<AppSettings>` |
| Endpoint scan/test | `MainViewModel.endpointScannerState` | `StateFlow<EndpointScannerState>` |
| Chain nodes + tests | `MainViewModel.chainState` | `StateFlow<ChainState>` |
| Exit / local address | `MainViewModel.addresses` | `StateFlow<AddressPair>` |
| Psiphon regions | `MainViewModel.psiphonRegions` | `StateFlow<List<String>>` |
| Tor bridge fetch | `MainViewModel.bridgesMessage` / `bridgesFetching` | `StateFlow<…>` |
| Identity messages | `MainViewModel.identityMessage` | `StateFlow<IdentityMessage?>` |
| Engine version | `NativeAetherBridge.versionOrNull()` | `String?` |

The adapter **maps** these. It does not duplicate them.

## 2. Layering

```
AuroraScreen (Composable)
        ↓ reads / calls
AuroraViewModel   ← the only Aurora-owned class
        ↓ reads        ↓ forwards
WAM StateFlows     WAM actions (MainViewModel, AetherVpnService.start/stop)
        ↓
WAM service / repository / engine / native
```

`AuroraViewModel` is a thin wrapper over the existing `MainViewModel` and the service
intents. It exists so that:

1. Screens never touch `com.whitedns.whiteaesther` internals directly — the mapping is
   in one place, so when WAM renames a field, one file changes.
2. Compose state stays simple: each screen asks for a mapped UI model, not a raw
   `EngineStatus` it then has to interpret.
3. The WAM `MainViewModel` is **not modified**. This is a hard requirement of the phase.

## 3. UI state model

One small sealed type for the connection, derived from `EngineStage`:

```kotlin
sealed interface AuroraConnectionState {
    data object Idle : AuroraConnectionState
    data class Preparing(val message: String) : AuroraConnectionState
    data class Connecting(val message: String) : AuroraConnectionState
    data class Connected(
        val peer: String,
        val transport: String,
        val startedAt: Long,
    ) : AuroraConnectionState
    data class Stopping(val message: String) : AuroraConnectionState
    data class Failed(val message: String) : AuroraConnectionState
}
```

`Preparing`/`Connecting`/`Connected` carry the `EngineStatus.message` verbatim — the
engine's own sentence, not one we wrote. `Failed` carries the engine's error. Nothing
here is invented; every field traces to an `EngineStatus` field.

## 4. Navigation

A single-activity nav graph, bottom-nav destinations only:

```
Home → Routes → Endpoints → Transport → More
                                        ├─ Identity
                                        ├─ Diagnostics
                                        ├─ Logs
                                        ├─ Settings
                                        └─ Carrier & Chain
```

`More` is a list of advanced screens so the bottom bar stays 5 items on a phone.

## 5. Screen-to-capability mapping

| Screen | Capabilities | Primary state |
|---|---|---|
| Home | connect/disconnect, stage, peer, transport, traffic, RTT summary, exit address, duration, error | `engineStatus`, `traffic`, `addresses` |
| Routes | direct/block rules, split tunnel, auto, proxy mode, route memory reset | `settings` |
| Endpoints | scan, test, clean-endpoint results, RTT, cancel, selection | `endpointScannerState` |
| Transport | H3/H2/WG/WIW/MIM/auto, scan mode, obfuscation, fragment/ECH | `settings` |
| Identity | status, export, import, provisioning config | `identityMessage`, `settings.autoReprovision` |
| Diagnostics | network, IPv4/IPv6, DNS, engine/carrier/chain state, RTT, errors, kill switch | `engineStatus`, `NetworkIdentity`, `EngineLog` |
| Logs | engine + service logs, timestamps, clear, copy/share | `EngineLog.entries` |
| Settings | every `AppSettings` field, theme, language, update check | `settings` |
| Carrier & Chain | carrier select, Psiphon region, Tor bridges, chain nodes/tests | `settings`, `psiphonRegions`, `bridgesMessage`, `chainState` |

## 6. Lifecycle

- The activity owns one `AuroraViewModel`. All WAM state is `StateFlow`, so rotation and
  process recreation re-subscribe with no loss.
- `EngineStatusStore`, `TrafficMeter` and `EngineLog` are singletons — they survive
  activity recreation, and service restart simply writes a new value into the same flow.
- VPN permission: the activity's existing `vpnPermission` launcher. On result,
  `continueConnectionRequest()` starts the service. If the user denies, the stage stays
  `IDLE` and Home shows that — never a fake connecting state.
- Notification permission: existing launcher, requested before connect on Android 13+.
- Identity import/export: existing `OpenDocument` / `CreateDocument` launchers.

## 7. Error handling

- Every failure surfaces as `EngineStatus.stage == ERROR` with the engine's `message`,
  shown on Home and Diagnostics.
- Endpoint scan/test failures set `endpointScannerState.error` — shown inline on
  Endpoints, not swallowed.
- Chain failures set `chainState.error` — shown inline on the Chain screen.
- Retry is the engine's own reconnect path (§Capability Contract, "Reconnect with
  backoff"). The UI exposes a manual connect button; it does not implement its own
  retry loop.

## 8. Performance

- No polling. Everything observable is a `StateFlow` the UI collects.
- The one exception is traffic sampling, which is 1 Hz **and already implemented** in
  `MainViewModel.sampleTrafficWhileConnected` — the UI just reads the flow.
- Changing a setting calls `save()`, which writes DataStore. The service picks the new
  config up on the next connect. The UI never restarts the engine itself.

## 9. Testing strategy

- Unit tests for the `AuroraViewModel` mappings — the only new logic worth testing
  without a device.
- Existing WAM tests (225) must keep passing untouched.
- CI runs: Rust fmt/clippy/test, unit tests, lint, APK build, JNI symbol verification.
- Runtime tests (connect, transport switch, traffic, RTT) need a device and are
  scheduled after this phase.

## 10. What is explicitly not here

- Network Orbit (visual design) — deferred.
- Canvas animations, globe, particles — deferred.
- Any new backend, engine, prober, or transport — forbidden by the phase contract.
- Any removal of WAM capability — forbidden.
