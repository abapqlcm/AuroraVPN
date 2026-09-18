# AuroraVPN — WAM True-Fork Rebuild, Final Report

Date: 2026-09-18
Fork base: WhiteAestherMobile `7ce2fcbce9dfdcc77b33cf6ffdcf00b19c64df5e`
Final HEAD: `0574cb9` — pushed to `github.com/abapqlcm/AuroraVPN`, branch `main`
CI run: `35321153898` — **success**, all 12 steps green
APK artifact: `auroravpn-preview` (id 10537608042)

---

## Repository

| Question | Answer |
|---|---|
| old Aurora files deleted | **YES** |
| old Aurora source remaining in working tree | **NO** |
| WAM base installed | **YES** |
| WAM revision | `7ce2fcbce9dfdcc77b33cf6ffdcf00b19c64df5e` |
| old history rewritten | NO (old commits remain reachable in history; the working tree was replaced) |
| backup created | **YES** — outside the tree |

**Backup location:** `/tmp/aurora-backup/`
- `aurora-old.bundle` (71MB) — git bundle of the entire old Aurora history, all branches and tags
- `working-tree-snapshot/` (125MB) — complete copy of the old working tree
- `AUDIT_MIGRATION.md`, `PRE_MIGRATION_GATE.md`, `WAM_AURORA_REBUILD_GATE.md` — the three audit reports

The rollback path is: `git clone aurora-old.bundle` and push over `main`. Nothing inside
the repository contains old Aurora source — no `old/`, `legacy/`, `archive/` folder.

---

## Capabilities

| Question | Answer |
|---|---|
| total capabilities discovered | 33 |
| total capabilities preserved | **33** |
| removed capabilities | **0** |
| reasons for removal | none |

The previous decision to drop Psiphon, Tor, Chain and the carrier abstraction was
**rescinded** per this prompt. Nothing was removed. All five Go/Rust engines are present
in the shipped APK, verified:

| Engine | Size (arm64) | Status |
|---|---|---|
| `libwhiteaesther_core.so` (Aether) | 24,808,456 | ✅ verified, 10 JNI symbols found |
| `libwhiteaestherchain.so` (mihomo exit chain) | 46,625,496 | ✅ verified |
| `libgojni.so` (Psiphon tunnel core) | 31,197,792 | ✅ verified |
| `libtor.so` | 7,576,992 | ✅ verified |
| `liblyrebird.so` (obfs4 transport) | 17,076,008 | ✅ verified |
| `libsnowflake.so` | 16,793,128 | ✅ verified |
| `assets/psiphon_server_entries.txt` | — | ✅ verified |

---

## Build

| Item | Value |
|---|---|
| GitHub Actions workflow | `.github/workflows/ci.yml` (upstream's, artifact renamed) |
| JDK | Temurin 21 |
| Gradle | via `gradle/actions/setup-gradle@v6` |
| AGP | 9.4.0 |
| NDK | 29.0.14206865 |
| Rust | 1.98.0 + cargo-ndk 4.1.2 |
| Go | 1.25 |
| ABIs | armeabi-v7a, arm64-v8a, x86_64 — all three, none dropped |
| APK | **PASS** (68.7MB arm64 preview, v2-signed, zipaligned) |
| AAB | not built (preview debug only; release.yml handles signed release) |

CI assertions that passed: each engine present once per ABI, universal APK carries all
three, VPN service declared in manifest with `BIND_VPN_SERVICE`.

---

## Tests

| Test | Result |
|---|---|
| unit | **PASS — 225 tests, 0 failures** |
| Rust bridge (`cargo test` + `clippy -D warnings` + `cargo fmt --check`) | **PASS** |
| native/JNI load | **PASS** — 10 `Java_com_whitedns_whiteaesther_core_NativeAetherBridge_*` symbols verified in the .so |
| APK packaging (signature, alignment, manifest) | **PASS** |
| design prototype | **PASS** |
| VPN/TUN smoke | NOT RUN — requires a device |
| endpoint / transport / routing / IPv4-IPv6 / DNS / kill switch / reconnect | NOT RUN — runtime tests, require a device |

---

## UI

| Question | Answer |
|---|---|
| Raw Functional UI | **NOT BUILT** — this phase replaced the base and rebranded |
| fake state | **NO** — no UI written, so no fake state exists |
| Network Orbit | **DEFERRED** |

The WAM UI still ships in this build, as-is. It is the reference implementation that
proved the backend compiles and packages. **It is not the product UI.** The Aurora
adapter layer and Raw Functional UI are the next phase, and the WAM UI is removed as each
Aurora screen replaces it.

---

## Git

| Commit | Purpose |
|---|---|
| `a907728` | Replace repository base with WhiteAestherMobile `7cfe2fcb` — old Aurora removed in full, 4314 files |
| `d4c538f` | Rebrand product identity to AuroraVPN (strings, session name, theme, README, notices) |
| `0574cb9` | Rename preview artifact to `auroravpn-preview` |

All small, all individually revertable. Rollback point: `aurora-old.bundle`.

---

## What was deliberately left as WAM named it

`applicationId`, the Kotlin package tree `com.whitedns.whiteaesther.*`, and the JNI
symbol prefix `Java_com_whitedns_whiteaesther_core_NativeAetherBridge_*` were **not**
renamed. Renaming the JNI prefix requires a matching edit in
`native/android-bridge/src/lib.rs` and a full native rebuild, and an unsynchronised
rename breaks `System.loadLibrary` at runtime with `UnsatisfiedLinkError`. The prompt
requires tracing, not blind renaming, so this is a documented decision, not an omission.

Product identity is AuroraVPN everywhere the user sees it: launcher label, notification
titles, the VPN session name shown in system settings, about and diagnostics strings,
Gradle project name, README.

---

## Definition of Done (§17)

- [x] All old Aurora content removed from the working tree
- [x] No old/legacy/archive folder containing Aurora source inside the repository
- [x] WAM placed as the complete repository base
- [x] Target WAM revision recorded (`7ce2fcb`)
- [x] All real WAM capabilities preserved
- [x] Psiphon/Tor/Chain/carrier/native/transport subsystems not removed
- [x] Aurora branding applied
- [x] Old Aurora UI absent
- [x] GitHub Actions build green
- [x] APK artifact produced
- [x] native/JNI load verification performed
- [x] License/attribution preserved (AGPL-3.0, THIRD_PARTY_NOTICES updated)
- [ ] Raw Functional UI — next phase
- [ ] Capability contract document — next phase
- [ ] Runtime VPN/TUN smoke test — requires a device
- [x] Network Orbit deferred until after functional verification

---

## Next recommended phase

1. **Capability contract** — for each of the 33 capabilities, record backend location,
   state source, setting, action, and UI binding.
2. **Aurora adapter** — a thin layer mapping `EngineStatusStore`, `TrafficMeter` and
   `EngineLog` to an Aurora state model. No new backend.
3. **Raw Functional UI** — Home, Routes, Endpoints, Transport, Identity, Diagnostics,
   Logs, Settings, Carrier/Chain. Capability-complete, not visually complete.
4. **Remove WAM UI** as each Aurora screen replaces it.
5. **Device verification** — the 12 runtime tests, on a real phone.
