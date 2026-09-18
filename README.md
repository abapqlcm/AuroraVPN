<div align="center">

# AuroraVPN

**A censorship-circumvention client for Android, powered by the [Aether](https://github.com/CluvexStudio/Aether) engine.**

Built from scratch around the Aether core compiled as a JNI library — the engine
lives inside the app rather than being spawned as a subprocess, which is what
keeps it running on modern Android.

[![CI](https://img.shields.io/github/actions/workflow/status/abapqlcm/AuroraVPN/ci.yml?branch=main&style=flat-square&label=CI)](https://github.com/abapqlcm/AuroraVPN/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/abapqlcm/AuroraVPN?style=flat-square&color=34d1a6)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3ddc84?style=flat-square&logo=android&logoColor=white)](#requirements)
[![Phase](https://img.shields.io/badge/phase-1%20skeleton-FFD700?style=flat-square)](#roadmap)

</div>

---

> **وضعیت:** این پروژه در مرحلهٔ اول توسعه است. هنوز موتور اتصال به آن وصل
> نشده — فعلاً فقط اسکلت برنامه، تنظیمات بیلد و CI کار می‌کنند. نسخهٔ قابل
> استفاده در [راهنمای پروژه](#roadmap) مشخص است.

## Why this exists

Aether already works in Iran — it finds a reachable Cloudflare endpoint, completes
an authenticated MASQUE handshake, and only trusts a route once real traffic
returns through it. What is missing is a good Android client around it. This
project is that client, with an interface designed from scratch rather than
inherited from a template.

The previous attempt at this app ran the engine as a subprocess. That approach is
dead on modern Android: the platform refuses to execute binaries from app-private
storage, so the tunnel could never start. This rebuild embeds the Aether core as
a JNI library inside the APK instead, which is how
[WhiteAestherMobile](https://github.com/WhiteDNS/WhiteAestherMobile) does it.

## Architecture

```
┌─ UI (Jetpack Compose) ────────────────────────┐
│  Status, routing, diagnostics, split tunnel   │
├─ AuroraVpnService (Kotlin) ───────────────────┤
│  VpnService.Builder → tun fd                  │
├─ android-bridge (Rust JNI) ───────────────────┤
│  fd + config → engine, callbacks back up      │
├─ Aether core (Rust) ──────────────────────────┤
│  MASQUE / WireGuard / Gool, endpoint scan     │
└───────────────────────────────────────────────┘
```

The engine and the JNI bridge are vendored and left alone. Everything we write
sits above them.

## Build

```bash
git clone https://github.com/abapqlcm/AuroraVPN.git
cd AuroraVPN
./gradlew :app:assembleStableDebug
```

Needs JDK 17 and Android SDK 36. CI performs the same build on every push.

## Roadmap

| Phase | Contents | Status |
| --- | --- | --- |
| **1 — Skeleton** | Clean repo, Gradle toolchain, CI, installable APK | ✅ Done |
| **2 — Engine** | Aether core + JNI bridge built into the APK | ⏳ Next |
| **3 — Tunnel** | `VpnService`, foreground service, real connection | ⏳ |
| **4 — Interface** | Custom UI, animations, routing, split tunnel | ⏳ |

## Requirements

Android 8.0 (API 26) or newer. `arm64-v8a` is the primary target.

## License

[AGPL-3.0](LICENSE). This project embeds the Aether engine, which is AGPL-3.0, so
the same license applies to the whole app. See `THIRD_PARTY_NOTICES.md` for the
components and the revisions they are pinned to.

## Credits

- **[Aether](https://github.com/CluvexStudio/Aether)** by CluvexStudio — the engine this client is built on.
- **[WhiteAestherMobile](https://github.com/WhiteDNS/WhiteAestherMobile)** by WhiteDNS — the reference for embedding Aether on Android.
