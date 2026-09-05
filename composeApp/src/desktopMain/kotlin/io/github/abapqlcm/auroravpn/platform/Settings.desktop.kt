package io.github.abapqlcm.auroravpn.platform

import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.*

class DesktopSettings private constructor() : Settings {
    private val props = Properties()
    private val file: File
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = Any()

    init {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val baseDir = if (isWindows) {
            System.getenv("AppData") ?: System.getProperty("user.home")
        } else {
            System.getProperty("user.home") + "/.config"
        }
        val dir = File(baseDir, "AuroraVPN-Tunnel")
        if (!dir.exists()) dir.mkdirs()
        file = File(dir, "settings.properties")
        // Migration: copy from legacy AetherST-Tunnel if new file empty/missing
        try {
            val legacyDir = File(baseDir, "AetherST-Tunnel")
            val legacyFile = File(legacyDir, "settings.properties")
            if (!file.exists() && legacyFile.exists()) {
                legacyFile.copyTo(file, overwrite = false)
            }
        } catch (_: Exception) {}
    }

    init {
        if (file.exists()) {
            try {
                synchronized(lock) { FileInputStream(file).use { props.load(it) } }
            } catch (_: Exception) {
                
            }
        }
    }

    private fun save() {
        val snapshot = synchronized(lock) { Properties().also { it.putAll(props) } }
        scope.launch {
            try {
                synchronized(lock) { FileOutputStream(file).use { snapshot.store(it, null) } }
            } catch (_: Exception) {
                
            }
        }
    }

    override fun getString(key: String, defaultValue: String): String = synchronized(lock) { props.getProperty(key, defaultValue) ?: defaultValue }
    override fun putString(key: String, value: String) { synchronized(lock) { props.setProperty(key, value) }; save() }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = synchronized(lock) { props.getProperty(key, defaultValue.toString()).toBoolean() }
    override fun putBoolean(key: String, value: Boolean) { synchronized(lock) { props.setProperty(key, value.toString()) }; save() }
    override fun getInt(key: String, defaultValue: Int): Int = synchronized(lock) { props.getProperty(key, defaultValue.toString()).toIntOrNull() ?: defaultValue }
    override fun putInt(key: String, value: Int) { synchronized(lock) { props.setProperty(key, value.toString()) }; save() }
    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        synchronized(lock) {
            val value = props.getProperty(key) ?: return defaultValue
            return value.split(",").filter { it.isNotEmpty() }.toSet()
        }
    }
    override fun putStringSet(key: String, value: Set<String>) { synchronized(lock) { props.setProperty(key, value.joinToString(",")) }; save() }
    override fun contains(key: String): Boolean = synchronized(lock) { props.containsKey(key) }

    companion object {
        @Volatile
        private var instance: DesktopSettings? = null
        fun getInstance(): DesktopSettings =
            instance ?: synchronized(this) {
                instance ?: DesktopSettings().also { instance = it }
            }
    }
}

actual fun getSettings(context: PlatformContext): Settings = DesktopSettings.getInstance()
