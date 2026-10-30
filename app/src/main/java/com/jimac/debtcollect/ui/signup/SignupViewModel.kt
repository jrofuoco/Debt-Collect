package com.jimac.debtcollect.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimac.debtcollect.auth.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SignupUiState(
    val loading: Boolean = false,
    val validation: SignupValidation = SignupValidation(),
    val error: String? = null,
    val otpChallenge: OtpChallenge? = null,
    val otpError: String? = null,
    val signedInName: String? = null,
)

class SignupViewModel(private val otpService: OtpService = DemoOtpService()) : ViewModel() {
    private val mutableState = MutableLiveData(SignupUiState())
    val state: LiveData<SignupUiState> = mutableState
    private var pendingInput: SignupInput? = null
    private var challenge: OtpChallenge? = null

    fun submit(input: SignupInput) {
        val validation = SignupValidator.validate(input)
        if (!validation.isValid) {
            mutableState.value = SignupUiState(validation = validation)
            return
        }
        pendingInput = input
        viewModelScope.launch {
            mutableState.value = SignupUiState(loading = true)
            delay(450)
            mutableState.value = when (val result = otpService.requestSignupOtp(input)) {
                is OtpRequestResult.Success -> {
                    challenge = result.challenge
                    SignupUiState(otpChallenge = result.challenge)
                }
                is OtpRequestResult.Error -> SignupUiState(error = result.message)
            }
        }
    }

    fun otpDialogShown() { mutableState.value = SignupUiState() }

    fun verifyOtp(code: String) {
        val active = challenge ?: return
        if (code.length != 6) {
            mutableState.value = SignupUiState(otpError = "Enter the complete six-digit code.")
            return
        }
        mutableState.value = when (val result = otpService.verifySignupOtp(active, code)) {
            OtpVerificationResult.Success -> SignupUiState(
                signedInName = pendingInput?.let { "${it.firstName} ${it.lastName}" }.orEmpty(),
            )
            is OtpVerificationResult.Error -> SignupUiState(otpError = result.message)
        }
    }

    fun resendOtp(): Boolean {
        val active = challenge ?: return false
        return when (val result = otpService.resendSignupOtp(active)) {
            is OtpRequestResult.Success -> { challenge = result.challenge; true }
            is OtpRequestResult.Error -> { mutableState.value = SignupUiState(otpError = result.message); false }
        }
    }

    fun clearOtpError() { mutableState.value = SignupUiState() }
    fun navigationHandled() { mutableState.value = SignupUiState() }
}
