package com.jimac.debtcollect.auth

import android.util.Patterns

data class SignupInput(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String,
    val acceptedTerms: Boolean,
)

data class SignupValidation(
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
) {
    val isValid: Boolean
        get() = listOf(
            firstNameError,
            lastNameError,
            emailError,
            phoneError,
            passwordError,
            confirmPasswordError,
            termsError,
        ).all { it == null }
}

object SignupValidator {
    fun validate(input: SignupInput): SignupValidation {
        val passwordError = when {
            input.password.isBlank() -> "Create a password."
            input.password.length < 8 -> "Use at least 8 characters."
            input.password.none(Char::isUpperCase) -> "Add at least one uppercase letter."
            input.password.none(Char::isLowerCase) -> "Add at least one lowercase letter."
            input.password.none(Char::isDigit) -> "Add at least one number."
            else -> null
        }

        return SignupValidation(
            firstNameError = if (input.firstName.length < 2) "Enter your first name." else null,
            lastNameError = if (input.lastName.length < 2) "Enter your last name." else null,
            emailError = when {
                input.email.isBlank() -> "Enter your email address."
                !Patterns.EMAIL_ADDRESS.matcher(input.email).matches() -> "Enter a valid email address."
                else -> null
            },
            phoneError = if (!input.phone.matches(Regex("^\\+?[0-9]{10,15}$"))) {
                "Enter a valid phone number with 10–15 digits."
            } else null,
            passwordError = passwordError,
            confirmPasswordError = when {
                input.confirmPassword.isBlank() -> "Confirm your password."
                input.confirmPassword != input.password -> "Passwords do not match."
                else -> null
            },
            termsError = if (!input.acceptedTerms) "Accept the terms to continue." else null,
        )
    }
}
