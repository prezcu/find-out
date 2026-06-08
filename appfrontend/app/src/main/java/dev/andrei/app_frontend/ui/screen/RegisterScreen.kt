package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import dev.andrei.app_frontend.ui.viewmodel.RegisterScreenViewModel

private val EMAIL_RE = Regex("\\S+@\\S+\\.\\S+")

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterScreenViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onRegisterSuccess() }

    val pwOk = state.password.length >= 8
    val mismatch = state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword
    val valid = EMAIL_RE.matches(state.email) &&
        state.username.length >= 2 &&
        pwOk &&
        state.password == state.confirmPassword

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))
        Kicker("Findout · Bucharest", color = c.accent)
        Spacer(Modifier.height(10.dp))
        Text("Create account", style = FindoutType.hero.copy(fontSize = 44.sp), color = c.ink)
        Spacer(Modifier.height(2.dp))
        Text("Sign up to start exploring.", style = FindoutType.bodyItalic.copy(fontSize = 17.sp), color = c.sub)
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
                label = "Username",
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                enabled = !state.isLoading,
                imeAction = ImeAction.Next
            )
            Column {
                FindoutTextField(
                    label = "Password",
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    enabled = !state.isLoading,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
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
                Spacer(Modifier.height(6.dp))
                Text(
                    if (pwOk) "✓ at least 8 characters" else "at least 8 characters",
                    style = FindoutType.mono.copy(fontSize = 10.sp),
                    color = if (pwOk) c.sage else c.faint,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            FindoutTextField(
                label = "Confirm password",
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { if (valid) viewModel.submit() },
                visualTransformation = passwordTransform(state.isPasswordVisible)
            )
            if (mismatch) {
                Text("passwords don't match", style = FindoutType.mono.copy(fontSize = 10.5.sp), color = c.accent2, modifier = Modifier.padding(start = 2.dp))
            }
            state.errorMessage?.let {
                Text(it, style = FindoutType.mono.copy(fontSize = 10.5.sp), color = c.accent2, modifier = Modifier.padding(start = 2.dp))
            }
        }

        Spacer(Modifier.height(22.dp))
        FindoutPrimaryButton(
            label = "Create account →",
            onClick = viewModel::submit,
            enabled = valid,
            loading = state.isLoading
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Already have an account? ", style = FindoutType.button.copy(fontSize = 13.5.sp), color = c.sub)
            Text(
                "Sign in",
                style = FindoutType.button.copy(fontSize = 13.5.sp),
                color = c.accent,
                modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToLogin() }
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
