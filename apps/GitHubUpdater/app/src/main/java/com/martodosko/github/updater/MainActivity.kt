package com.martodosko.github.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
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
    private val VERSION = "v5.77"
    private val REPO_OWNER = "fbvlink2026-lab"
    private val REPO_NAME = "apk-generator"

    private var currentScreen = "MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        findViewById<TextView>(R.id.tvCurrentVersion).text = "📌 Bersyon: $VERSION"
        findViewById<TextView>(R.id.tvStatus).text = "Katayuan: Handa na"

        buildMainMenu()

        findViewById<Button>(R.id.btnCheckVersion).setOnClickListener {
            checkVersion()
        }
        findViewById<Button>(R.id.btnCloseDrawer).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
        }
    }

    // ==========================================
    //   📤 M A I N   M E N U  — KATULAD NG MARTOPUSH
    // ==========================================
    private fun buildMainMenu() {
        currentScreen = "MAIN"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "========================================")
        addMenuHeader(container, "       📤  M A R T O P U S H  $VERSION")
        addMenuHeader(container, "    Developed by MartoDosko © 2026")
        addMenuHeader(container, "========================================")
        addSpace(container, 16)

        addMenuItem(container, "1", "🖼️ ICON — Pumili, I-resize, Ipadala") {
            buildIconMenu()
        }

        addMenuItem(container, "2", "📄 Ipadala ang Cat Code / Maraming File") {
            showCatCodeMenu()
        }

        addMenuItem(container, "3", "📤 Direktang Pagpadala sa GitHub") {
            showDirectPushMenu()
        }

        addMenuItem(container, "4", "📂 Pumili / I-set ang Destination Path") {
            showPathMenu()
        }

        addMenuItem(container, "5", "🔄 Tumatsek ng Update at Bersyon") {
            checkVersion()
        }

        addMenuDivider(container)

        addMenuItem(container, "0", "↩️ Lumabas / Isara") {
            finish()
        }
    }

    // ==========================================
    //   🖼️ O P T I O N   1  —  I C O N   M E N U
    // ==========================================
    private fun buildIconMenu() {
        currentScreen = "ICON"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "🖼️  ICON — PUMILI → I-RESIZE → IPADALA")
        addSpace(container, 12)

        addMenuItem(container, "1", "📂 Pumili ng Larawan mula sa Folder") {
            Toast.makeText(this, "📂 Binubuksan ang folder...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "2", "📏 I-resize sa tamang sukat (mdpi-hdpi-xhdpi-xxhdpi)") {
            Toast.makeText(this, "📏 Inaayos ang sukat...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "3", "📤 Ipadala sa tamang GitHub folder") {
            Toast.makeText(this, "📤 Pinapadala ang icon...", Toast.LENGTH_SHORT).show()
        }

        addMenuDivider(container)

        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") {
            buildMainMenu()
        }
    }

    // ==========================================
    //   📄 O P T I O N   2  —  C A T   C O D E
    // ==========================================
    private fun showCatCodeMenu() {
        currentScreen = "CATCODE"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "📄  CAT CODE — SABAY-SABAY NA PAGPADALA")
        addSpace(container, 12)

        addMenuItem(container, "1", "📋 I-paste ang Cat Code dito") {
            Toast.makeText(this, "📋 Hinihintay ang Cat Code...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "2", "📂 Piliin ang Destinasyon sa GitHub") {
            Toast.makeText(this, "📂 Pinipili ang daan...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "3", "📤 Ipadala Lahat") {
            Toast.makeText(this, "📤 Pinapadala ang lahat...", Toast.LENGTH_SHORT).show()
        }

        addMenuDivider(container)

        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") {
            buildMainMenu()
        }
    }

    // ==========================================
    //   📤 O P T I O N   3  —  D I R E K T A N G   P A D A L A
    // ==========================================
    private fun showDirectPushMenu() {
        currentScreen = "PUSH"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "📤  DIREKTANG PAGPADALA SA GITHUB")
        addSpace(container, 12)

        addMenuItem(container, "1", "📤 I-commit at I-push lahat ng nabago") {
            Toast.makeText(this, "📤 Ina-commit at I-push...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "2", "📋 Tignan muna ang mga babaguhin") {
            Toast.makeText(this, "📋 Tinitignan ang status...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "3", "🔄 Auto-Pull bago I-push (iwas-reject)") {
            Toast.makeText(this, "🔄 Kinuha ang bagong pagbabago...", Toast.LENGTH_SHORT).show()
        }

        addMenuDivider(container)

        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") {
            buildMainMenu()
        }
    }

    // ==========================================
    //   📂 O P T I O N   4  —  P A T H
    // ==========================================
    private fun showPathMenu() {
        currentScreen = "PATH"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "📂  DESTINATION PATH — PUMILI O I-SET")
        addSpace(container, 12)

        addMenuItem(container, "1", "📂 Listahan ng mga Path sa Repository") {
            Toast.makeText(this, "📂 Inililista ang mga daan...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "2", "💾 I-save bilang Default Path") {
            Toast.makeText(this, "💾 Na-save bilang default...", Toast.LENGTH_SHORT).show()
        }

        addMenuItem(container, "3", "✏️ I-type ang sariling Path") {
            Toast.makeText(this, "✏️ Pagta-type ng daan...", Toast.LENGTH_SHORT).show()
        }

        addMenuDivider(container)

        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") {
            buildMainMenu()
        }
    }

    // ==========================================
    //   🔄 O P T I O N   5  —  B E R S Y O N
    // ==========================================
    private fun checkVersion() {
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = "🔍 Tinitignan..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
                val json = JSONObject(URL(url).readText())
                val latest = json.optString("tag_name", VERSION).removePrefix("v")

                launch(Dispatchers.Main) {
                    tvStatus.text = if (latest == VERSION.removePrefix("v")) {
                        "✅ Nasa Pinakabago na: v$latest"
                    } else {
                        "⚠️ May Bagong Bersyon: v$latest"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvStatus.text = "⚠️ Hindi matignan — walang koneksyon"
                }
            }
        }
    }

    // ==========================================
    //   🛠️  T U L O N G   F U N K S Y O N
    // ==========================================
    private fun addMenuHeader(container: LinearLayout, text: String) {
        val tv = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFF1565C0.toInt())
            setPadding(0, 4, 0, 4)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }
        container.addView(tv)
    }

    private fun addMenuItem(container: LinearLayout, num: String, label: String, action: () -> Unit) {
        val btn = Button(this).apply {
            text = "[$num]   $label"
            textSize = 15f
            setPadding(20, 16, 20, 16)
            setBackgroundResource(android.R.drawable.btn_default)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            setOnClickListener { action() }
        }
        container.addView(btn)
    }

    private fun addMenuDivider(container: LinearLayout) {
        val v = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(0xFFE0E0E0.toInt())
            setPadding(0, 8, 0, 8)
        }
        container.addView(v)
    }

    private fun addSpace(container: LinearLayout, dp: Int) {
        val v = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp
            )
        }
        container.addView(v)
    }

    override fun onBackPressed() {
        if (currentScreen != "MAIN") {
            buildMainMenu()
        } else {
            super.onBackPressed()
        }
    }
}
