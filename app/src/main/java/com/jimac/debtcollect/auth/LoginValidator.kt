package com.jimac.debtcollect.auth

import android.util.Patterns

data class LoginValidation(
    val emailError: String? = null,
    val passwordError: String? = null,
) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

object LoginValidator {
    fun validate(email: String, password: String): LoginValidation {
        val emailError = when {
            email.isBlank() -> "Enter your email address."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Enter your password."
            password.length < 8 -> "Password must contain at least 8 characters."
            else -> null
        }
        return LoginValidation(emailError, passwordError)
    }
}
