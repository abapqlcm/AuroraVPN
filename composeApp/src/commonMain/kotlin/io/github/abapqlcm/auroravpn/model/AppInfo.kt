package io.github.abapqlcm.auroravpn.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: String? = null,
    val isSystemApp: Boolean
)
