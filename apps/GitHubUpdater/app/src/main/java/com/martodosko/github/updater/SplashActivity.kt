package com.martodosko.github.updater

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val titleText = findViewById<TextView>(R.id.splashTitle)
        val copyText = findViewById<TextView>(R.id.splashCopyright)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1500
        titleText.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            val fadeCopy = AlphaAnimation(0f, 1f)
            fadeCopy.duration = 1200
            fadeCopy.startOffset = 400
            copyText.startAnimation(fadeCopy)
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3500)
    }
}
