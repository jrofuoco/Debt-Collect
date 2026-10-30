package com.jimac.debtcollect.auth

data class OtpChallenge(val id: String, val destination: String)

sealed interface OtpRequestResult {
    data class Success(val challenge: OtpChallenge) : OtpRequestResult
    data class Error(val message: String) : OtpRequestResult
}

sealed interface OtpVerificationResult {
    data object Success : OtpVerificationResult
    data class Error(val message: String) : OtpVerificationResult
}

interface OtpService {
    fun requestSignupOtp(input: SignupInput): OtpRequestResult
    fun resendSignupOtp(challenge: OtpChallenge): OtpRequestResult
    fun verifySignupOtp(challenge: OtpChallenge, code: String): OtpVerificationResult
}

class DemoOtpService : OtpService {
    override fun requestSignupOtp(input: SignupInput): OtpRequestResult =
        OtpRequestResult.Success(OtpChallenge("demo-signup", input.email))

    override fun resendSignupOtp(challenge: OtpChallenge): OtpRequestResult =
        OtpRequestResult.Success(challenge)

    override fun verifySignupOtp(
        challenge: OtpChallenge,
        code: String,
    ): OtpVerificationResult = if (code == DEMO_OTP) {
        OtpVerificationResult.Success
    } else {
        OtpVerificationResult.Error("That verification code is incorrect.")
    }

    companion object {
        const val DEMO_OTP = "123456"
    }
}
