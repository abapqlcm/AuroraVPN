package io.github.abapqlcm.auroravpn.shared

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.abapqlcm.auroravpn.platform.PlatformContext
import io.github.abapqlcm.auroravpn.shared.i18n.LocalAppStrings
import io.github.abapqlcm.auroravpn.shared.i18n.getEffectiveStrings
import io.github.abapqlcm.auroravpn.shared.ui.AetherViewModel
import io.github.abapqlcm.auroravpn.shared.ui.OnboardingViewModel
import io.github.abapqlcm.auroravpn.shared.ui.screens.MainScreen
import io.github.abapqlcm.auroravpn.shared.ui.theme.MyApplicationTheme

@Composable
fun App(context: PlatformContext) {
    val viewModel: AetherViewModel = viewModel(key = "aether_main_vm") { AetherViewModel(context) }
    val onboardingViewModel: OnboardingViewModel = viewModel(key = "onboarding_vm") { OnboardingViewModel(context) }
    val config by viewModel.config.collectAsStateWithLifecycle()
    val strings = getEffectiveStrings(config.appLanguage)
    val isRtl = config.resolvedLanguage() == "fa"
    CompositionLocalProvider(LocalAppStrings provides strings) {
        MyApplicationTheme(isRtl = isRtl) {
            MainScreen(viewModel, onboardingViewModel, context)
        }
    }
}
