package com.jimac.debtcollect.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.jimac.debtcollect.R

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private val handler = Handler(Looper.getMainLooper())
    private val openLogin = Runnable {
        if (isAdded) NavHostFragment.findNavController(this)
            .navigate(R.id.action_splash_to_login)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logo = view.findViewById<View>(R.id.logoMark)
        val halo = view.findViewById<View>(R.id.logoHalo)
        val brand = view.findViewById<View>(R.id.brandName)
        val tagline = view.findViewById<View>(R.id.tagline)
        logo.scaleX = 0.25f
        logo.scaleY = 0.25f
        logo.alpha = 0f
        halo.scaleX = 0.78f
        halo.scaleY = 0.78f
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(180)
            .setDuration(620).setInterpolator(DecelerateInterpolator()).start()
        halo.animate().scaleX(1f).scaleY(1f).setStartDelay(180)
            .setDuration(720).setInterpolator(DecelerateInterpolator()).start()
        brand.animate().alpha(1f).translationY(-5f).setStartDelay(520).setDuration(420).start()
        tagline.animate().alpha(1f).setStartDelay(700).setDuration(420).start()
        handler.postDelayed(openLogin, 2_200L)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(openLogin)
        super.onDestroyView()
    }
}
