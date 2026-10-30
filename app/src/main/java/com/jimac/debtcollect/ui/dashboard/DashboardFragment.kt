package com.jimac.debtcollect.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.jimac.debtcollect.R

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.load(arguments?.getString("displayName").orEmpty())
        viewModel.displayName.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.signedInTitle).text = getString(R.string.signed_in_title, it)
        }
        view.findViewById<View>(R.id.signOutButton).setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_login)
        }
    }
}
