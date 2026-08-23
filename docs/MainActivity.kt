package com.martodosko.github.updater

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val VERSION = "v6.0.0 — BETA"
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvStatus.text = "✅ MartoPush — Bersyon: $VERSION"

        checkForUpdates()
    }

    private fun checkForUpdates() {
        // Kasalukuyan: Simpleng mensahe lang
        tvStatus.text = "🔍 Tinitignan ang bagong bersyon..."
    }
}