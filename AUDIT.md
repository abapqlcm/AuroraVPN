# AuroraVPN ← Aether Capability Audit (Report-only, no code changed)

Source of truth: the repository source. README claims were not trusted.

## PART 1 — Aether version and how it is obtained

- **Version: Aether 2.0.0** (`native/aether/aether/Cargo.toml`, `version = "2.0.0"`), confirmed by
  `pub const fn version() -> env!("CARGO_PKG_VERSION")` in `src/lib.rs`.
- **Obtained by: vendored copy.** No `.gitmodules`, no git dependency. The whole tree
  `native/aether/` is committed into the AuroraVPN repo, including its own `quiche` and
  `Docs/`. It is built locally by cargo-ndk into `libauroravpn_core.so`.
- **No prebuilt binaries** are used for the engine. `boring-sys` (BoringSSL) and `quiche` are
  also compiled from source on every build.
- The `native/aether/` tree carries local patches relative to upstream (a fork of the
  CluvexStudio/Aether engine): `UPSTREAM.md` exists but is empty; the divergences are in
  `src/socks.rs` (access control), `src/aethernoize.rs`, and the removal of the `tor` feature
  dependencies (see PART 13).

## PART 2 / 3 — Capability matrix

Statuses used: IMPLEMENTED / PARTIALLY IMPLEMENTED / NOT EXPOSED / NOT IMPLEMENTED / DISABLED / UNKNOWN.

| Capability | Aether impl | JNI exposed? | Kotlin reachable? | AuroraVPN usable? | UI should expose? |
|---|---|---|---|---|---|
| MASQUE (HTTP/3 QUIC) | IMPLEMENTED | YES (transport "h3") | YES | YES | YES |
| MASQUE (HTTP/2 TCP) | IMPLEMENTED | YES (transport "h2") | YES | YES | YES |
| WireGuard | IMPLEMENTED | YES (transport "wg") | YES | YES | YES |
| Gool / WARP-in-WARP | IMPLEMENTED | YES (transport "wiw") | YES | YES | YES (requires a second WARP account; may fail to provision) |
| MASQUE-in-MASQUE | IMPLEMENTED | YES (transport "mim") | YES | YES | YES (same caveat as wiw) |
| Cloudflare Zero Trust | PARTIALLY IMPLEMENTED | NOT EXPOSED | NO | NO | NO — interactive/CLI-only path (env `AETHER_TEAM`) |
| Endpoint scanning (gateway discovery) | IMPLEMENTED | YES (nativeScan) | YES | YES | YES |
| Endpoint testing (single peer) | IMPLEMENTED | YES (nativeTestEndpoint) | YES | YES | YES |
| Gateway selection | IMPLEMENTED | (inside prepare) | YES | YES | indirect (as "endpoint chosen") |
| Fallback endpoint | IMPLEMENTED | YES (peerFallback flag) | YES | YES | YES |
| Reconnect (cached gateway reuse) | IMPLEMENTED | (env AETHER_QUICK_RECONNECT, set by bridge) | partly | partly | as a setting only |
| Obfuscation (noize) | IMPLEMENTED | YES (noize profile) | YES | YES | YES |
| IPv4 / IPv6 / dual | IMPLEMENTED | YES (ipScan) | YES | YES | YES |
| DNS (custom resolvers in tunnel) | IMPLEMENTED | YES (dnsServers) | YES | YES | YES |
| SOCKS5 proxy mode | IMPLEMENTED | YES (mode proxy) | YES | YES | YES (Coverage: "Proxy only") |
| TUN whole-device mode | IMPLEMENTED | YES (mode tun) | YES | needs VpnService fd | YES (Coverage: "Whole device") |
| Routing: direct/block lists | IMPLEMENTED | YES (routeDirect/routeBlock) | YES | YES | advanced settings |
| Identity provisioning | IMPLEMENTED | YES (nativeProvision) | YES | YES | YES (status only, no secrets) |
| Latency / RTT (tunnel ping) | IMPLEMENTED | YES (rttMs in scan/test results) | YES | YES | YES |
| Engine logging | IMPLEMENTED | YES (nativeDrainLog) | YES | YES | YES (Diagnostics) |
| Traffic bytes / speed | NOT IMPLEMENTED in Aether | NO | NO | NO — must come from Android TrafficStats | YES (via Android API) |
| Session duration | NOT IMPLEMENTED in Aether | NO | NO | NO — must be tracked in Kotlin | YES (via Kotlin timestamp) |
| Tor | DISABLED | NO | NO | NO | NO |
| Psiphon | NOT PRESENT in this tree | NO | NO | NO | NO |
| Exit Chain | NOT PRESENT | NO | NO | NO | NO |

## PART 4 — JNI bridge trace

Every exported symbol lives in `native/android-bridge/src/lib.rs` and is exported as
`Java_com_auroravpn_app_core_NativeAetherBridge_*`.

Flow for connect (the important one):

```
Kotlin: NativeAetherBridge.nativePrepare(json)      → blocks until a peer is chosen
  Rust JNI: BridgeConfig::parse → apply_environment → embedded(None)
  Aether: prepare_embedded() → load_or_provision_masque/warp + select_embedded_peer
  returns JSON: { ok, data: { ipv4, ipv6, peer } }   ← ipv4/ipv6 are the tunnel's addresses
Kotlin: NativeAetherBridge.nativeRun(json, preparedPeer, tunFd, listener)  → blocks for the session
  Rust JNI: mode=="tun" and tunFd<0 → hard error "TUN mode requires a valid file descriptor"
            tun::TunPump::start(tun_fd, ...) → two packet channels
  Aether: run_embedded(config, EmbeddedEndpoint::Tun{..}, ready_tx)
  on ready: calls Java listener.onNativeReady()        ← this is the "connected" signal
  on every outbound socket: listener.protectSocket(fd) ← must call VpnService.protect()
  returns when: engine future completes, or stop_rx fires (nativeStop)
```

Other functions:
- `nativeScan(config)` → `{ results: [ { peer, rttMs } ] }`. Refused while the engine is running.
- `nativeTestEndpoint(config)` → `{ peer, rttMs }`. Requires a custom peer; refused while running.
- `nativeProvision(config)` → buys an identity only (used when another carrier already exists).
- `nativeStop()` → signals the stop channel; returns true if an engine was running.
- `nativeVersion()` → `"<aether version>+android.<bridge version>"`.
- `nativeDrainLog()` → drains the in-memory ring buffer of up to 400 lines.
- `nativeImportIdentity` / `nativeExportIdentity` → key backup/restore.
- `nativeSetSocketProtector` / `nativeCancelPrepare` / `nativeCancelScan` / `validateConfig`.

**Thread behaviour:** `nativePrepare`, `nativeScan`, `nativeTestEndpoint` and `nativeRun` all
call `runtime().block_on(...)` — they block the calling thread. Kotlin must call them off the
main thread. `nativeRun` blocks for the whole session.

**Errors:** every function returns a JSON string `{ ok: bool, error?: string, data?: ... }`.
`catch_unwind` wraps all of them, so a Rust panic becomes an error response instead of
crashing the app.

## PART 5 — Kotlin layer today

`VpnController.kt` (present on disk, uncommitted, not yet wired) builds a JSON config and calls
the bridge. It holds `StateFlow<VpnStatus>`. Configuration is stored in `SharedPreferences`.
Nothing yet calls `nativeRun` for real; `AuroraVpnService` currently has no TUN builder.

The committed (working) state has a **simulated** controller — the UI is real, the tunnel is not.

## PART 6 — VpnService

- `AuroraVpnService` is registered in the manifest with `BIND_VPN_SERVICE` and a
  quick-settings tile (`AuroraTileService`).
- **No TUN interface is established today.** There is no `VpnService.Builder` call, so no
  `establish()`, and therefore no fd. `connect()` is a no-op stub; the comment says establishing
  a placeholder interface would black-hole traffic.
- So: traffic is **not** routed through the VPN yet. The engine compiles and links, but the
  Android side has never handed it a descriptor. This is the single biggest missing piece.

## PART 7 — Real state model

States the backend can actually distinguish, and where each comes from:

- DISCONNECTED — `nativeRun` returned / never called.
- CONNECTING — between `nativePrepare` returning Ok and `onNativeReady()` firing. Real signal.
- CONNECTED — the listener's `onNativeReady()` callback. Real signal.
- DISCONNECTING — after `nativeStop()`, before the run future completes.
- ERROR — any `{ ok:false, error }` from prepare or run. The message is the engine's own.
- PERMISSION_REQUIRED — `VpnService.prepare(context) != null`. Android, not Aether.
- SCANNING / TESTING_ENDPOINT — only true while `nativeScan`/`nativeTestEndpoint` is in flight;
  not part of the connect flow.
- RECONNECTING — **cannot be reliably represented.** Aether reuses a cached gateway internally,
  but reports nothing to the UI about it. It would be a guess.

Proposed model (proposal only, not implemented): the six above plus
`Preparing` (pre-permission / building TUN) and `Error(reason)`. Drop RECONNECTING and
PROVISIONING as distinct UI states — the engine does not signal them.

## PART 8 — Real statistics

Aether-provided:
- **RTT latency — REAL.** `rttMs` in every scan and test result, from `tunnelping.rs`
  (`masque_http_ping`, `wg_http_ping_established`).
- **Tunnel IPv4/IPv6 — REAL**, from `prepare_embedded` → `EmbeddedPrepared.ipv4/ipv6`.

Aether does NOT provide: download/upload bytes, speed, packet counts, session duration.
`netstack.rs` and `quic.rs` have no traffic counters.

Android-provided (the honest source for the rest):
- `android.net.TrafficStats.getUidTxBytes(uid)` / `getUidRxBytes(uid)` — per-app bytes since
  device boot. Sampling these at an interval gives both cumulative traffic and, by
  differencing, instantaneous speed. Not used anywhere in the project yet.
- Session duration: a Kotlin timestamp at `onNativeReady()`. Trivially real, not a metric.

## PART 9 — Route information

What the backend can actually tell the UI about the active route:
- **Transport** — the string the UI sent (`h3/h2/wg/wiw/mim`), which the bridge validated.
  Known for certain.
- **Endpoint peer** — `prepare_embedded` returns the chosen `peer` as `IP:port`. Known.
- **Fallback active** — true when the engine selected a different peer than the primary the
  user set. Determinable by comparison. Known.
- **Tunnel addresses** — ipv4/ipv6 from prepare. Known.
- **Gateway / exit / chain nodes** — NOT EXPOSED. The engine has a notion of a gateway proxy
  (`socks::set_gateway_proxy`) but the bridge never reports it. A `DEVICE → TRANSPORT →
  PEER → INTERNET` capsule is honest; anything with GATEWAY or EXIT would be invented.

## PART 10 — Scan modes (verified, not renamed)

From `prober.rs::ScanMode::parse` — five values, each a distinct concurrency/budget strategy:

- `turbo` (also `fast`) — 20 concurrent, 45s deadline, exits on the first success
- `balanced` (default) — 16 concurrent, 120s, 6 successes, 140 samples per CIDR
- `thorough` (`deep`/`pro`) — 20 concurrent, 300s deadline
- `stealth` (`quiet`) — quiet probing, longer per-probe timeouts
- `ironclad` (`real`/`verify`/`guaranteed`) — verification-heavy

These are **endpoint scan profiles**, not routing profiles. "Adaptive / Patchy Signal /
Strict Network" do not exist anywhere in the source and must not be invented as replacements.
The UI may present them with friendlier descriptions but must keep these exact backend values.

## PART 11 — Obfuscation

`noize.rs::from_profile` accepts: `off` / `none`, `light`, `gfw` / `aggressive` / `heavy`,
`balanced` (fallback), `firewall` (bridge default). Wired via env `AETHER_NOIZE`, set by the
bridge from the `noize` config field. Kotlin can change it by sending a different config
string. **Changing it requires a reconnect** — it is read at connect time in
`noize_config()` / `aethernoize_config()`.

## PART 12 — Identity / provisioning

`identity.rs` has a `Store` with `Slot` (Wireguard / Masque), `Family`, `Device`, and a
`RegistrationBudget` that rate-limits registration attempts per IP. Provisioning is
`load_or_provision_masque` / `load_or_provision_warp`.

Safe to expose in UI: identity slot present/absent, provisioning succeeded or failed, and
budget state (rate-limited or not) via log lines. **Never expose:** the private key, the
device ID, the token, or the JWT. `nativeExportIdentity` hands out the identity blob — the UI
may offer export but must never display the contents inline.

## PART 13 — Tor / Psiphon / Exit Chain

- **Tor: DISABLED, by deliberate upstream decision.** `src/tor.rs` is vendored whole but every
  real call sits behind `#[cfg(feature = "tor")]`, and the `tor` feature is **not declared** in
  `Cargo.toml`. The manifest comment states why: enabling it needs arti-client, tor-chanmgr,
  tor-rtcompat, liblzma (a C build per ABI) and Go-built pluggable transports, which the tree
  refuses to carry. Upstream reaches Tor through `info.guardianproject:tor-android` instead.
  Status: **DISABLED — not flippable without new dependencies.**
- **Psiphon: NOT PRESENT.** No module, no references outside a stale comment.
- **Exit Chain: NOT PRESENT.** No module.

UI consequence: the CHAIN section of the original prompt is dropped entirely.

## PART 14 — Logging / diagnostics

`TeeLogger` writes every record to logcat (`-s aether`) **and** into a bounded ring buffer
(`MAX_ENGINE_LOG = 400` lines, oldest evicted). Format is `"<target>: <args>"` — severity is
**not** included in the line, and there are no timestamps. `nativeDrainLog()` returns and
clears the buffer, so calling it is destructive and each line is seen once. It is a lock-guarded
drain, so it cannot block the engine. Safe to display in a Diagnostics screen; the lines contain
engine targets and endpoint addresses but no secrets.

## PART 15 — UI capability contract (proposal only)

All of these are backed by a verified code path. Nothing below requires inventing a value.

- **transports**: `h3`, `h2`, `wg`, `wiw`, `mim` — from the bridge's validation list.
- **scanModes**: `turbo`, `balanced`, `thorough`, `stealth`, `ironclad` — from `prober.rs`.
- **obfuscationModes**: `off`, `light`, `balanced`, `firewall`, `aggressive` — from `noize.rs`.
- **ipModes**: `v4`, `v6`, `both` — from `prober.rs::IpScan`.
- **coverage**: `tun` (whole device) / `proxy` (local SOCKS5 at 127.0.0.1:listenPort) — from
  `BridgeConfig.mode`.
- **endpointScan**: `nativeScan` → list of `{peer, rttMs}`.
- **endpointTest**: `nativeTestEndpoint` → single `{peer, rttMs}`.
- **latency**: `rttMs` from scan/test; periodic re-ping is a Kotlin-side poll, not an engine stream.
- **identity**: provisioning status from prepare/provision outcomes + log lines. No secrets.
- **diagnostics**: `nativeDrainLog` lines + `nativeVersion`.
- **stats**: RTT from Aether; bytes/speed from Android `TrafficStats`; duration from Kotlin.

## PART 16 — Network Orbit UI mapping (real only)

- **HOME**: status (from `onNativeReady`), transport, chosen peer, fallback indicator, RTT,
  cumulative bytes + speed (TrafficStats), session duration, route capsule
  (`DEVICE → TRANSPORT → PEER → INTERNET`).
- **ROUTES**: transport selector (5 values), scan mode (5 values), endpoint (auto/manual/fallback),
  obfuscation (5 values), IP mode (3), coverage (tun/proxy), custom DNS, direct/block lists.
- **ACTIVITY**: engine log events drained via `nativeDrainLog`, plus Kotlin-side connect/
  disconnect/endpoint-changed events. No invented entries.
- **DIAGNOSTICS**: endpoint test results, RTT, engine version, drained log lines, last error.
- **SETTINGS**: identity status (provisioned/not, no key material), log level, auto-reprovision,
  WG keepalive, TLS fragment/ECH toggles, LAN sharing, about/version.

## PART 17 — File plan (categorised, not executed)

**DO NOT TOUCH:** `native/aether/**`, `native/third-party/**`, `native/android-bridge/src/lib.rs`
(the engine contract is verified and working — the UI adapts to it, not it to the UI).

**MODIFY:** `AuroraVpnService.kt` (add the TUN builder + fd plumbing — the one real gap),
`VpnController.kt` (already on disk, needs finishing), `AuroraApp.kt` (navigation),
`Color.kt` / `Theme.kt` / `Type.kt` (design tokens), `MainActivity.kt` (permission result),
`AndroidManifest.xml` (only if the foreground notification type needs adjusting).

**CREATE:** `NetworkCore.kt` (Canvas), `TrafficGraph.kt`, `RouteCapsule.kt`, `MetricsRow.kt`,
`VpnSettings.kt` (the settings model + prefs), `Screens`: Home/Routes/Activity/Diagnostics/Settings,
`AuroraCapabilities.kt` (the contract from PART 15), `Motion.kt` (animation constants),
`strings.xml` Persian entries.

## PART 18 — Risks and recommended next phase

Risks:
1. **The TUN descriptor is the whole game.** Until `AuroraVpnService` builds and establishes an
   interface, `nativeRun` in tun mode returns the hard error "TUN mode requires a valid file
   descriptor". Every UI state would be a lie on top of a tunnel that cannot start.
2. **`protectSocket` must work** or the engine's own handshake traffic loops back through the
   tunnel it just created. Requires the service to be bound when the engine calls back.
3. **`nativeRun` blocks indefinitely.** It must never be called on the main thread, and
   cancellation must go through `nativeStop()`, not coroutine cancellation alone.
4. **Cloudflare rate-limiting** on identity provisioning (the `RegistrationBudget`) means a
   first connect can fail with a quota error the UI must explain, not hide.
5. **TrafficStats** counts since device boot, not since session start — the sampling layer must
   snapshot at connect time or the numbers will be wildly wrong.

Recommended next phase: finish the service's TUN builder and prove one real connection end to
end (prepare → run → onNativeReady → protected traffic), with the UI still on the simulated
controller. Then swap the controller. Everything above that is presentation.
