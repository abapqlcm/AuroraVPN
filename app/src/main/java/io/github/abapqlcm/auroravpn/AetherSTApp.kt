package io.github.abapqlcm.auroravpn

import android.app.Application
import androidx.annotation.Keep
import io.github.abapqlcm.auroravpn.core.ConnectionController
import io.github.abapqlcm.auroravpn.service.AetherWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@Keep
class AuroraVPNApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        observeStatusForWidgets()
        
        io.github.abapqlcm.auroravpn.shared.platform.Bridge.submitLoginCode = { code ->
            ConnectionController.getInstance(this).submitLoginCode(code)
        }
    }

    private fun observeStatusForWidgets() {
        applicationScope.launch {
            ConnectionController.status.collect {
                AetherWidgetProvider.updateAllWidgets(this@AuroraVPNApp)
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                val crashLog = "Thread: ${thread.name}\n\nException: ${throwable.localizedMessage}\n\nStack Trace:\n$stackTrace"
                val file = File(cacheDir, "last_crash.log")
                file.writeText(crashLog)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
