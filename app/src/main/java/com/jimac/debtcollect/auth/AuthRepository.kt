package com.jimac.debtcollect.auth

interface AuthRepository {
    fun signIn(email: String, password: String): AuthResult
}

sealed interface AuthResult {
    data class Success(val displayName: String) : AuthResult
    data class Error(val message: String) : AuthResult
}

class DemoAuthRepository : AuthRepository {
    override fun signIn(email: String, password: String): AuthResult {
        return if (
            email.equals(DEMO_EMAIL, ignoreCase = true) && password == DEMO_PASSWORD
        ) {
            AuthResult.Success(displayName = "Demo Collector")
        } else {
            AuthResult.Error("Email or password is incorrect.")
        }
    }

    companion object {
        const val DEMO_EMAIL = "demo@debtcollect.app"
        const val DEMO_PASSWORD = "DebtCollect1!"
    }
}
