package com.martodosko.github.updater

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.GravityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var versionText: TextView
    private lateinit var updateBtn: Button
    private lateinit var sideVersion: TextView
    private lateinit var sideStatus: TextView
    private lateinit var sideUpdate: Button

    private val APP_VERSION = "1.0.0"
    private val GITHUB_REPO = "fbvlink2026-lab/apk-generator-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_screen)
        window.decorView.alpha = 0f
        window.decorView.animate().alpha(1f).setDuration(800)

        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.main_screen)
            initViews()
            checkUpdates()
        }, 2500)
    }

    private fun initViews() {
        drawer = findViewById(R.id.drawer_layout)
        versionText = findViewById(R.id.tv_version)
        updateBtn = findViewById(R.id.btn_update_now)
        sideVersion = findViewById(R.id.tv_side_version)
        sideStatus = findViewById(R.id.tv_side_status)
        sideUpdate = findViewById(R.id.btn_side_update)

        versionText.text = "Version: $APP_VERSION"
        sideVersion.text = APP_VERSION

        updateBtn.setOnClickListener { openReleases() }
        sideUpdate.setOnClickListener { openReleases() }

        findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    private fun checkUpdates() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val json = conn.inputStream.bufferedReader().readText()
                val latest = Regex("\"tag_name\":\"v?([\\d.]+)\"").find(json)?.groupValues?.get(1) ?: APP_VERSION

                withContext(Dispatchers.Main) {
                    if (latest > APP_VERSION) {
                        versionText.append("  ✅ NEW RELEASE: $latest")
                        updateBtn.text = "🔄 UPDATE NOW TO v$latest"
                        sideStatus.text = "May Bagong Bersyon!"
                        sideStatus.setTextColor(0xFFFFC107.toInt())
                    } else {
                        versionText.append("  ✅ Up to Date")
                        updateBtn.text = "✅ Already Latest"
                        sideStatus.text = "Nasa Pinakabago"
                        sideStatus.setTextColor(0xFF4CAF50.toInt())
                    }
                }
            } catch {
                withContext(Dispatchers.Main) {
                    sideStatus.text = "Hindi Masuri"
                }
            }
        }
    }

    private fun openReleases() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$GITHUB_REPO/releases")))
    }
}
