package com.jimac.debtcollect

import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.jimac.debtcollect.auth.AuthRepository
import com.jimac.debtcollect.auth.AuthResult
import com.jimac.debtcollect.auth.DemoAuthRepository
import com.jimac.debtcollect.auth.LoginValidator
import com.jimac.debtcollect.auth.DemoOtpService
import com.jimac.debtcollect.auth.OtpChallenge
import com.jimac.debtcollect.auth.OtpRequestResult
import com.jimac.debtcollect.auth.OtpService
import com.jimac.debtcollect.auth.OtpVerificationResult
import com.jimac.debtcollect.auth.SignupInput
import com.jimac.debtcollect.auth.SignupValidator

class MainActivity : AppCompatActivity() {
    private val authRepository: AuthRepository = DemoAuthRepository()
    private val otpService: OtpService = DemoOtpService()
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
        setupSignup(login)

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
            showSignup(loginContent)
        }
    }

    private fun setupSignup(loginContent: View) {
        val signupContent = findViewById<View>(R.id.signupContent)
        val firstName = findViewById<EditText>(R.id.signupFirstName)
        val lastName = findViewById<EditText>(R.id.signupLastName)
        val email = findViewById<EditText>(R.id.signupEmail)
        val phone = findViewById<EditText>(R.id.signupPhone)
        val password = findViewById<EditText>(R.id.signupPassword)
        val confirmPassword = findViewById<EditText>(R.id.signupConfirmPassword)
        val terms = findViewById<CheckBox>(R.id.signupTerms)
        val generalError = findViewById<TextView>(R.id.signupGeneralError)
        val createButton = findViewById<Button>(R.id.createAccountButton)
        val progress = findViewById<ProgressBar>(R.id.signupProgress)

        val inputErrorPairs = listOf(
            firstName to findViewById<TextView>(R.id.signupFirstNameError),
            lastName to findViewById(R.id.signupLastNameError),
            email to findViewById(R.id.signupEmailError),
            phone to findViewById(R.id.signupPhoneError),
            password to findViewById(R.id.signupPasswordError),
            confirmPassword to findViewById(R.id.signupConfirmPasswordError),
        )
        inputErrorPairs.forEach { (input, errorView) ->
            input.doAfterTextChanged {
                errorView.visibility = View.GONE
                generalError.visibility = View.GONE
            }
        }
        terms.setOnCheckedChangeListener { _, _ ->
            findViewById<View>(R.id.signupTermsError).visibility = View.GONE
        }

        fun submitSignup() {
            val input = SignupInput(
                firstName = firstName.text.toString().trim(),
                lastName = lastName.text.toString().trim(),
                email = email.text.toString().trim(),
                phone = phone.text.toString().filterNot(Char::isWhitespace),
                password = password.text.toString(),
                confirmPassword = confirmPassword.text.toString(),
                acceptedTerms = terms.isChecked,
            )
            val validation = SignupValidator.validate(input)
            showFieldError(findViewById(R.id.signupFirstNameError), validation.firstNameError)
            showFieldError(findViewById(R.id.signupLastNameError), validation.lastNameError)
            showFieldError(findViewById(R.id.signupEmailError), validation.emailError)
            showFieldError(findViewById(R.id.signupPhoneError), validation.phoneError)
            showFieldError(findViewById(R.id.signupPasswordError), validation.passwordError)
            showFieldError(findViewById(R.id.signupConfirmPasswordError), validation.confirmPasswordError)
            showFieldError(findViewById(R.id.signupTermsError), validation.termsError)
            generalError.visibility = View.GONE
            if (!validation.isValid) return

            currentFocus?.let { focusedView ->
                getSystemService(InputMethodManager::class.java)
                    .hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
            createButton.isEnabled = false
            progress.visibility = View.VISIBLE

            mainHandler.postDelayed({
                when (val result = otpService.requestSignupOtp(input)) {
                    is OtpRequestResult.Success -> {
                        progress.visibility = View.GONE
                        createButton.isEnabled = true
                        showOtpDialog(result.challenge, input, signupContent)
                    }
                    is OtpRequestResult.Error -> {
                        generalError.text = result.message
                        generalError.visibility = View.VISIBLE
                        progress.visibility = View.GONE
                        createButton.isEnabled = true
                    }
                }
            }, OTP_REQUEST_DELAY_MS)
        }

        createButton.setOnClickListener { submitSignup() }
        confirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitSignup()
                true
            } else false
        }
        findViewById<View>(R.id.backToLogin).setOnClickListener {
            showLogin(signupContent, loginContent)
        }
    }

    private fun showOtpDialog(
        initialChallenge: OtpChallenge,
        input: SignupInput,
        signupContent: View,
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_otp, null)
        val otpInput = dialogView.findViewById<EditText>(R.id.otpInput)
        val otpError = dialogView.findViewById<TextView>(R.id.otpError)
        val destination = dialogView.findViewById<TextView>(R.id.otpDestination)
        val resend = dialogView.findViewById<TextView>(R.id.resendOtp)
        var challenge = initialChallenge
        var resendTimer: CountDownTimer? = null

        destination.text = getString(R.string.otp_sent_to, initialChallenge.destination)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.verify_email)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.verify, null)
            .create()

        fun startResendCooldown() {
            resend.isEnabled = false
            resend.alpha = 0.55f
            resendTimer?.cancel()
            resendTimer = object : CountDownTimer(OTP_RESEND_COOLDOWN_MS, 1_000L) {
                override fun onTick(millisUntilFinished: Long) {
                    resend.text = getString(R.string.resend_in, millisUntilFinished / 1_000L + 1L)
                }

                override fun onFinish() {
                    resend.text = getString(R.string.resend_code)
                    resend.isEnabled = true
                    resend.alpha = 1f
                }
            }.start()
        }

        dialog.setOnShowListener {
            startResendCooldown()
            otpInput.requestFocus()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val code = otpInput.text.toString()
                if (code.length != OTP_LENGTH) {
                    showFieldError(otpError, getString(R.string.enter_six_digit_code))
                    return@setOnClickListener
                }
                when (val result = otpService.verifySignupOtp(challenge, code)) {
                    OtpVerificationResult.Success -> {
                        resendTimer?.cancel()
                        dialog.dismiss()
                        clearSignupSecrets()
                        showSignedInState(signupContent, "${input.firstName} ${input.lastName}")
                    }
                    is OtpVerificationResult.Error -> showFieldError(otpError, result.message)
                }
            }
            resend.setOnClickListener {
                when (val result = otpService.resendSignupOtp(challenge)) {
                    is OtpRequestResult.Success -> {
                        challenge = result.challenge
                        otpInput.text.clear()
                        otpError.visibility = View.GONE
                        Toast.makeText(this, R.string.code_resent, Toast.LENGTH_SHORT).show()
                        startResendCooldown()
                    }
                    is OtpRequestResult.Error -> showFieldError(otpError, result.message)
                }
            }
        }
        dialog.setOnDismissListener { resendTimer?.cancel() }
        dialog.show()
    }

    private fun showSignup(loginContent: View) {
        val signupContent = findViewById<View>(R.id.signupContent)
        loginContent.visibility = View.GONE
        signupContent.alpha = 0f
        signupContent.translationX = 36f
        signupContent.visibility = View.VISIBLE
        signupContent.animate().alpha(1f).translationX(0f).setDuration(280).start()
    }

    private fun showLogin(signupContent: View, loginContent: View) {
        signupContent.visibility = View.GONE
        loginContent.alpha = 1f
        loginContent.translationX = 0f
        loginContent.visibility = View.VISIBLE
    }

    private fun clearSignupSecrets() {
        findViewById<EditText>(R.id.signupPassword).text.clear()
        findViewById<EditText>(R.id.signupConfirmPassword).text.clear()
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
        private const val OTP_REQUEST_DELAY_MS = 450L
        private const val OTP_RESEND_COOLDOWN_MS = 30_000L
        private const val OTP_LENGTH = 6
    }
}
