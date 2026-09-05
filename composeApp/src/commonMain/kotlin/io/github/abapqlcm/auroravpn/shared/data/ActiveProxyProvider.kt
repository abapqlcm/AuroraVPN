package io.github.abapqlcm.auroravpn.shared.data

object ActiveProxyProvider {
    @Volatile
    var psiphonProxyUrl: String? = null
}
