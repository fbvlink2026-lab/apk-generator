package com.martodosko.github.updater

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val currentVersion = "1.0.0"
    private val newVersion = "1.1.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val tvCurrent = findViewById<TextView>(R.id.tvCurrentVersion)
        val tvNew = findViewById<TextView>(R.id.tvNewVersion)
        val btnDownload = findViewById<Button>(R.id.btnDownload)

        tvCurrent.text = "Kasalukuyang Bersyon: $currentVersion"
        tvNew.text = "Bagong Bersyon: $newVersion"

        navView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawer(Gravity.LEFT)
            true
        }

        btnDownload.setOnClickListener {
            btnDownload.text = "⬇️ Dinadownload..."
            btnDownload.postDelayed({
                btnDownload.text = "✅ Handang I-install"
                tvNew.text = "Bersyon $newVersion — Nai-download na!"
            }, 2000)
        }
    }
}
