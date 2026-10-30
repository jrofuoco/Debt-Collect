package com.jimac.debtcollect.ui.signup

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.jimac.debtcollect.R
import com.jimac.debtcollect.auth.SignupInput

class SignupFragment : Fragment(R.layout.fragment_signup) {
    private lateinit var viewModel: SignupViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[SignupViewModel::class.java]
        val first = view.findViewById<EditText>(R.id.signupFirstName)
        val last = view.findViewById<EditText>(R.id.signupLastName)
        val email = view.findViewById<EditText>(R.id.signupEmail)
        val phone = view.findViewById<EditText>(R.id.signupPhone)
        val password = view.findViewById<EditText>(R.id.signupPassword)
        val confirm = view.findViewById<EditText>(R.id.signupConfirmPassword)
        val terms = view.findViewById<CheckBox>(R.id.signupTerms)
        val button = view.findViewById<Button>(R.id.createAccountButton)
        val progress = view.findViewById<ProgressBar>(R.id.signupProgress)
        val generalError = view.findViewById<TextView>(R.id.signupGeneralError)

        fun submit() = viewModel.submit(SignupInput(
            first.text.toString().trim(), last.text.toString().trim(), email.text.toString().trim(),
            phone.text.toString().filterNot(Char::isWhitespace), password.text.toString(),
            confirm.text.toString(), terms.isChecked,
        ))
        button.setOnClickListener { submit() }
        confirm.setOnEditorActionListener { _, action, _ -> if (action == EditorInfo.IME_ACTION_DONE) { submit(); true } else false }
        view.findViewById<View>(R.id.backToLogin).setOnClickListener { NavHostFragment.findNavController(this).navigateUp() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val v = state.validation
            showError(view, R.id.signupFirstNameError, v.firstNameError)
            showError(view, R.id.signupLastNameError, v.lastNameError)
            showError(view, R.id.signupEmailError, v.emailError)
            showError(view, R.id.signupPhoneError, v.phoneError)
            showError(view, R.id.signupPasswordError, v.passwordError)
            showError(view, R.id.signupConfirmPasswordError, v.confirmPasswordError)
            showError(view, R.id.signupTermsError, v.termsError)
            showTextError(generalError, state.error)
            progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            button.isEnabled = !state.loading
            state.otpChallenge?.let {
                OtpDialogFragment.newInstance(it.destination).show(parentFragmentManager, "otp")
                viewModel.otpDialogShown()
            }
            state.signedInName?.let { name ->
                password.text.clear(); confirm.text.clear()
                viewModel.navigationHandled()
                NavHostFragment.findNavController(this).navigate(
                    R.id.action_signup_to_dashboard,
                    Bundle().apply { putString("displayName", name) },
                )
            }
        }
    }

    private fun showError(root: View, id: Int, error: String?) = showTextError(root.findViewById(id), error)
    private fun showTextError(view: TextView, error: String?) {
        view.text = error.orEmpty(); view.visibility = if (error == null) View.GONE else View.VISIBLE
    }
}
