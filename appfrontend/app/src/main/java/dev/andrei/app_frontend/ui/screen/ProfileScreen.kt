package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.viewmodel.ProfileScreenViewmodel

@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileScreenViewmodel = hiltViewModel()
) {
    val state by viewModel.logInState.collectAsStateWithLifecycle()

    viewModel.updateLogInState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = if (state) onLogout else onLogin,
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(if (state) "Logout" else "Login")
        }
    }
}
