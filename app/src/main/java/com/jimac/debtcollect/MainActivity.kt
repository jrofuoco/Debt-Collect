package com.jimac.debtcollect

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
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

        Handler(Looper.getMainLooper()).postDelayed({
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
}
