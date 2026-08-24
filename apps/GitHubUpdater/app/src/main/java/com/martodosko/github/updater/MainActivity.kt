package com.martodosko.github.updater

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var mainMenuContainer: LinearLayout
    private lateinit var mainScrollView: ScrollView
    private lateinit var tvStatus: TextView
    private lateinit var tvCurrentVersion: TextView
    private lateinit var btnDownloadUpdate: Button

    private var repoOwner = ""
    private var repoName = ""
    private var savedDefaultPath = ""
    private var latestApkUrl: String? = null
    private var latestApkName: String? = null
    private var GITHUB_TOKEN = ""

    // 🏷️ KASALUKUYANG BERSYON — AWTOMATIKONG PALITAN NG build.yml BAGO MAG-COMPILE!
    private val CURRENT_VERSION = "v6.0.5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainMenuContainer = findViewById(R.id.mainMenuContainer)
        mainScrollView = findViewById(R.id.mainScrollView)
        tvStatus = findViewById(R.id.tvStatus)
        tvCurrentVersion = findViewById(R.id.tvCurrentVersion)
        btnDownloadUpdate = findViewById(R.id.btnDownloadUpdate)

        tvCurrentVersion.text = "📌 Bersyon: $CURRENT_VERSION"
        btnDownloadUpdate.visibility = View.GONE
        btnDownloadUpdate.setOnClickListener { downloadAndInstallUpdate() }

        loadPreferences()
        buildMainMenu()

        // ✅ SA PAGBUKAS — AGAD TIGNAN KUNG MAY BAGONG BERSYON!
        checkUpdateOnLaunch()
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
        GITHUB_TOKEN = prefs.getString("github_token", "") ?: ""
        repoOwner = prefs.getString("github_owner", "") ?: ""
        repoName = prefs.getString("github_repo", "") ?: ""
        savedDefaultPath = prefs.getString("default_destination_path", "") ?: ""
        updateStatusDisplay()
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE).edit()
        prefs.putString("github_token", GITHUB_TOKEN)
        prefs.putString("github_owner", repoOwner)
        prefs.putString("github_repo", repoName)
        prefs.putString("default_destination_path", savedDefaultPath)
        prefs.apply()
        updateStatusDisplay()
    }

    private fun updateStatusDisplay() {
        if (repoOwner.isNotEmpty() && repoName.isNotEmpty()) {
            tvStatus.text = "✅ $repoOwner/$repoName"
        } else if (GITHUB_TOKEN.isNotEmpty()) {
            tvStatus.text = "✅ Token — Handa na"
        } else {
            tvStatus.text = "⚠️ I-setup muna ang GitHub"
        }
    }

    private fun buildMainMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📋 PANGUNAHING MENU")
        addMenuButton("1️⃣ Ipadala ang Code sa GitHub") { showCodeSubmenu() }
        addMenuButton("2️⃣ Ipadala ang Cat Code / Patch") { showCatCodeMenu() }
        addMenuButton("3️⃣ Ipadala ang Icon") { showIconMenu() }
        addMenuButton("4️⃣ Pumili ng Destinasyon") { showDestinationMenu() }
        addMenuButton("5️⃣ Tungkol sa App") { showAboutDialog() }
        addMenuButton("6️⃣ I-setup ang GitHub") { showSetupMenu() }
        addMenuButton("7️⃣ I-Check ang Update") { checkVersionFromGitHub() }
    }

    private fun addMenuHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 18f
        tv.setPadding(48, 48, 48, 16)
        tv.setTextColor(0xFF1565C0.toInt())
        tv.setTypeface(null, android.graphics.Typeface.BOLD)
        mainMenuContainer.addView(tv)
    }

    private fun addMenuButton(text: String, action: () -> Unit) {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 15f
        btn.setPadding(48, 24, 48, 24)
        btn.setBackgroundColor(0xFFE3F2FD.toInt())
        btn.setTextColor(0xFF0D47A1.toInt())
        btn.setOnClickListener { action() }
        mainMenuContainer.addView(btn)
    }

    private fun addSubHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 14f
        tv.setPadding(64, 16, 64, 8)
        tv.setTextColor(0xFF616161.toInt())
        mainMenuContainer.addView(tv)
    }

    private fun showBackButton() {
        addMenuButton("🔙 Bumalik sa Menu") { buildMainMenu() }
    }

    // ==========================================
    // ✅ OPTION 1 — SUBMENU: Code / Cat Code
    // ==========================================
    private fun showCodeSubmenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📤 1 — Ipadala ang Code")
        addMenuButton("   1.1 — Ipadala ang Direktang Code") { showDirectCodeMenu() }
        addMenuButton("   1.2 — Ipadala ang Cat Code (Maraming Files)") { showCatCodeMenu() }
        showBackButton()
    }

    private fun showDirectCodeMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📤 1.1 — Direktang Code")
        addSubHeader("Ilagay ang filename:")
        val etFile = EditText(this)
        etFile.hint = "hal. MainActivity.kt"
        etFile.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etFile)
        addSubHeader("Ilagay ang code:")
        val etCode = EditText(this)
        etCode.hint = "I-paste dito ang code..."
        etCode.minLines = 8
        etCode.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etCode)
        addMenuButton("✅ Ipadala") {
            val filename = etFile.text.toString().trim()
            val code = etCode.text.toString()
            if (filename.isEmpty() || code.isEmpty()) {
                toast("❌ Punan ang filename at code")
                return@addMenuButton
            }
            promptDestinationAndUpload(filename, code)
        }
        showBackButton()
    }

    // ==========================================
    // ✅ OPTION 2 — CAT CODE + DESTINATION CHOICE
    // ==========================================
    private fun showCatCodeMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📤 1.2 — Cat Code / Patch")
        addSubHeader("I-paste ang buong cat code dito:")
        val etCatInput = EditText(this)
        etCatInput.hint = """Halimbawa:
FILE: README.md
# Aking Proyekto

FILE: docs/note.txt
Ito ay talaan...
""".trimIndent()
        etCatInput.minLines = 12
        etCatInput.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etCatInput)
        addMenuButton("✅ I-ANALYZE AT IPADALA") {
            val input = etCatInput.text.toString()
            if (input.isBlank()) {
                toast("❌ Walang naipaste na code")
                return@addMenuButton
            }
            processCatCode(input)
        }
        showBackButton()
    }

    private data class ParsedFile(val path: String, val content: String)

    private fun processCatCode(input: String) {
        val files = mutableListOf<ParsedFile>()
        val lines = input.lines().toMutableList()
        var i = 0
        var currentPath: String? = null
        val contentBuffer = mutableListOf<String>()

        fun flushFile() {
            currentPath?.let { path ->
                val content = contentBuffer.joinToString("\n")
                    .replace(Regex("(?s)(?i)\\b(ENDOFFILE|EOF|--- END ---)\\b.*$"), "")
                    .trim()
                if (path.isNotEmpty() && content.isNotEmpty()) {
                    files.add(ParsedFile(path, content))
                }
                contentBuffer.clear()
            }
            currentPath = null
        }

        while (i < lines.size) {
            val line = lines[i]
            val fileMatch = Regex("^FILE:\\s*(.+)$", RegexOption.IGNORE_CASE).find(line)
            if (fileMatch != null) {
                flushFile()
                currentPath = fileMatch.groupValues[1].trim()
                i++
                continue
            }
            val catMatch = Regex("^cat\\s*>\\s*(\\S+)\\s*<<", RegexOption.IGNORE_CASE).find(line)
            if (catMatch != null) {
                flushFile()
                currentPath = catMatch.groupValues[1].trim()
                i++
                continue
            }
            if (currentPath != null) {
                val endMarker = Regex("^\\s*(ENDOFFILE|EOF|--- END ---)\\s*$", RegexOption.IGNORE_CASE)
                if (endMarker.matches(line)) {
                    flushFile()
                } else {
                    contentBuffer.add(line)
                }
            }
            i++
        }
        flushFile()

        if (files.isEmpty()) {
            toast("❌ Walang matukoy na file")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("📋 Nakitang Files: ${files.size}")
                .setMessage(files.joinToString("\n") { "• ${it.path}" })
                .setPositiveButton("✅ GAMITIN ITO") { _, _ ->
                    askDestinationMode(files)
                }
                .setNegativeButton("❌ KANSELA", null)
                .show()
        }
    }

    private fun askDestinationMode(files: List<ParsedFile>) {
        val detectedPaths = files.map { it.path }.distinct()
        AlertDialog.Builder(this)
            .setTitle("📂 Pagpili ng Destinasyon")
            .setMessage(
                """
                Nakitang daan sa loob ng code:
                ${detectedPaths.joinToString("\n")}
                
                Anong gagawin?
                """.trimIndent()
            )
            .setPositiveButton("✅ GAMITIN ANG NAKITA SA CODE") { _, _ ->
                uploadFilesToGitHub(files)
            }
            .setNeutralButton("📂 PUMILI NG SARILING DESTINASYON") { _, _ ->
                showDestinationMenuForFiles(files)
            }
            .setNegativeButton("❌ KANSELA", null)
            .show()
    }

    private fun showDestinationMenuForFiles(files: List<ParsedFile>) {
        CoroutineScope(Dispatchers.IO).launch {
            val folders = fetchAllFolders()
            withContext(Dispatchers.Main) {
                if (folders.isEmpty()) {
                    toast("❌ Walang nakitang folder — I-setup muna ang GitHub")
                    return@withContext
                }
                // ✅ NA-AYOS: TINUKOY NA ANG URI — String
                val folderArray: Array<String> = folders.toTypedArray()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("📂 Pumili ng Base Folder")
                    .setItems(folderArray) { _, which ->
                        val basePath = folderArray[which]
                        savedDefaultPath = basePath
                        savePreferences()
                        val adjustedFiles = files.map {
                            it.copy(path = "$basePath/${it.path.substringAfterLast('/')}")
                        }
                        uploadFilesToGitHub(adjustedFiles)
                    }
                    .show()
            }
        }
    }

    // ==========================================
    // ✅ OPTION 3 — ICON UPLOAD
    // ==========================================
    private fun showIconMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🖼️ 3 — Ipadala ang Icon")
        addSubHeader("Piliin ang larawan mula sa Gallery")
        addMenuButton("📂 PUMILI NG LARAWAN") {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(intent, 300)
        }
        showBackButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == RESULT_OK) {
            data?.data?.let { uri -> processAndUploadIcon(uri) }
        }
    }

    private fun processAndUploadIcon(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sizes = listOf(
                    "mdpi" to 48,
                    "hdpi" to 72,
                    "xhdpi" to 96,
                    "xxhdpi" to 144,
                    "xxxhdpi" to 192
                )
                val output = mutableListOf<Triple<String, Int, ByteArray>>()
                for ((qual, size) in sizes) {
                    val stream = contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    stream?.close()
                    val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, size, size, true)
                    val out = java.io.ByteArrayOutputStream()
                    scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    output.add(Triple(qual, size, out.toByteArray()))
                }
                withContext(Dispatchers.Main) {
                    toast("✅ ${output.size} na laki naproseso — Ipinapadala...")
                }
                for ((qual, _, bytes) in output) {
                    val path = "apps/GitHubUpdater/app/src/main/res/mipmap-$qual/ic_launcher.png"
                    uploadSingleFile(path, bytes, "🖼️ Icon $qual")
                }
                withContext(Dispatchers.Main) {
                    toast("✅ LAHAT NG ICON — NAIPADALA NA!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("❌ Error: ${e.message}")
                }
            }
        }
    }

    // ==========================================
    // ✅ OPTION 4 — DESTINATION MENU
    // ==========================================
    private fun showDestinationMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 4 — Pumili ng Destinasyon")
        if (savedDefaultPath.isNotEmpty()) {
            addSubHeader("💾 Kasalukuyan: $savedDefaultPath")
        }
        addSubHeader("🔍 Kinukuha ang lahat ng folder...")
        showBackButton()

        CoroutineScope(Dispatchers.IO).launch {
            val folders = fetchAllFolders()
            withContext(Dispatchers.Main) {
                mainMenuContainer.removeAllViews()
                addMenuHeader("📂 4 — Pumili ng Destinasyon")
                if (savedDefaultPath.isNotEmpty()) {
                    addSubHeader("💾 Kasalukuyan: $savedDefaultPath")
                }
                if (folders.isEmpty()) {
                    addSubHeader("⚠️ Walang nakitang folder — I-setup muna ang GitHub")
                } else {
                    addSubHeader("✅ ${folders.size} folder ang nakita — pumili:")
                    folders.forEach { path ->
                        addMenuButton("📁 $path") {
                            savedDefaultPath = path
                            savePreferences()
                            toast("✅ NAPILI: $path")
                            buildMainMenu()
                        }
                    }
                }
                showBackButton()
            }
        }
    }

    private suspend fun fetchAllFolders(): List<String> = withContext(Dispatchers.IO) {
        val list = mutableListOf<String>()
        if (repoOwner.isEmpty() || repoName.isEmpty() || GITHUB_TOKEN.isEmpty()) return@withContext emptyList()
        try {
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/git/trees/main?recursive=1").openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            if (conn.responseCode == 200) {
                val root = JSONObject(readText(conn.inputStream))
                val tree = root.optJSONArray("tree")
                if (tree != null) {
                    for (i in 0 until tree.length()) {
                        val item = tree.getJSONObject(i)
                        if (item.optString("type") == "tree") {
                            list.add(item.optString("path"))
                        }
                    }
                }
            }
            conn.disconnect()
            list.sort()
        } catch (e: Exception) {
            Log.e("FOLDERS", e.message ?: "error")
        }
        return@withContext list
    }

    // ==========================================
    // ✅ OPTION 5 — TUNGKOL SA APP
    // ==========================================
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("ℹ️ Tungkol sa App")
            .setMessage(
                """
                GitHub Updater — $CURRENT_VERSION
                
                Binuo ni: MartoDosko © 2026
                
                📌 Kakayahan:
                • Auto-check update sa pagbukas
                • Auto-detect bagong bersyon
                • I-download at i-install
                • Magpadala ng code sa GitHub
                • Magpadala ng icon at cat code
                • Pumili ng sariling destinasyon
                """.trimIndent()
            )
            .setPositiveButton("✅ Sige", null)
            .show()
    }

    // ==========================================
    // ✅ OPTION 6 — GITHUB SETUP
    // ==========================================
    private fun showSetupMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔐 6 — I-setup ang GitHub")
        addSubHeader("Username / May-ari ng Repository:")
        val etOwner = EditText(this)
        etOwner.setText(repoOwner)
        etOwner.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etOwner)
        addSubHeader("Pangalan ng Repository:")
        val etRepo = EditText(this)
        etRepo.setText(repoName)
        etRepo.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etRepo)
        addSubHeader("Personal Access Token:")
        val etToken = EditText(this)
        etToken.setText(GITHUB_TOKEN)
        etToken.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        etToken.setPadding(48, 16, 48, 16)
        mainMenuContainer.addView(etToken)
        addMenuButton("✅ I-SAVE") {
            repoOwner = etOwner.text.toString().trim()
            repoName = etRepo.text.toString().trim()
            GITHUB_TOKEN = etToken.text.toString().trim()
            if (repoOwner.isEmpty() || repoName.isEmpty() || GITHUB_TOKEN.isEmpty()) {
                toast("❌ Punan ang lahat")
                return@addMenuButton
            }
            savePreferences()
            toast("✅ NAI-SAVE NA!")
            buildMainMenu()
        }
        showBackButton()
    }

    // ==========================================
    // ✅ OPTION 7 — CHECK UPDATE + AUTO-DETECT
    // ==========================================
    private fun checkUpdateOnLaunch() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                if (conn.responseCode == 200) {
                    val release = JSONObject(readText(conn.inputStream))
                    conn.disconnect()
                    val latestTag = release.getString("tag_name")
                    val assets = release.optJSONArray("assets")
                    var latestName = ""
                    var latestDownload = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val n = asset.getString("name")
                            if (n.lowercase().endsWith(".apk")) {
                                latestName = n
                                latestDownload = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }
                    launch(Dispatchers.Main) {
                        if (latestName.isNotEmpty() && isNewerVersion(latestTag, CURRENT_VERSION)) {
                            latestApkName = latestName
                            latestApkUrl = latestDownload
                            showUpdateAvailableDialog(latestName, latestDownload, latestTag)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun checkVersionFromGitHub() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            toast("⚠️ I-setup muna ang GitHub (Option 6)")
            return
        }
        tvStatus.text = "🔍 Tinitignan sa Releases..."
        btnDownloadUpdate.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                if (conn.responseCode == 200) {
                    val release = JSONObject(readText(conn.inputStream))
                    conn.disconnect()
                    val latestTag = release.getString("tag_name")
                    val assets = release.optJSONArray("assets")
                    var latestName = ""
                    var latestDownload = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val n = asset.getString("name")
                            if (n.lowercase().endsWith(".apk")) {
                                latestName = n
                                latestDownload = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }
                    launch(Dispatchers.Main) {
                        if (latestName.isNotEmpty()) {
                            tvStatus.text = "✅ Nakita: $latestTag"
                            latestApkName = latestName
                            latestApkUrl = latestDownload
                            btnDownloadUpdate.visibility = View.VISIBLE
                            btnDownloadUpdate.text = "⬇️ I-UPDATE: $latestTag"
                            if (isNewerVersion(latestTag, CURRENT_VERSION)) {
                                showUpdateAvailableDialog(latestName, latestDownload, latestTag)
                            } else {
                                toast("✅ Kasalukuyan ka na sa pinakabagong bersyon")
                            }
                        } else {
                            tvStatus.text = "⚠️ Walang APK sa Releases"
                        }
                    }
                } else {
                    conn.disconnect()
                    launch(Dispatchers.Main) {
                        tvStatus.text = "⚠️ Walang Release pa"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvStatus.text = "❌ Error: ${e.message}"
                }
            }
        }
    }

    // ✅ TOTOONG PAGKUMPARA NG BERSYON
    private fun isNewerVersion(latestTag: String, currentTag: String): Boolean {
        val latest = latestTag.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val current = currentTag.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val maxParts = maxOf(latest.size, current.size)
        for (i in 0 until maxParts) {
            val l = latest.getOrNull(i) ?: 0
            val c = current.getOrNull(i) ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    private fun showUpdateAvailableDialog(apkName: String, downloadUrl: String, newTag: String) {
        AlertDialog.Builder(this)
            .setTitle("📢 MAY BAGONG BERSYON!")
            .setMessage(
                """
                Kasalukuyan: $CURRENT_VERSION
                Bago:        $newTag
                
                I-update na ba?
                """.trimIndent()
            )
            .setPositiveButton("✅ I-UPDATE AGAD") { _, _ ->
                downloadAndInstallUpdate()
            }
            .setNegativeButton("❌ HUWAG MUNA", null)
            .setCancelable(false)
            .show()
    }

    private fun downloadAndInstallUpdate() {
        val url = latestApkUrl ?: return
        val name = latestApkName ?: "update.apk"
        tvStatus.text = "⬇️ Dinadownload..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name)
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        input.copyTo(output)
                    }
                }
                conn.disconnect()

                launch(Dispatchers.Main) {
                    tvStatus.text = "✅ Na-download — Ini-install..."
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvStatus.text = "❌ Nabigo: ${e.message}"
                }
            }
        }
    }

    private fun installApk(apk: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", apk)
        } else {
            Uri.fromFile(apk)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // ==========================================
    // ✅ PAGPAPADALA SA GITHUB
    // ==========================================
    private fun promptDestinationAndUpload(path: String, content: String) {
        val dest = if (savedDefaultPath.isNotEmpty()) "$savedDefaultPath/$path" else path
        AlertDialog.Builder(this)
            .setTitle("📤 Ipadala sa GitHub")
            .setMessage("Daan: $dest\n\nTama ba?")
            .setPositiveButton("✅ OO") { _, _ ->
                uploadSingleFile(dest, content.toByteArray(StandardCharsets.UTF_8), "📤 $dest")
            }
            .setNeutralButton("📂 PALITAN ANG DAAN") { _, _ ->
                showDestinationMenu()
            }
            .setNegativeButton("❌ KANSELA", null)
            .show()
    }

    private fun uploadFilesToGitHub(files: List<ParsedFile>) {
        CoroutineScope(Dispatchers.IO).launch {
            var success = 0
            files.forEach { file ->
                if (uploadSingleFile(file.path, file.content.toByteArray(StandardCharsets.UTF_8), "📤 ${file.path}")) {
                    success++
                }
            }
            launch(Dispatchers.Main) {
                toast("✅ Tapos: $success / ${files.size} naipadala")
            }
        }
    }

    private suspend fun uploadSingleFile(path: String, content: ByteArray, desc: String): Boolean = withContext(Dispatchers.IO) {
        if (repoOwner.isEmpty() || repoName.isEmpty() || GITHUB_TOKEN.isEmpty()) {
            toast("❌ I-setup muna ang GitHub")
            return@withContext false
        }
        return@withContext try {
            val apiPath = path.replace(" ", "%20")
            var serverSha: String? = null
            val checkConn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$apiPath").openConnection() as HttpURLConnection
            checkConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            checkConn.setRequestProperty("Accept", "application/vnd.github+json")
            if (checkConn.responseCode == 200) {
                val json = JSONObject(readText(checkConn.inputStream))
                serverSha = json.optString("sha")
            }
            checkConn.disconnect()

            val body = JSONObject().apply {
                put("message", "$desc — Auto-upload")
                put("content", Base64.encodeToString(content, Base64.NO_WRAP))
                serverSha?.let { put("sha", it) }
            }.toString()

            val pushConn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$apiPath").openConnection() as HttpURLConnection
            pushConn.requestMethod = "PUT"
            pushConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            pushConn.setRequestProperty("Content-Type", "application/json")
            pushConn.doOutput = true
            pushConn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            val ok = pushConn.responseCode in 200..299
            pushConn.disconnect()
            withContext(Dispatchers.Main) {
                toast(if (ok) "✅ NAIPADALA: $path" else "❌ NABIGO: $path")
            }
            ok
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                toast("❌ Error: ${e.message}")
            }
            false
        }
    }

    private fun readText(stream: java.io.InputStream): String {
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
