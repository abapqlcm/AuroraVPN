package io.github.abapqlcm.auroravpn.shared.core

import io.github.abapqlcm.auroravpn.platform.PlatformContext

interface BinaryManager {
    fun prepareBinary(name: String = "aether"): String
}

expect fun getBinaryManager(context: PlatformContext): BinaryManager
