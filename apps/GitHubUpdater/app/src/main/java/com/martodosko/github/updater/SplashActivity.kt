package com.martodosko.github.updater

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // ✅ HABANG NAGBUBUKAS — 2 SEGUNDO LANG
    private val SPLASH_DELAY = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ✅ MAGHINTAY → PUMUNTA AGAD SA MAIN
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // ✅ ISARA ANG SPLASH — HINDI BUMABALIK PAG-BACK
        }, SPLASH_DELAY)
    }
}
