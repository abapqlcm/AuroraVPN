# AuroraVPN — Phase 2 Final Report: Raw Functional UI

Base: WhiteAestherMobile `7ce2fcb` (untouched). Commit `e86ee88` on `main`.

The Aurora UI is a new Compose surface that reads WAM's state and calls WAM's
functions. The backend was not rewritten, replaced or trimmed.

---

## UI

| Screen | Status | Notes |
|---|---|---|
| Home | PASS | Real stage, transport, peer, traffic, addresses, duration, engine version |
| Routes | PASS | Block/direct rules, split-tunnel mode, engine mode, route sniffing |
| Endpoints | PASS | Scan, test, cancel, results with RTT, selection, endpoint mode |
| Transport | PASS | H3/H2/WG/WIW/MIM/Auto, scan mode, obfuscation, fragment TLS, ECH, language, theme |
| Identity | PASS | Export/import via file pickers, auto-reprovision toggle, engine messages |
| Diagnostics | PASS | Engine, carrier path, auto attempts, network, addresses, last error |
| Logs | PASS | Engine buffer, timestamps, level colours, share, clear, auto-scroll |
| Settings | PASS | DNS, dual stack, kill switch, strict, proxy, LAN sharing, ports, TLS groups, advanced |
| Carrier & Chain | PASS | Carrier select, Psiphon regions, Tor bridges + fetch, second carrier, chain nodes/test/select |

## Capabilities

- **Discovered:** 55 (33 from the gate audit + 22 more found while tracing source)
- **Mapped:** 55 — every one has a Backend, State Source, Setting, Action and Screen
- **Exposed in UI:** 55
- **Preserved:** 55
- **Removed:** **0**

See `docs/CAPABILITY_CONTRACT.md`.

## Backend

- WAM modified: **NO** — not one line of the 9,562-line backend was changed
- Aether modified: **NO**
- native/JNI modified: **NO**
- Psiphon preserved: YES
- Tor preserved: YES (incl. obfs4, snowflake, custom bridges, Moat fetch)
- Chain preserved: YES (incl. node testing, REALITY `supported` flag, through-tunnel)
- Carrier preserved: YES

The only non-UI source change is `ConnectionProfile` moving from `ui/Screens.kt` to
the `data` package. It is a capability — scan/transport presets — not a drawing, and
moving it is what let the old screens be deleted without losing the presets. Its
`icon` accessor was dropped because it depended on UI icons.

## State

- **Fake state: NO.** `Connected` is only reachable through the engine's own
  `onNativeReady`; `Failed` carries the engine's own message; an empty peer means the
  engine has not chosen one; a zero byte count means the session carried nothing.
- **Production mock backend: NO.** No mock of any kind exists in the tree.

## Tests

| Check | Result |
|---|---|
| `compilePreviewDebugKotlin` | PASS |
| Unit tests | 225 tests, 0 failures, 0 errors, 0 skipped |
| Lint | 0 errors (warnings only, all pre-existing from WAM) |
| APK build | PASS — 3 ABIs, Aurora classes + fa/en string resources in the dex, removed UI classes absent |
| Rust/native/JNI | Run by CI (needs the Rust + NDK toolchain; built on GitHub Actions only) |
| GitHub Actions | CI run `35373157576` on `e86ee88` — see status below |
| Device VPN/TUN | NOT RUN — no device available |

The local APK was built with the Rust/NDK task skipped, so its native engine is from a
prior build. The CI run builds the real thing.

## WAM UI

**Replaced screens (8 files, 5,022 lines removed):** `WhiteAestherApp`, `Screens`,
`ChainScreen`, `SplitTunnelScreen`, `ConnectOrb`, `AetherComponents`, `Summaries`,
`TvSupport`.

**Remaining:** `theme/Theme.kt`, `theme/Type.kt`, `TvUiPolicy.kt`. These are the theme
and the TV layout policy — the Aurora surface uses them deliberately, they are not
product screens. `MainActivity` now hosts `AuroraApp` instead of `WhiteAestherApp`.

## Network Orbit

**DEFERRED.** No orbit, globe, particles or canvas work was done. Bottom-bar icons are
text glyphs because the Material Icons artifact's versions do not track Compose's; the
visual language arrives with Network Orbit.

## Git

| Commit | Purpose |
|---|---|
| `e86ee88` | Phase 2: Aurora raw functional UI on the WAM backend |

Pushed to `main`. Prior commits `a907728` (WAM base), `d4c538f` (rebrand),
`0574cb9` (CI), `6c778ce` (fork report) are unchanged.

## What is not verified

Anything that needs a phone: a real connect, transport switching, live traffic, RTT,
DNS, IPv6, split tunnel in practice, proxy, kill switch, carrier and Psiphon/Tor paths.
All of these are wired to real WAM code paths and all are untested on hardware.
