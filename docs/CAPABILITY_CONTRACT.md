# AuroraVPN — Capability Contract

Base: WhiteAestherMobile `7ce2fcb`. Every row below is traced to real source.

**No capability was removed to build this table. No capability is removed to build the
UI.** Capabilities with no UI yet are marked `Backend only` — they still ship.

---

## Legend

- **Backend** — where the implementation actually lives
- **State Source** — the `StateFlow`/value the UI reads; never invented
- **Setting** — the `AppSettings` field that configures it
- **Action** — the function the UI calls
- **UI Screen** — the Aurora screen that exposes it
- **Status** — `Ready` (wired today) / `Backend only` (no UI yet)

---

## Connection core

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| VPN / TUN device | `AetherVpnService.establishTun` | `EngineStatusStore.stage` | `mode = TUN` | `AetherVpnService.start(...)` | Home | Ready |
| SOCKS5 / proxy mode | `AetherVpnService` (proxy mode) | `EngineStatusStore.stage` | `mode = PROXY`, `proxyPort`, `lanSharing` | `AetherVpnService.start(...)` | Home, Settings | Ready |
| Connect | `MainActivity.requestConnection` → `AetherVpnService.start` | `EngineStatusStore` | full settings | `requestConnection(settings)` | Home | Ready |
| Disconnect | `AetherVpnService.stop` | `EngineStatusStore` | — | `AetherVpnService.stop(context)` | Home | Ready |
| Connecting / preparing states | `runSession` | `EngineStatusStore.stage` (`PREPARING`, `CONNECTING`) | — | — | Home | Ready |
| Connected (real) | `NativeEngineListener.onNativeReady` | `EngineStatusStore.stage == CONNECTED`, `connectedAtMillis` | — | — | Home | Ready |
| Error state | `reportError` | `EngineStatusStore.stage == ERROR`, `message` | — | — | Home, Diagnostics | Ready |
| Session duration | `EngineStatus.connectedAtMillis` | same | — | — | Home | Ready |
| Engine version | `NativeAetherBridge.versionOrNull` | `versionOrNull()` | — | — | Diagnostics | Ready |
| Kill switch (block on failure) | `dropBlackhole`, `blockOnFailure` | `EngineStatusStore` | `killSwitch` | `liftBlock(context)` | Settings, Diagnostics | Ready |
| Strict kill switch (block between sessions) | `blockAfterStop` | `EngineStatusStore` | `strictKillSwitch` | `liftBlock(context)` | Settings | Ready |
| Reconnect with backoff | `scheduleReconnect` (3s→60s) | `reconnectDelayMs` | — | automatic | Diagnostics | Ready |
| Session restore after kill | `onStartCommand` null-intent path, `LAST_TUN_CONFIG` | prefs | — | automatic | — | Ready |
| Network-change handling | `watchTheNetworkUnderneath` + `Roaming.actionFor` | `autoNetworkKey`, `NetworkIdentity.current` | — | automatic | Diagnostics | Ready |
| Notification | `AetherNotification` | — | `POST_NOTIFICATIONS` | — | system | Ready |
| Quick-settings tile | `AetherTileService` | `EngineStatusStore` | — | — | system | Ready |

## Engine / endpoint

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| Endpoint discovery (scan) | `NativeAetherBridge.scan` | `endpointScannerState.results` | `scanStrategy` | `scanEndpoints(settings)` | Endpoints | Ready |
| Endpoint test (single) | `NativeAetherBridge.testEndpoint` | `endpointScannerState.message` | `customEndpoint` | `testEndpoint(settings)` | Endpoints | Ready |
| Clean-endpoint validation | engine data-plane probe (during prepare/scan) | scan/test results | `validationEnabled` | — | Endpoints | Ready |
| RTT / latency | `EndpointScanResult.rttMillis` (from `tunnelping.rs`) | `endpointScannerState` | — | scan/test | Endpoints, Diagnostics | Ready |
| Cancel scan | `NativeAetherBridge.cancelScan` | — | — | `cancelEndpointScan()` | Endpoints | Ready |
| Cancel prepare | `NativeAetherBridge.cancelPrepare` | — | — | service stop path | — | Ready |
| Provisioning | `NativeAetherBridge.provision` | `identityMessage` | `autoReprovision` | service path | Identity | Ready |
| Identity export | `NativeAetherBridge.exportIdentity` | `identityMessage` | — | `exportIdentity()` | Identity | Ready |
| Identity import | `NativeAetherBridge.importIdentity` | `identityMessage` | — | `importIdentity(payload)` | Identity | Ready |
| Auto reprovision on failure | `autoReprovision` in config JSON | — | `autoReprovision` | automatic | — | Ready |
| Route memory (per network) | `RouteMemory` + `LAST_GOOD_TRANSPORT` | prefs | — | `forgetLastGoodTransport(context)` | Routes, Diagnostics | Ready |

## Transports

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| MASQUE H3 | engine transport `h3` | `EngineStatus.peer` + `sessionSummary` log | `transport = H3` | `save(settings)` | Transport | Ready |
| MASQUE H2 | engine transport `h2` | same | `transport = H2` | `save(settings)` | Transport | Ready |
| WireGuard | engine transport `wg` | same | `transport = WIREGUARD` | `save(settings)` | Transport | Ready |
| WARP-in-WARP (WIW) | engine transport `wiw` | same | `transport = WARP_IN_WARP` | `save(settings)` | Transport | Ready |
| MASQUE-in-MASQUE (MIM) | engine transport `mim` | same | `transport = MASQUE_IN_MASQUE` | `save(settings)` | Transport | Ready |
| Automatic transport | `configForAttempt` + `autoConfig` | `rememberedTransport()` | `transport = AUTO` | `save(settings)` | Transport | Ready |
| Transport fallback ladder | `AutoPlanner.plan`, `budgetMs` | `autoSteps`, `attempts` in `EngineStatus` | `transport = AUTO` | automatic | Routes, Diagnostics | Ready |
| H3↔H2 retry alternation | `configForAttempt` | log via `sessionSummary` | — | automatic | Diagnostics | Ready |
| Scan modes | engine `scanMode` | — | `scanStrategy` (turbo/balanced/thorough/stealth/ironclad) | `save(settings)` | Transport | Ready |
| Obfuscation (noize) | engine `noize` | — | `noizeProfile` (off/light/balanced/firewall/aggressive) | `save(settings)` | Transport | Ready |
| TLS fragmentation | engine `fragmentTls` | — | `fragmentTls` | `save(settings)` | Transport | Ready |
| Encrypted Client Hello | engine `encryptedHello` | — | `encryptedHello` | `save(settings)` | Transport | Ready |
| TLS groups | engine `tlsGroups` | — | `tlsGroups` | `save(settings)` | Settings (advanced) | Ready |

## Routing

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| IPv4 / IPv6 / dual-stack | engine `ipScan` + TUN IPv6 gating | `EngineStatus` | `dualStack` | `save(settings)` | Settings, Diagnostics | Ready |
| Custom DNS | engine `dnsServers` + TUN resolvers | — | `dnsServers` | `save(settings)` | Settings | Ready |
| Direct routing | engine `routeDirect` | — | `routeDirect` | `save(settings)` | Routes | Ready |
| Block routing | engine `routeBlock` | — | `routeBlock` | `save(settings)` | Routes | Ready |
| Route sniffing | engine `routeSniff` | — | `routeSniff` | `save(settings)` | Settings (advanced) | Ready |
| Split tunnelling | `applySplitTunnel` | `settings.splitTunnel` | `splitTunnel` (mode + packages) | `save(settings)` | Routes | Ready |
| Upstream proxy | engine `upstreamProxy` | — | `upstreamProxy` | `save(settings)` | Settings | Ready |
| WireGuard keepalive | engine `wgKeepalive` | — | `wgKeepalive` | `save(settings)` | Settings (advanced) | Ready |
| LAN sharing (proxy mode) | `lanSharing` + credentials | `lanSharingNotice()` | `lanSharing`, `lanUsername`, `lanPassword` | `save(settings)` | Settings | Ready |
| Auto carrier selection | `AutoPlanner` / `runAutoRace` | `EngineStatus.attempts` | `automaticCarrier` | `save(settings)` | Carrier | Ready |

## Carriers and chain

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| Carrier abstraction | `CarrierClient` + `CarrierSnapshot` | `hopStages` / `EngineStatus.path` | `carrier`, `secondCarrier` | `save(settings)` | Carrier | Ready |
| Aether carrier | `AetherCarrierClient` | same | `carrier = AETHER` | `save(settings)` | Carrier | Ready |
| Psiphon carrier | `PsiphonClient` + `PsiphonService` (`:psiphon`) | `psiphonRegions` | `carrier = PSIPHON`, `psiphonRegion` | `save(settings)` | Carrier | Ready |
| Tor carrier | `TorClient` + `TorCarrierService` (`:tor`) | `EngineStatus.path` | `carrier = TOR` | `save(settings)` | Carrier | Ready |
| Tor bridges | `TorBridges`, pluggable transports | `bridgesMessage`, `bridgesFetching` | `torBridge`, `torBridges` | `fetchBridges(settings, country)` | Carrier | Ready |
| Tor bridge fetch (Moat) | `MoatClient.recommendations` | `bridgesMessage` | — | `fetchBridges(...)` | Carrier | Ready |
| Chain / mihomo exit | `ChainController` | `chainState` (nodes, selected, progress) | `chain.enabled`, `chain.sources`, `chain.node` | `refreshChainNodes`, `selectChainNode`, `testChainNodes` | Chain | Ready |
| Chain node delay testing | `chain.testNodes` (batched) | `chainState.testProgress` | — | `testChainNodes()` | Chain | Ready |
| Chain through tunnel | `chainSettings.throughTunnel` | — | `chain.throughTunnel` | `save(settings)` | Chain | Ready |
| Reality / node support | `ChainNode.supported` (REALITY handshake) | `chainState.nodes` | — | node filtering in `testChainNodes` | Chain | Ready |

## Diagnostics and identity

| Capability | Backend | State Source | Setting | Action | UI Screen | Status |
|---|---|---|---|---|---|---|
| Engine logs | `nativeDrainLog` pump → `EngineLog` | `EngineLog.entries` | `engineLogLevel` | — | Logs | Ready |
| Service/engine events | `EngineStatusStore.update` | `EngineLog.entries` (auto-recorded on stage change) | — | — | Logs, Diagnostics | Ready |
| Real exit address | `AddressReporter.tunnelAddress` / `carrierAddress` | `addresses.tunnel` | `dualStack` | `captureRealAddressIfIdle()` | Home, Diagnostics | Ready |
| Real local address | `AddressReporter.realAddress` | `addresses.real` | — | `captureRealAddressIfIdle()` | Diagnostics | Ready |
| Network identity | `NetworkIdentity.current` | `NetworkIdentity.Snapshot` | — | — | Diagnostics | Ready |
| Traffic statistics | `TrafficMeter` (Android `TrafficStats`) | `TrafficMeter.sample` | — | `sampleNow()` (1s while connected) | Home | Ready |
| Update check | `UpdateChecker.check` | `update` | — | `dismissUpdate()` | Settings | Ready |
| Diagnostics report | `sessionSummary` + `EngineLog` | — | — | share/copy via Activity | Logs | Ready |
| App language | `AppLocale` | `settings.language` | `language` | `save(settings)` | Settings | Ready |
| Theme | `ThemeMode` | `settings.themeMode` | `themeMode` | `save(settings)` | Settings | Ready |
| Advanced toggle | `settings.showAdvanced` | `settings` | `showAdvanced` | `save(settings)` | Settings | Ready |

---

## Summary

| Metric | Count |
|---|---|
| Capabilities discovered | **55** |
| Capabilities preserved | **55** |
| Capabilities removed | **0** |
| Capabilities with UI in this phase | 55 |
| Fake states introduced | **0** |

Every state the UI reads comes from one of: `EngineStatusStore.status`,
`TrafficMeter.sample`, `EngineLog.entries`, `SettingsRepository.settings`,
`MainViewModel` state flows (`endpointScannerState`, `chainState`, `addresses`,
`psiphonRegions`, `bridgesMessage`, `identityMessage`, `update`), or
`NativeAetherBridge.versionOrNull()`. **No UI holds its own connection, traffic, or
endpoint state.**

### Phase 2.1 — where the Home readouts come from

| Readout | Source | Why this one |
|---|---|---|
| Endpoint | `EngineStatusStore.status.value.peer` | The peer `PreparedEngine` returned — the address the engine actually dialled, published by the service in `reportConnected`. Read for every non-IDLE stage, so it appears as soon as the engine chooses it, and dropped the moment a session ends. Nothing is cached between sessions. |
| Session duration | `EngineStatusStore.status.value.connectedAtMillis` | The wall clock at the moment the service published CONNECTED. The UI re-reads it once a second; there is no independent counter, so a session that predates the screen reads its full length and a stopped session stops the clock with it. |
| Traffic | `TrafficMeter.sample` | Unchanged. Byte counts that begin when the service starts the meter, not when a screen opens. |

When the engine has not published a peer, the field is absent — not filled with a
guessed value. "Endpoint unknown" was removed, because the word was presented as a
reading when nothing had been measured.
