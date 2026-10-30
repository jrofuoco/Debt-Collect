package com.jimac.debtcollect

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.jimac.debtcollect.auth.AuthRepository
import com.jimac.debtcollect.auth.AuthResult
import com.jimac.debtcollect.auth.DemoAuthRepository
import com.jimac.debtcollect.auth.LoginValidator

class MainActivity : AppCompatActivity() {
    private val authRepository: AuthRepository = DemoAuthRepository()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<View>(R.id.logoMark)
        val halo = findViewById<View>(R.id.logoHalo)
        val brandName = findViewById<View>(R.id.brandName)
        val tagline = findViewById<View>(R.id.tagline)
        val splash = findViewById<View>(R.id.splashContent)
        val login = findViewById<View>(R.id.loginContent)

        setupLogin(login)

        logo.scaleX = 0.25f
        logo.scaleY = 0.25f
        logo.alpha = 0f
        halo.scaleX = 0.78f
        halo.scaleY = 0.78f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(180)
            .setDuration(620)
            .setInterpolator(DecelerateInterpolator())
            .start()

        halo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(180)
            .setDuration(720)
            .setInterpolator(DecelerateInterpolator())
            .start()

        brandName.animate().alpha(1f).translationY(-5f)
            .setStartDelay(520).setDuration(420).start()
        tagline.animate().alpha(1f)
            .setStartDelay(700).setDuration(420).start()

        mainHandler.postDelayed({
            splash.animate()
                .alpha(0f)
                .setDuration(360)
                .withEndAction {
                    splash.visibility = View.GONE
                    login.visibility = View.VISIBLE
                    login.translationY = 34f
                    login.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(520)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                .start()
        }, 2200)
    }

    private fun setupLogin(loginContent: View) {
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val emailError = findViewById<TextView>(R.id.emailError)
        val passwordError = findViewById<TextView>(R.id.passwordError)
        val loginError = findViewById<TextView>(R.id.loginError)
        val passwordToggle = findViewById<TextView>(R.id.passwordToggle)
        val rememberEmail = findViewById<CheckBox>(R.id.rememberEmail)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val progress = findViewById<ProgressBar>(R.id.loginProgress)
        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

        preferences.getString(KEY_REMEMBERED_EMAIL, null)?.let { rememberedEmail ->
            emailInput.setText(rememberedEmail)
            rememberEmail.isChecked = true
        }

        emailInput.doAfterTextChanged {
            emailError.visibility = View.GONE
            loginError.visibility = View.GONE
        }
        passwordInput.doAfterTextChanged {
            passwordError.visibility = View.GONE
            loginError.visibility = View.GONE
        }

        passwordToggle.setOnClickListener {
            val passwordIsVisible = passwordInput.transformationMethod == null
            if (passwordIsVisible) {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.text = getString(R.string.show_password)
            } else {
                passwordInput.transformationMethod = null
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.text = getString(R.string.hide_password)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }

        fun submitLogin() {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val validation = LoginValidator.validate(email, password)

            showFieldError(emailError, validation.emailError)
            showFieldError(passwordError, validation.passwordError)
            loginError.visibility = View.GONE
            if (!validation.isValid) return

            currentFocus?.let { focusedView ->
                getSystemService(InputMethodManager::class.java)
                    .hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
            signInButton.isEnabled = false
            progress.visibility = View.VISIBLE

            mainHandler.postDelayed({
                when (val result = authRepository.signIn(email, password)) {
                    is AuthResult.Success -> {
                        if (rememberEmail.isChecked) {
                            preferences.edit().putString(KEY_REMEMBERED_EMAIL, email).apply()
                        } else {
                            preferences.edit().remove(KEY_REMEMBERED_EMAIL).apply()
                        }
                        showSignedInState(loginContent, result.displayName)
                    }

                    is AuthResult.Error -> {
                        loginError.text = result.message
                        loginError.visibility = View.VISIBLE
                        progress.visibility = View.GONE
                        signInButton.isEnabled = true
                    }
                }
            }, LOGIN_DELAY_MS)
        }

        signInButton.setOnClickListener { submitLogin() }
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin()
                true
            } else {
                false
            }
        }

        findViewById<View>(R.id.forgotPassword).setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_connected, Toast.LENGTH_LONG).show()
        }
        findViewById<View>(R.id.createAccount).setOnClickListener {
            Toast.makeText(this, R.string.registration_not_connected, Toast.LENGTH_LONG).show()
        }
    }

    private fun showFieldError(errorView: TextView, error: String?) {
        errorView.text = error.orEmpty()
        errorView.visibility = if (error == null) View.GONE else View.VISIBLE
    }

    private fun showSignedInState(loginContent: View, displayName: String) {
        val signedInContent = findViewById<View>(R.id.signedInContent)
        findViewById<TextView>(R.id.signedInTitle).text = getString(R.string.signed_in_title, displayName)

        loginContent.animate()
            .alpha(0f)
            .setDuration(240)
            .withEndAction {
                loginContent.visibility = View.GONE
                signedInContent.alpha = 0f
                signedInContent.scaleX = 0.96f
                signedInContent.scaleY = 0.96f
                signedInContent.visibility = View.VISIBLE
                signedInContent.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(360)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()

        findViewById<View>(R.id.signOutButton).setOnClickListener {
            signedInContent.visibility = View.GONE
            loginContent.alpha = 1f
            loginContent.visibility = View.VISIBLE
            findViewById<EditText>(R.id.passwordInput).text.clear()
            findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
            findViewById<Button>(R.id.signInButton).isEnabled = true
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "login_preferences"
        private const val KEY_REMEMBERED_EMAIL = "remembered_email"
        private const val LOGIN_DELAY_MS = 550L
    }
}
