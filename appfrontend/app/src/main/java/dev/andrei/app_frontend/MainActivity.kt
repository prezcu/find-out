package dev.andrei.app_frontend

import dev.andrei.app_frontend.ui.navigation.AppNavHost
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.andrei.app_frontend.data.repository.SessionRepository
import dev.andrei.app_frontend.ui.theme.AppfrontendTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            sessionRepository.onPermissionResult(isGranted)
            if (isGranted) {
                lifecycleScope.launch { sessionRepository.updateDeviceLocation() }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            sessionRepository.onPermissionResult(true)
            lifecycleScope.launch { sessionRepository.updateDeviceLocation() }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            AppfrontendTheme {
                AppNavHost()
            }
        }
    }
}