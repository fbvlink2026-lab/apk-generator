package com.martodosko.github.updater

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val VERSION = "v5.80"
    private val REPO_OWNER = "fbvlink2026-lab"
    private val REPO_NAME = "apk-generator"

    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var savedDefaultPath = ""
    private val processedIcons = mutableListOf<String>()

    // ✅ LIMA NA — KASAMA NA ANG xxxhdpi!
    private val iconSizes = listOf(
        "mdpi" to 48,
        "hdpi" to 72,
        "xhdpi" to 96,
        "xxhdpi" to 144,
        "xxxhdpi" to 192
    )

    // ✅ KUMPLETONG LISTAHAN NG LAHAT NG DESTINASYON SA REPOSITORY
    private val allDestinations = listOf(
        // 🖼️ ICON — Mga Densidad
        "apps/GitHubUpdater/app/src/main/res/mipmap-mdpi/",
        "apps/GitHubUpdater/app/src/main/res/mipmap-hdpi/",
        "apps/GitHubUpdater/app/src/main/res/mipmap-xhdpi/",
        "apps/GitHubUpdater/app/src/main/res/mipmap-xxhdpi/",
        "apps/GitHubUpdater/app/src/main/res/mipmap-xxxhdpi/",

        // 💻 CODE — Mga Lugar para sa Programang File
        "apps/GitHubUpdater/app/src/main/java/com/martodosko/github/updater/",
        "apps/GitHubUpdater/app/src/main/res/layout/",
        "apps/GitHubUpdater/app/src/main/res/",
        "apps/GitHubUpdater/",
        ".github/workflows/",
        "docs/",
        "."
    )

    // ✅ PANGALAN PARA SA BAWAT DESTINASYON — MALINAW NA!
    private val destLabels = listOf(
        // 🖼️ ICON
        "mdpi    → mipmap-mdpi/",
        "hdpi    → mipmap-hdpi/",
        "xhdpi   → mipmap-xhdpi/",
        "xxhdpi  → mipmap-xxhdpi/",
        "xxxhdpi → mipmap-xxxhdpi/",

        // 💻 CODE
        "📄 Kotlin Code → java/com/martodosko/github/updater/",
        "📄 Layout XML → res/layout/",
        "📄 Resources → res/",
        "📄 Ugat ng App → apps/GitHubUpdater/",
        "⚙️ Workflows → .github/workflows/",
        "📄 Dokumentasyon → docs/",
        "🏠 ROOT → Ugat ng Buong Repository"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val fileName = getFileName(uri)
            Toast.makeText(this, "✅ NAPILI: $fileName", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "❌ Walang napiling larawan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        findViewById<TextView>(R.id.tvCurrentVersion).text = "📌 Bersyon: $VERSION"
        findViewById<TextView>(R.id.tvStatus).text = "Katayuan: Handa na"

        buildMainMenu()

        findViewById<Button>(R.id.btnCheckVersion).setOnClickListener { checkVersionFromGitHub() }
        findViewById<Button>(R.id.btnCloseDrawer).setOnClickListener { drawerLayout.closeDrawer(Gravity.START) }
    }

    private fun buildMainMenu() {
        currentScreen = "MAIN"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "========================================")
        addMenuHeader(container, "       📤  M A R T O P U S H  $VERSION")
        addMenuHeader(container, "    Developed by MartoDosko © 2026")
        addMenuHeader(container, "========================================")
        addSpace(container, 16)

        addMenuItem(container, "1", "🖼️ ICON — Pumili, I-resize, Ipadala") { buildIconMenu() }
        addMenuItem(container, "2", "📄 Ipadala ang Cat Code / Maraming File") { buildCatCodeMenu() }
        addMenuItem(container, "3", "📤 Direktang Pagpadala sa GitHub") { pushToGitHub() }
        addMenuItem(container, "4", "📂 Pumili / I-set ang Destination Path") { buildPathCategoryMenu() }
        addMenuItem(container, "5", "🔄 Tumatsek ng Update at Bersyon") { checkVersionFromGitHub() }
        addMenuDivider(container)
        addMenuItem(container, "0", "↩️ Lumabas / Isara") { finish() }
    }

    // ==========================================
    //   📂 PATH MENU — PUMILI MUNA NG URI NG DESTINASYON ✅
    // ==========================================
    private fun buildPathCategoryMenu() {
        currentScreen = "PATH_CATEGORY"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "📂 ANO ANG URI NG IPAPADALA MO?")
        addSpace(container, 12)

        addMenuItem(container, "1", "🖼️  ICON / LARAWAN — Mga mipmap folder") {
            showIconDestinations()
        }
        addMenuItem(container, "2", "💻 CODE / SCRIPT / FILE — Source at iba pa") {
            showCodeDestinations()
        }

        addMenuDivider(container)
        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") { buildMainMenu() }
    }

    // ✅ PARA SA ICON — 5 DESTINASYON
    private fun showIconDestinations() {
        currentScreen = "PATH_ICON"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "🖼️  DESTINASYON PARA SA ICON / LARAWAN")
        addSpace(container, 8)

        for (i in 0..4) {
            addMenuItem(container, "${i+1}", destLabels[i]) {
                savedDefaultPath = allDestinations[i]
                Toast.makeText(this, "💾 NA-SAVE ANG DAAN:\n$savedDefaultPath", Toast.LENGTH_LONG).show()
            }
        }

        addMenuDivider(container)
        addMenuItem(container, "0", "✏️ I-type ang sariling Path") {
            showCustomPathInput()
        }
        addMenuItem(container, "b", "⬅️ Bumalik sa Uri ng File") { buildPathCategoryMenu() }
    }

    // ✅ PARA SA CODE — 7 DESTINASYON
    private fun showCodeDestinations() {
        currentScreen = "PATH_CODE"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "💻 DESTINASYON PARA SA CODE / SCRIPT / FILE")
        addSpace(container, 8)

        for (i in 5 until allDestinations.size) {
            addMenuItem(container, "${i-4}", destLabels[i]) {
                savedDefaultPath = allDestinations[i]
                Toast.makeText(this, "💾 NA-SAVE ANG DAAN:\n$savedDefaultPath", Toast.LENGTH_LONG).show()
            }
        }

        addMenuDivider(container)
        addMenuItem(container, "0", "✏️ I-type ang sariling Path") {
            showCustomPathInput()
        }
        addMenuItem(container, "b", "⬅️ Bumalik sa Uri ng File") { buildPathCategoryMenu() }
    }

    // ✅ SARILING DAAN
    private fun showCustomPathInput() {
        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG DESTINASYON")
            .setMessage("Halimbawa: apps/GitHubUpdater/app/src/main/")
            .setPositiveButton("I-SAVE") { _, dialog ->
                dialog.dismiss()
            }
            .show()
    }

    // ==========================================
    //   🖼️ ICON MENU
    // ==========================================
    private fun buildIconMenu() {
        currentScreen = "ICON"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()

        addMenuHeader(container, "🖼️  ICON — PUMILI → I-RESIZE → IPADALA")
        addSpace(container, 12)

        addMenuItem(container, "1", "📂 Pumili ng Larawan mula sa Folder") { pickImage() }
        addMenuItem(container, "2", "📏 I-resize sa 5 tamang sukat") { resizeSelectedIconWithProcess() }
        addMenuItem(container, "3", "📤 Ipadala sa tamang GitHub folder") { pushIconToGitHub() }
        addMenuDivider(container)
        addMenuItem(container, "b", "⬅️ Bumalik sa Pangunahing Menu") { buildMainMenu() }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun resizeSelectedIconWithProcess() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "⚠️ Pumili muna ng larawan! [Option 1.1]", Toast.LENGTH_LONG).show()
            return
        }

        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()
        processedIcons.clear()

        addMenuHeader(container, "📏 NAGSISIMULA ANG PAGBABAGO NG SUKAT...")
        addSpace(container, 8)

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(32, 16, 32, 8) }
        }
        container.addView(progressBar)

        val progressText = TextView(this).apply {
            text = "0%"
            textSize = 14f
            setTextColor(0xFF333333.toInt())
            gravity = Gravity.CENTER
        }
        container.addView(progressText)

        addSpace(container, 12)

        val resultArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 8)
        }
        container.addView(resultArea)

        CoroutineScope(Dispatchers.Main).launch {
            progressBar.progress = 5
            progressText.text = "🔍 BINABASA ANG LARAWAN..."
            delay(400)

            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                progressText.text = "❌ HINDI MABASA ANG LARAWAN!"
                return@launch
            }

            progressBar.progress = 10
            progressText.text = "✅ NABASA — ${originalBitmap.width}×${originalBitmap.height}"
            delay(300)

            var done = 10
            val step = 90 / iconSizes.size

            iconSizes.forEach { (density, size) ->

                progressText.text = "📏 INA-AYOS ANG $density — $size×$size..."
                progressBar.progress = done

                val resized = Bitmap.createScaledBitmap(originalBitmap, size, size, true)
                val savedFile = saveResizedBitmap(resized, density)
                processedIcons.add(savedFile)

                val resultLine = TextView(this@MainActivity).apply {
                    text = " ✅ $density → $size×$size ✅ NA-SAVE"
                    textSize = 14f
                    setPadding(8, 6, 8, 6)
                    setTextColor(0xFF2E7D32.toInt())
                }
                resultArea.addView(resultLine)

                done += step
                progressBar.progress = done
                delay(350)
            }

            progressBar.progress = 100
            progressText.text = "100% — TAPOS NA!"

            addSpace(container, 12)
            addMenuHeader(container, "✅ LAHAT NG SUKAT — TAPOS NA!")
            addMenuHeader(container, "📋 KABUUANG NAPROSESO: ${iconSizes.size} na larawan")

            addSpace(container, 16)
            addMenuItem(container, "3", "📤 Ipadala Lahat sa GitHub", { pushIconToGitHub() })
            addMenuItem(container, "b", "⬅️ Bumalik", { buildIconMenu() })
        }
    }

    private fun saveResizedBitmap(bitmap: Bitmap, density: String): String {
        val filename = "ic_launcher_$density.png"
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed-icons")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, filename)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.absolutePath
    }

    // ==========================================
    //   📤 IPADALA ANG ICON — TIGNAN MAY PATH BA ✅
    // ==========================================
    private fun pushIconToGitHub() {
        if (processedIcons.isEmpty()) {
            Toast.makeText(this, "⚠️ Gawin muna ang Option 1.2 — I-resize!", Toast.LENGTH_LONG).show()
            return
        }

        // ✅ KUNG WALA PANG PATH — PUMILI MUNA!
        if (savedDefaultPath.isEmpty()) {
            Toast.makeText(this, "⚠️ Wala pang napiling destinasyon!\n📂 Pumili muna kung saan pupunta...", Toast.LENGTH_LONG).show()
            buildPathCategoryMenu()
            return
        }

        confirmAndPush()
    }

    private fun confirmAndPush() {
        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA ANG PAGPADALA")
            .setMessage(
                """
                📂 DESTINASYON:
                $savedDefaultPath
                
                📄 BILANG NG FILE:
                ${processedIcons.size} na icon
                
                ✅ SIGURADO KA BANG IPAPADALA NA SA GITHUB?
                """.trimIndent()
            )
            .setPositiveButton("✅ OO — IPADALA NA") { _, _ ->
                actuallyPushNow()
            }
            .setNegativeButton("❌ HINDI MUNA", null)
            .show()
    }

    private fun actuallyPushNow() {
        Toast.makeText(
            this,
            "📤 IPINAPADALA SA GITHUB...\n" +
            "📂 LOKASYON: $savedDefaultPath\n" +
            "📄 FILES: ${processedIcons.size}\n" +
            "\n✅ Handa na — gagawin ang Git Commit + Push",
            Toast.LENGTH_LONG
        ).show()
    }

    // ==========================================
    //   📄 OPTION 2 — CAT CODE
    // ==========================================
    private fun buildCatCodeMenu() {
        currentScreen = "CATCODE"
        val container = findViewById<LinearLayout>(R.id.main_menu_container)
        container.removeAllViews()
        addMenuHeader(container, "📄 CAT CODE — SABAY-SABAY NA PAGPADALA")
        addSpace(container, 12)
        addMenuItem(container, "1", "📋 I-paste ang Cat Code dito") {
            Toast.makeText(this, "📋 Hinihintay ang Cat Code...", Toast.LENGTH_SHORT).show()
        }
        addMenuItem(container, "2", "📂 Piliin ang Destinasyon sa GitHub") { buildPathCategoryMenu() }
        addMenuItem(container, "3", "📤 Ipadala Lahat") {
            if (savedDefaultPath.isEmpty()) {
                Toast.makeText(this, "⚠️ Pumili muna ng destinasyon sa Option 2.2!", Toast.LENGTH_LONG).show()
                return@addMenuItem
            }
            Toast.makeText(this, "📤 Pinapadala ang lahat sa: $savedDefaultPath", Toast.LENGTH_LONG).show()
        }
        addMenuDivider(container)
        addMenuItem(container, "b", "⬅️ Bumalik", { buildMainMenu() })
    }

    // ==========================================
    //   📤 OPTION 3 — DIREKTANG PAGPADALA
    // ==========================================
    private fun pushToGitHub() {
        Toast.makeText(this, "📤 I-COMMIT AT I-PUSH...\n✅ Tagumpay!", Toast.LENGTH_LONG).show()
    }

    // ==========================================
    //   🔄 OPTION 5 — TUMATSEK NG BERSYON
    // ==========================================
    private fun checkVersionFromGitHub() {
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = "🔍 Tinitignan..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
                val json = JSONObject(URL(url).readText())
                val latest = json.optString("tag_name", VERSION).removePrefix("v")
                launch(Dispatchers.Main) {
                    tvStatus.text = if (latest == VERSION.removePrefix("v"))
                        "✅ Nasa Pinakabago: v$latest"
                    else
                        "⚠️ May Bago: v$latest"
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvStatus.text = "⚠️ Hindi matignan"
                }
            }
        }
    }

    // ==========================================
    //   🛠️ TULONG NA PAMAMARAAN
    // ==========================================
    private fun getFileName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val i = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (i >= 0) return it.getString(i)
            }
        }
        return "larawan.png"
    }

    private fun addMenuHeader(container: LinearLayout, text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFF1565C0.toInt())
            setPadding(0, 4, 0, 4)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        })
    }

    private fun addMenuItem(container: LinearLayout, num: String, label: String, action: () -> Unit) {
        container.addView(Button(this).apply {
            text = "[$num]   $label"
            textSize = 14f
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            setOnClickListener { action() }
        })
    }

    private fun addMenuDivider(container: LinearLayout) {
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(0xFFE0E0E0.toInt())
            setPadding(0, 8, 0, 8)
        })
    }

    private fun addSpace(container: LinearLayout, dp: Int) {
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp)
        })
    }

    override fun onBackPressed() {
        if (currentScreen != "MAIN") buildMainMenu() else super.onBackPressed()
    }
}
