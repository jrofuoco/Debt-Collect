package com.jimac.debtcollect.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimac.debtcollect.auth.AuthRepository
import com.jimac.debtcollect.auth.AuthResult
import com.jimac.debtcollect.auth.DemoAuthRepository
import com.jimac.debtcollect.auth.LoginValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val signedInName: String? = null,
)

class LoginViewModel(
    private val repository: AuthRepository = DemoAuthRepository(),
) : ViewModel() {
    private val mutableState = MutableLiveData(LoginUiState())
    val state: LiveData<LoginUiState> = mutableState

    fun signIn(email: String, password: String) {
        val validation = LoginValidator.validate(email, password)
        if (!validation.isValid) {
            mutableState.value = LoginUiState(
                emailError = validation.emailError,
                passwordError = validation.passwordError,
            )
            return
        }
        viewModelScope.launch {
            mutableState.value = LoginUiState(loading = true)
            delay(550)
            mutableState.value = when (val result = repository.signIn(email, password)) {
                is AuthResult.Success -> LoginUiState(signedInName = result.displayName)
                is AuthResult.Error -> LoginUiState(error = result.message)
            }
        }
    }

    fun clearTransientState() { mutableState.value = LoginUiState() }
}
