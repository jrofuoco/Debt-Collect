package com.jimac.debtcollect.ui.login

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.jimac.debtcollect.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var viewModel: LoginViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        val email = view.findViewById<EditText>(R.id.emailInput)
        val password = view.findViewById<EditText>(R.id.passwordInput)
        val emailError = view.findViewById<TextView>(R.id.emailError)
        val passwordError = view.findViewById<TextView>(R.id.passwordError)
        val generalError = view.findViewById<TextView>(R.id.loginError)
        val remember = view.findViewById<CheckBox>(R.id.rememberEmail)
        val button = view.findViewById<Button>(R.id.signInButton)
        val progress = view.findViewById<ProgressBar>(R.id.loginProgress)
        val preferences = requireContext().getSharedPreferences("login_preferences", 0)
        preferences.getString("remembered_email", null)?.let {
            email.setText(it)
            remember.isChecked = true
        }

        fun submit() = viewModel.signIn(email.text.toString().trim(), password.text.toString())
        button.setOnClickListener { submit() }
        password.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { submit(); true } else false
        }
        view.findViewById<TextView>(R.id.passwordToggle).setOnClickListener { toggle ->
            val visible = password.transformationMethod == null
            password.transformationMethod = if (visible) PasswordTransformationMethod.getInstance() else null
            password.inputType = InputType.TYPE_CLASS_TEXT or if (visible) InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            (toggle as TextView).setText(if (visible) R.string.show_password else R.string.hide_password)
            password.setSelection(password.text.length)
        }
        view.findViewById<View>(R.id.createAccount).setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_login_to_signup)
        }
        view.findViewById<View>(R.id.forgotPassword).setOnClickListener {
            Toast.makeText(context, R.string.password_reset_not_connected, Toast.LENGTH_LONG).show()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            showError(emailError, state.emailError)
            showError(passwordError, state.passwordError)
            showError(generalError, state.error)
            progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            button.isEnabled = !state.loading
            state.signedInName?.let { name ->
                if (remember.isChecked) preferences.edit().putString("remembered_email", email.text.toString().trim()).apply()
                else preferences.edit().remove("remembered_email").apply()
                viewModel.clearTransientState()
                NavHostFragment.findNavController(this).navigate(
                    R.id.action_login_to_dashboard,
                    Bundle().apply { putString("displayName", name) },
                )
            }
        }
    }

    private fun showError(view: TextView, error: String?) {
        view.text = error.orEmpty()
        view.visibility = if (error == null) View.GONE else View.VISIBLE
    }
}
