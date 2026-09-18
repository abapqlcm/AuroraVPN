<div align="center">

# AuroraVPN

**Android client for the Aether encrypted route engine.**

[![License](https://img.shields.io/badge/license-AGPL--3.0-34d1a6?style=flat-square)](LICENSE)
[![CI](https://img.shields.io/github/actions/workflow/status/abapqlcm/AuroraVPN/ci.yml?branch=main&style=flat-square&label=CI)](https://github.com/abapqlcm/AuroraVPN/actions/workflows/ci.yml)

</div>

AuroraVPN is an anti-censorship Android VPN client built on the
[Aether](https://github.com/CluvexStudio/Aether) engine, embedded directly as a JNI
library rather than run as a subprocess.

## Provenance and licensing

AuroraVPN is a fork of [WhiteAestherMobile](https://github.com/WhiteDNS/WhiteAestherMobile)
at revision `7ce2fcb`, carrying its full backend: the Aether engine and JNI bridge, the
carrier and exit-chain subsystems, the endpoint prober, route memory, and the transport
ladder. No WAM capability was removed to shrink the build.

Both AuroraVPN and WhiteAestherMobile are **AGPL-3.0**, as is Aether. The complete
source for this build is published here, and the third-party notices carry the
attribution for every embedded component: see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Building

```bash
git clone https://github.com/abapqlcm/AuroraVPN.git
cd AuroraVPN
./gradlew assemblePreviewDebug
```

The native core builds via `cargo-ndk` as part of the Gradle build. The Go engines (the
exit chain, Psiphon, and the Tor pluggable transports) are built by the scripts under
`native/chain`, `native/psiphon` and `native/tor`, and are also produced by CI.

## Downloads

Grab the APK for your phone from the [latest release](https://github.com/abapqlcm/AuroraVPN/releases/latest).
