package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.components.FindoutPrimaryButton
import dev.andrei.app_frontend.ui.components.FindoutTextField
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.passwordTransform
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.viewmodel.LoginScreenViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onLoginSuccess() }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Kicker("Findout · Bucharest", color = c.accent)
        Spacer(Modifier.height(10.dp))
        Text("Welcome back", style = FindoutType.hero.copy(fontSize = 44.sp), color = c.ink)
        Spacer(Modifier.height(2.dp))
        Text("Sign in to continue.", style = FindoutType.bodyItalic.copy(fontSize = 17.sp), color = c.sub)
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FindoutTextField(
                label = "Email",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            FindoutTextField(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::submit,
                visualTransformation = passwordTransform(state.isPasswordVisible),
                trailing = {
                    Text(
                        if (state.isPasswordVisible) "Hide" else "Show",
                        style = FindoutType.button.copy(fontSize = 12.5.sp),
                        color = c.accent,
                        modifier = Modifier.clickable { viewModel.togglePasswordVisibility() }
                    )
                }
            )
            state.errorMessage?.let {
                Text(it, style = FindoutType.mono.copy(fontSize = 10.5.sp), color = c.accent2, modifier = Modifier.padding(start = 2.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        FindoutPrimaryButton(
            label = "Sign in →",
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            loading = state.isLoading
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Don't have an account? ", style = FindoutType.button.copy(fontSize = 13.5.sp), color = c.sub)
            Text(
                "Sign up",
                style = FindoutType.button.copy(fontSize = 13.5.sp),
                color = c.accent,
                modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToRegister() }
            )
        }
    }
}
