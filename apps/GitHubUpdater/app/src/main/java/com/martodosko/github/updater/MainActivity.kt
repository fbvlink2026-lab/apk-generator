package com.martodosko.github.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val currentVersion = "1.0.0"

    private val REPO_OWNER = "fbvlink2026-lab"
    private val REPO_NAME = "apk-generator"
    private val APK_URL = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/main/docs/GitHubUpdater-debug.apk"

    private var latestVersion: String? = null
    private var isChecking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        // ✅ IMPORMASYON SA GILID — HINDI NA SA GITNA!
        val tvCurrent = findViewById<TextView>(R.id.tvCurrentVersion)
        val tvNew = findViewById<TextView>(R.id.tvNewVersion)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val btnAbout = findViewById<Button>(R.id.btnAbout)
        val btnGitHub = findViewById<Button>(R.id.btnGitHub)

        tvCurrent.text = "📌 Kasalukuyan: v$currentVersion"

        // ✅ MENU SA GITNA — ANG PANGUNAHING LAMAN!
        val mainMenu = findViewById<LinearLayout>(R.id.main_menu_container)
        buildMainMenu(mainMenu)

        // ✅ KUSANG TUMATSEK SA PAGBUKAS — LUMALABAS SA GILID
        checkForUpdates(tvCurrent, tvNew, btnDownload)

        // ✅ MGA PINDUTAN SA GILID
        btnCheck.setOnClickListener { checkForUpdates(tvCurrent, tvNew, btnDownload) }
        btnDownload.setOnClickListener { downloadApk() }
        btnAbout.setOnClickListener { showAboutDialog() }
        btnGitHub.setOnClickListener { openGitHubPage() }
    }

    // ✅ MENU SA GITNA — KOPYA MULA SA TERMUX MENU!
    private fun buildMainMenu(container: LinearLayout) {
        container.removeAllViews()

        val menuItems = listOf(
            "🔄 Tumatsek ng Bagong Bersyon" to {
                checkForUpdates(
                    findViewById(R.id.tvCurrentVersion),
                    findViewById(R.id.tvNewVersion),
                    findViewById(R.id.btnDownload)
                )
                Toast.makeText(this, "🔍 Tinitignan...", Toast.LENGTH_SHORT).show()
            },
            "⬇️ I-download ang Pinakabago" to {
                downloadApk()
            },
            "📂 Buksan ang Pahina sa GitHub" to {
                openGitHubPage()
            },
            "ℹ️ Tungkol sa Programang Ito" to {
                showAboutDialog()
            }
        )

        for ((label, action) in menuItems) {
            val btn = Button(this).apply {
                text = label
                textSize = 17f
                setPadding(32, 24, 32, 24)
                setBackgroundResource(android.R.drawable.btn_default)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
                setOnClickListener { action() }
            }
            container.addView(btn)
        }
    }

    // ✅ TUMATSEK — LUMALABAS SA GILID
    private fun checkForUpdates(
        tvCurrent: TextView,
        tvNew: TextView,
        btnDownload: Button
    ) {
        isChecking = true
        tvNew.text = "🔍 Tinitignan..."
        btnDownload.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
                val json = JSONObject(URL(url).readText())
                latestVersion = json.optString("tag_name", "v$currentVersion").removePrefix("v")

                launch(Dispatchers.Main) {
                    if (latestVersion != currentVersion) {
                        tvNew.text = "✅ May Bago: v$latestVersion"
                        tvNew.setTextColor(0xFF4CAF50.toInt())
                        btnDownload.isEnabled = true
                        btnDownload.text = "⬇️ I-update Ngayon"
                    } else {
                        tvNew.text = "✅ Nasa Pinakabago"
                        tvNew.setTextColor(0xFF2196F3.toInt())
                        btnDownload.isEnabled = false
                        btnDownload.text = "✅ Napa-update Na"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvNew.text = "⚠️ Hindi Matignan"
                    tvNew.setTextColor(0xFFFF9800.toInt())
                    latestVersion = currentVersion
                }
            }
            isChecking = false
        }
    }

    // ✅ PAG-DOWNLOAD
    private fun downloadApk() {
        if (latestVersion == currentVersion || latestVersion == null) {
            Toast.makeText(this, "✅ Wala pang bagong bersyon!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val req = DownloadManager.Request(Uri.parse(APK_URL)).apply {
                setTitle("Updater — v$latestVersion")
                setDescription "Dinadownload ang bagong bersyon..."
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GitHubUpdater-update.apk")
                setMimeType("application/vnd.android.package-archive")
            }
            getSystemService(DownloadManager::class.java).enqueue(req)
            Toast.makeText(this, "✅ Nagsimula ang Pag-download!", Toast.LENGTH_LONG).show()
            drawerLayout.closeDrawer(Gravity.START)
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Nabigo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("ℹ️ Tungkol sa GitHubUpdater")
            .setMessage("""
                Bersyon: v$currentVersion
                
                Awtomatikong tumatsek at nagda-download
                ng mga bagong bersyon mula sa GitHub.
                
                — Galing sa Termux, Ngayon ay APK na —
                
                Developed by MartoDosko © 2026
            """.trimIndent())
            .setPositiveButton("Sige", null)
            .show()
    }

    private fun openGitHubPage() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$REPO_OWNER/$REPO_NAME")))
    }
}
