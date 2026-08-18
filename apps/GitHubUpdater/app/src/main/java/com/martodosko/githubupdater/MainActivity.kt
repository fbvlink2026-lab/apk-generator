package com.martodosko.githubupdater

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val SPLASH_DELAY = 2800L
    private val CURRENT_VERSION = "1.0.0"
    private val GITHUB_API_URL = "https://api.github.com/repos/fbvlink2026-lab/apk-generator/releases/latest"
    private val GITHUB_REPO_URL = "https://github.com/fbvlink2026-lab/apk-generator"
    private val GITHUB_ACTIONS_URL = "https://github.com/fbvlink2026-lab/apk-generator/actions"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var statusMode: TextView
    private lateinit var updateBtn: Button
    private lateinit var latestVersionPanel: TextView
    private lateinit var upToDateText: TextView
    private var autoRunMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val loadingText = findViewById<TextView>(R.id.loadingText)
        val subTitle = findViewById<TextView>(R.id.subTitle)
        val versionText = findViewById<TextView>(R.id.versionText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val copyrightText = findViewById<TextView>(R.id.copyrightText)

        loadingText.alpha = 0f
        loadingText.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 1500; interpolator = DecelerateInterpolator(); fillAfter = true })
        loadingText.alpha = 1f
        subTitle.alpha = 0f
        subTitle.postDelayed({ subTitle.alpha = 1f }, 400)
        versionText.alpha = 0f
        versionText.postDelayed({ versionText.alpha = 1f }, 700)
        progressBar.alpha = 0f
        progressBar.postDelayed({ progressBar.alpha = 1f }, 1000)
        copyrightText.alpha = 0f
        copyrightText.postDelayed({ copyrightText.alpha = 1f }, 1400)

        Handler(Looper.getMainLooper()).postDelayed({ setContentView(R.layout.activity_main); initMainMenu() }, SPLASH_DELAY)
    }

    private fun initMainMenu() {
        drawerLayout = findViewById(R.id.drawerLayout)
        statusMode = findViewById(R.id.statusMode)
        updateBtn = findViewById(R.id.updateBtn)
        latestVersionPanel = findViewById(R.id.latestVersionPanel)
        upToDateText = findViewById(R.id.upToDateText)

        val prefs = getSharedPreferences("MartoDoskoPrefs", Context.MODE_PRIVATE)
        autoRunMode = prefs.getBoolean("autoRunMode", false)
        statusMode.text = if (autoRunMode) "⚡ AUTO-RUN: ON" else "🛑 MANO-MANO MODE"

        findViewById<LinearLayout>(R.id.menuItem1).setOnClickListener { Toast.makeText(this, "📤 IPADALA SA GITHUB", Toast.LENGTH_SHORT).show() }
        findViewById<LinearLayout>(R.id.menuItem2).setOnClickListener { Toast.makeText(this, "🚀 IPADALA + PATAKBUHIN AGAD", Toast.LENGTH_SHORT).show() }
        findViewById<LinearLayout>(R.id.menuItem3).setOnClickListener { Toast.makeText(this, "🖼️ PILIN ANG ICON", Toast.LENGTH_SHORT).show() }
        findViewById<LinearLayout>(R.id.menuItem4).setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL))) }
        findViewById<LinearLayout>(R.id.menuItem5).setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ACTIONS_URL))) }
        findViewById<LinearLayout>(R.id.menuItem6).setOnClickListener { autoRunMode = true; statusMode.text = "⚡ AUTO-RUN: ON"; prefs.edit().putBoolean("autoRunMode", true).apply(); Toast.makeText(this, "AUTO-RUN: ON", Toast.LENGTH_SHORT).show() }
        findViewById<LinearLayout>(R.id.menuItem7).setOnClickListener { autoRunMode = false; statusMode.text = "🛑 MANO-MANO MODE"; prefs.edit().putBoolean("autoRunMode", false).apply(); Toast.makeText(this, "AUTO-RUN: OFF", Toast.LENGTH_SHORT).show() }
        findViewById<LinearLayout>(R.id.menuItem8).setOnClickListener {
            AlertDialog.Builder(this).setTitle("📋 STATUS").setMessage("Version: $CURRENT_VERSION\nMode: ${if (autoRunMode) "AUTO-RUN" else "MANO-MANO"}\nPackage: com.martodosko.githubupdater").setPositiveButton("OK", null).show()
        }
        findViewById<LinearLayout>(R.id.menuItem9).setOnClickListener { finish() }
        updateBtn.setOnClickListener {
            val url = updateBtn.tag as? String ?: return@setOnClickListener
            AlertDialog.Builder(this).setTitle("🎉 New Release").setMessage("May bagong bersyon. Gusto mo bang mag-update?")
                .setPositiveButton("Oo — I-Update Ngayon") { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                .setNegativeButton("Mamaya Na", null).show()
        }
        checkForUpdates()
    }

    private fun checkForUpdates() {
        latestVersionPanel.text = "Checking..."
        updateBtn.visibility = View.GONE
        upToDateText.visibility = View.GONE
        OkHttpClient().newCall(Request.Builder().url(GITHUB_API_URL).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { runOnUiThread { latestVersionPanel.text = "Offline" } }
            override fun onResponse(call: Call, response: Response) {
                response.body()?.string()?.let { body ->
                    val json = JSONObject(body)
                    val latest = json.optString("tag_name", CURRENT_VERSION).removePrefix("v")
                    val url = json.optString("html_url", GITHUB_REPO_URL)
                    runOnUiThread {
                        latestVersionPanel.text = latest
                        if (isNewer(CURRENT_VERSION, latest)) { updateBtn.visibility = View.VISIBLE; updateBtn.tag = url }
                        else upToDateText.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun isNewer(current: String, latest: String): Boolean {
        val c = current.split(".").map { it.toIntOrNull() ?: 0 }
        val l = latest.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(c.size, l.size)) {
            val cv = c.getOrElse(i) { 0 }; val lv = l.getOrElse(i) { 0 }
            if (lv > cv) return true; if (lv < cv) return false
        }
        return false
    }
}
