package com.martodosko.github.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val currentVersion = "1.0.0"

    // ✅ TOTOONG LUGAR NG IYONG APK SA GITHUB
    private val REPO_OWNER = "fbvlink2026-lab"
    private val REPO_NAME = "apk-generator"
    private val APK_URL = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/main/docs/GitHubUpdater-debug.apk"
    private val VERSION_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/docs/version.json"

    private var latestVersion: String? = null
    private var isChecking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val tvCurrent = findViewById<TextView>(R.id.tvCurrentVersion)
        val tvNew = findViewById<TextView>(R.id.tvNewVersion)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val menuContainer = findViewById<LinearLayout>(R.id.menu_container)

        tvCurrent.text = "📌 Kasalukuyang Bersyon: $currentVersion"

        // ==============================================
        // ✅ MENU SA HARAP — AGAD MAKIKITA SA PAGBUKAS!
        // ==============================================
        showFrontMenu(menuContainer)

        // ==============================================
        // ✅ PAGBUKAS PA LANG — KUSANG TUMATSEK KUNG MAY BAGONG BERSYON!
        // ==============================================
        checkForUpdates(tvNew, btnDownload)

        // ==============================================
        // ✅ PINDUTIN — TUMATSEK ULIT
        // ==============================================
        btnCheck.setOnClickListener {
            if (!isChecking) {
                checkForUpdates(tvNew, btnDownload)
            }
        }

        // ==============================================
        // ✅ PINDUTIN — TOTOONG MAGDADOWNLOAD NG TAMAANG APK!
        // ==============================================
        btnDownload.setOnClickListener {
            if (!latestVersion.isNullOrEmpty() && latestVersion != currentVersion) {
                downloadAndInstallApk()
                btnDownload.text = "⬇️ Dinadownload..."
                btnDownload.isEnabled = false
            } else {
                Toast.makeText(this, "✅ Wala pang bagong bersyon!", Toast.LENGTH_SHORT).show()
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(Gravity.LEFT)
            true
        }
    }

    // ==============================================
    // ✅ MENU SA HARAP — AGAD MAKIKITA! HINDI NAKATAGO!
    // ==============================================
    private fun showFrontMenu(container: LinearLayout) {
        container.removeAllViews()

        val options = listOf(
            "🔍 Tumatsek ng Bagong Bersyon" to {
                checkForUpdates(findViewById(R.id.tvNewVersion), findViewById(R.id.btnDownload))
            },
            "⬇️ I-download ang Update" to {
                downloadAndInstallApk()
            },
            "ℹ️ Tungkol sa App" to {
                showAboutDialog()
            },
            "📂 Buksan ang GitHub Page" to {
                openGitHubPage()
            }
        )

        for ((label, action) in options) {
            val btn = Button(this).apply {
                text = label
                setPadding(48, 32, 48, 32)
                textSize = 15f
                setOnClickListener { action() }
            }
            container.addView(btn)
        }
    }

    // ==============================================
    // ✅ KUSANG TUMATSEK SA GITHUB — TOTOONG VERSION CHECK!
    // ==============================================
    private fun checkForUpdates(statusView: TextView, downloadBtn: Button) {
        isChecking = true
        statusView.text = "🔍 Tinitignan kung may bago..."
        downloadBtn.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ✅ Paraan 1 — Tignan ang latest release
                val releaseUrl = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
                val response = URL(releaseUrl).readText()
                val json = JSONObject(response)
                latestVersion = json.optString("tag_name", "v$currentVersion").removePrefix("v")

                launch(Dispatchers.Main) {
                    if (latestVersion != currentVersion) {
                        statusView.text = "✅ MAY BAGONG BERSYON: $latestVersion"
                        downloadBtn.text = "⬇️ I-update ngayon"
                        downloadBtn.isEnabled = true
                    } else {
                        statusView.text = "✅ Nasa Pinakabagong Bersyon: $currentVersion"
                        downloadBtn.text = "✅ Napa-update na"
                        downloadBtn.isEnabled = false
                    }
                }
            } catch (e: Exception) {
                // ✅ Paraan 2 — Kung walang release — gamitin ang hardcoded pero gumagana pa rin
                launch(Dispatchers.Main) {
                    statusView.text = "⚠️ Hindi matignan ang bersyon — offline mode"
                    latestVersion = currentVersion
                    downloadBtn.isEnabled = false
                }
            }
            isChecking = false
        }
    }

    // ==============================================
    // ✅ TOTOONG PAG-DOWNLOAD — HINDI SIMULASYON LANG!
    // ==============================================
    private fun downloadAndInstallApk() {
        try {
            val request = DownloadManager.Request(Uri.parse(APK_URL)).apply {
                setTitle("GitHubUpdater Update")
                setDescription("Dinadownload ang bersyon $latestVersion...")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GitHubUpdater-update.apk")
                setMimeType("application/vnd.android.package-archive")
            }

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "✅ Nagsimula ang pag-download — tignan ang abiso!", Toast.LENGTH_LONG).show()

            findViewById<Button>(R.id.btnDownload).apply {
                text = "✅ Nagsimula ang Pag-download"
                isEnabled = false
            }

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Nabigo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("ℹ️ Tungkol sa GitHubUpdater")
            .setMessage("""
                Bersyon: $currentVersion
                
                Awtomatikong tumatsek at nagda-download 
                ng mga bagong bersyon mula sa GitHub.
                
                Developed by MartoDosko © 2026
            """.trimIndent())
            .setPositiveButton("Sige", null)
            .show()
    }

    private fun openGitHubPage() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$REPO_OWNER/$REPO_NAME"))
        startActivity(intent)
    }
}
