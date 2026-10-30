package com.jimac.debtcollect.ui.signup

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.jimac.debtcollect.R

class OtpDialogFragment : DialogFragment() {
    private lateinit var viewModel: SignupViewModel
    private var timer: CountDownTimer? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[SignupViewModel::class.java]
        val content = layoutInflater.inflate(R.layout.dialog_otp, null)
        val input = content.findViewById<EditText>(R.id.otpInput)
        val error = content.findViewById<TextView>(R.id.otpError)
        val resend = content.findViewById<TextView>(R.id.resendOtp)
        content.findViewById<TextView>(R.id.otpDestination).text =
            getString(R.string.otp_sent_to, requireArguments().getString(ARG_DESTINATION).orEmpty())
        val dialog = AlertDialog.Builder(requireContext()).setTitle(R.string.verify_email)
            .setView(content).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.verify, null).create()
        dialog.setOnShowListener {
            startCooldown(resend)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { viewModel.verifyOtp(input.text.toString()) }
            resend.setOnClickListener {
                if (viewModel.resendOtp()) {
                    input.text.clear(); error.visibility = View.GONE
                    Toast.makeText(context, R.string.code_resent, Toast.LENGTH_SHORT).show(); startCooldown(resend)
                }
            }
            viewModel.state.observe(this) { state ->
                error.text = state.otpError.orEmpty()
                error.visibility = if (state.otpError == null) View.GONE else View.VISIBLE
                if (state.signedInName != null) dismiss()
            }
        }
        return dialog
    }

    private fun startCooldown(view: TextView) {
        view.isEnabled = false; view.alpha = 0.55f; timer?.cancel()
        timer = object : CountDownTimer(30_000L, 1_000L) {
            override fun onTick(ms: Long) { view.text = getString(R.string.resend_in, ms / 1_000 + 1) }
            override fun onFinish() { view.setText(R.string.resend_code); view.isEnabled = true; view.alpha = 1f }
        }.start()
    }

    override fun onDestroyView() { timer?.cancel(); super.onDestroyView() }

    companion object {
        private const val ARG_DESTINATION = "destination"
        fun newInstance(destination: String) = OtpDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_DESTINATION, destination) }
        }
    }
}
