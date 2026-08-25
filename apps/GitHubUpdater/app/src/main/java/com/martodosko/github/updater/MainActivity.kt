package com.martodosko.github.updater

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private var latestApkUrl: String = ""
    private var latestApkName: String = ""
    private var latestVersionFound: String = ""
    private var GITHUB_TOKEN = ""

    private val VERSION = "v6.0.6 — Fixed All Warnings"
    private val CURRENT_APP_VERSION = "v6.0.6"

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processSelectedIcon(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainMenuContainer = findViewById(R.id.main_menu_container)
        mainScrollView = findViewById(R.id.main_scroll_view)
        tvStatus = findViewById(R.id.tvStatus)
        tvCurrentVersion = findViewById(R.id.tvCurrentVersion)
        btnDownloadUpdate = findViewById(R.id.btnDownloadUpdate)

        tvCurrentVersion.text = "📌 Bersyon: $CURRENT_APP_VERSION"
        btnDownloadUpdate.visibility = View.GONE

        findViewById<Button>(R.id.btnCheckVersion)?.setOnClickListener { checkVersionFromGitHub() }
        btnDownloadUpdate.setOnClickListener { downloadUpdate() }
        findViewById<Button>(R.id.btnCloseDrawer)?.setOnClickListener { buildMainMenu() }

        loadGitHubToken()
        updateStatusDisplay()
        buildMainMenu()
        checkUpdateOnLaunch()
    }

    private fun updateStatusDisplay() {
        if (repoOwner.isNotEmpty() && repoName.isNotEmpty()) {
            tvStatus.text = "✅ $repoOwner/$repoName"
        } else if (GITHUB_TOKEN.isNotEmpty()) {
            tvStatus.text = "✅ Token — Handa na"
        } else {
            tvStatus.text = "⚠️ Walang Token — I-setup muna"
        }
    }

    private fun loadGitHubToken() {
        val prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
        GITHUB_TOKEN = prefs.getString("github_token", "") ?: ""
        repoOwner = prefs.getString("github_owner", "") ?: ""
        repoName = prefs.getString("github_repo", "") ?: ""
        savedDefaultPath = prefs.getString("default_destination_path", "") ?: ""
    }

    private fun saveGitHubInfo(token: String, owner: String, repo: String) {
        getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE).edit().apply {
            putString("github_token", token)
            putString("github_owner", owner)
            putString("github_repo", repo)
            apply()
        }
        GITHUB_TOKEN = token
        repoOwner = owner
        repoName = repo
    }

    private fun saveDefaultPath(path: String) {
        savedDefaultPath = path
        getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
            .edit().putString("default_destination_path", path).apply()
    }

    private fun buildMainMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📋 PANGUNAHING MENU")
        addMenuButton("1️⃣ Ipadala ang Code sa GitHub") { sendCodeMenu() }
        addMenuButton("2️⃣ Ipadala ang Icon sa GitHub") { sendIconMenu() }
        addMenuButton("3️⃣ PATCH — Ayusin ang File") { patchFileMenu() }
        addMenuButton("4️⃣ Pumili ng Destinasyon") { selectDestinationMenu() }
        addMenuButton("5️⃣ Tungkol sa App") { aboutAppMenu() }
        addMenuButton("6️⃣ I-setup ang GitHub — Token Lang") { setupGitHubMenu() }
        addMenuButton("7️⃣ I-Check ang Update") { checkVersionFromGitHub() }
    }

    private fun addMenuHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 16f
        tv.setTextColor(0xFF1565C0.toInt())
        tv.setPadding(0, 24, 0, 8)
        tv.setTypeface(null, android.graphics.Typeface.BOLD)
        mainMenuContainer.addView(tv)
    }

    private fun addMenuButton(text: String, action: () -> Unit) {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 14f
        btn.setPadding(24, 16, 24, 16)
        btn.setBackgroundColor(0xFFE3F2FD.toInt())
        btn.setTextColor(0xFF1565C0.toInt())
        btn.setOnClickListener { action() }
        mainMenuContainer.addView(btn)
    }

    private fun addSubHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 14f
        tv.setTextColor(0xFF666666.toInt())
        tv.setPadding(0, 16, 0, 4)
        mainMenuContainer.addView(tv)
    }

    // ==========================================
    // ✅ OPTION 6 — TOKEN + AUTO-DETECT REPO
    // ==========================================
    private fun setupGitHubMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔐 6 — I-SETUP ANG GITHUB")
        addSubHeader("👉 Ilagay lang ang Personal Access Token")
        addSubHeader("   — Awtomatikong kukunin ang iyong Repository")
        addSubHeader("")

        val etToken = EditText(this)
        etToken.hint = "GitHub Personal Access Token"
        etToken.setText(GITHUB_TOKEN)
        etToken.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        mainMenuContainer.addView(etToken)

        addSubHeader("💡 Saan makukuha?")
        addSubHeader("   GitHub → Settings → Developer settings →")
        addSubHeader("   Personal access tokens → Tokens (classic)")
        addSubHeader("   Pahintulot: repo")

        addMenuButton("✅ I-DETECT ANG REPOSITORY") {
            val token = etToken.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, "❌ Ilagay muna ang Token!", Toast.LENGTH_SHORT).show()
                return@addMenuButton
            }
            detectRepositoriesFromToken(token)
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun detectRepositoriesFromToken(token: String) {
        tvStatus.text = "🔍 Binabasa ang iyong mga Repository..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/user/repos?per_page=100").openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "token $token")
                conn.setRequestProperty("Accept", "application/json")

                if (conn.responseCode != 200) {
                    conn.disconnect()
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "❌ Maling Token o hindi makakonekta!", Toast.LENGTH_LONG).show()
                        tvStatus.text = "❌ Hindi makabasa — Suriin ang Token"
                    }
                    return@launch
                }

                val reposArray = JSONArray(readConnectionText(conn))
                conn.disconnect()

                // ✅ NATATANGING PANGALAN — WALANG DOBLE!
                val userRepos = mutableListOf<Pair<String, String>>()
                for (i in 0 until reposArray.length()) {
                    val repo = reposArray.getJSONObject(i)
                    val fullName = repo.getString("full_name")
                    val name = repo.getString("name")
                    userRepos.add(fullName to name)
                }

                launch(Dispatchers.Main) {
                    if (userRepos.isEmpty()) {
                        Toast.makeText(this@MainActivity, "⚠️ Walang nakitang Repository!", Toast.LENGTH_LONG).show()
                        tvStatus.text = "⚠️ Walang Repository"
                        return@launch
                    }
                    showRepoSelectionDialog(token, userRepos)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                    tvStatus.text = "❌ Nabigo"
                }
            }
        }
    }

    private fun showRepoSelectionDialog(token: String, repos: List<Pair<String, String>>) {
        val namesArray = repos.map { it.second }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("📂 PUMILI NG REPOSITORY")
            .setMessage("${repos.size} na nakitang Repository — Pumili:")
            .setItems(namesArray) { _, which ->
                val selectedFull = repos[which].first
                val parts = selectedFull.split("/", limit = 2)
                if (parts.size == 2) {
                    saveGitHubInfo(token, parts[0], parts[1])
                    updateStatusDisplay()
                    Toast.makeText(this, "✅ NAPILI: ${parts[0]}/${parts[1]}", Toast.LENGTH_LONG).show()
                    buildMainMenu()
                }
            }
            .setCancelable(false)
            .show()
    }

    // ==========================================
    // ✅ OPTION 4 — LAHAT NG FOLDER + SUBFOLDER
    // ==========================================
    private fun selectDestinationMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 4 — PUMILI NG DESTINASYON")
        if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
        addSubHeader("🔍 Kinukuha ang lahat ng folder at subfolder...")

        CoroutineScope(Dispatchers.IO).launch {
            val folders = fetchAllFoldersRecursive()
            launch(Dispatchers.Main) {
                mainMenuContainer.removeAllViews()
                addMenuHeader("📂 4 — PUMILI NG DESTINASYON")
                if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
                if (folders.isEmpty()) {
                    addSubHeader("⚠️ Walang nakitang folder!")
                    addSubHeader("💡 Suriin ang koneksyon")
                } else {
                    addSubHeader("✅ ${folders.size} folder/subfolder — Pumili:")
                    folders.forEach { path ->
                        addMenuButton("📁 $path") {
                            saveDefaultPath(path)
                            Toast.makeText(this@MainActivity, "✅ Napili: $path", Toast.LENGTH_SHORT).show()
                            buildMainMenu()
                        }
                    }
                }
                addMenuButton("🔙 Bumalik") { buildMainMenu() }
            }
        }
    }

    private suspend fun fetchAllFoldersRecursive(): List<String> = withContext(Dispatchers.IO) {
        val result = mutableSetOf<String>()
        try {
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/git/trees/main?recursive=1").openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            if (conn.responseCode != 200) { conn.disconnect(); return@withContext emptyList() }
            val root = JSONObject(readConnectionText(conn))
            conn.disconnect()
            val tree = root.optJSONArray("tree") ?: return@withContext emptyList()
            for (i in 0 until tree.length()) {
                val item = tree.getJSONObject(i)
                if (item.optString("type", "") == "tree") {
                    result.add(item.getString("path"))
                }
            }
            return@withContext result.sorted().toList()
        } catch (e: Exception) {
            Log.e("FOLDERS", "Error: ${e.message}")
            emptyList()
        }
    }

    // ==========================================
    // ✅ AUTO-UPDATE MULA SA /docs FOLDER
    // ==========================================
    private fun checkUpdateOnLaunch() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val apkInfo = findLatestApkInDocsFolder()
            if (apkInfo != null) {
                latestApkName = apkInfo.first
                latestApkUrl = apkInfo.second
                latestVersionFound = apkInfo.third
                if (isNewerVersion(latestVersionFound, CURRENT_APP_VERSION)) {
                    launch(Dispatchers.Main) {
                        showUpdatePrompt(latestApkName, latestApkUrl, latestVersionFound)
                    }
                }
            }
        }
    }

    private fun checkVersionFromGitHub() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            return
        }
        tvStatus.text = "🔍 Tinitignan sa /docs folder..."
        btnDownloadUpdate.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val apkInfo = findLatestApkInDocsFolder()
            launch(Dispatchers.Main) {
                if (apkInfo != null) {
                    latestApkName = apkInfo.first
                    latestApkUrl = apkInfo.second
                    latestVersionFound = apkInfo.third
                    tvStatus.text = "✅ NAKITA: ${apkInfo.first}"
                    btnDownloadUpdate.visibility = View.VISIBLE
                    btnDownloadUpdate.text = "⬇️ I-UPDATE: ${apkInfo.third}"
                    if (isNewerVersion(apkInfo.third, CURRENT_APP_VERSION)) {
                        showUpdatePrompt(apkInfo.first, apkInfo.second, apkInfo.third)
                    } else {
                        Toast.makeText(this@MainActivity, "✅ Kasalukuyan ka na sa pinakabagong bersyon", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    tvStatus.text = "⚠️ Walang .apk sa /docs folder"
                }
            }
        }
    }

    // ✅ AYUSIN: SIGURADONG WALANG NULL — PALAGING MAY DEFAULT NA HALAGA!
    private suspend fun findLatestApkInDocsFolder(): Triple<String, String, String>? = withContext(Dispatchers.IO) {
        try {
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/docs").openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            if (conn.responseCode != 200) { conn.disconnect(); return@withContext null }
            val jsonArray = JSONArray(readConnectionText(conn))
            conn.disconnect()

            var latestName = ""
            var latestDownload = ""
            var latestVer = ""

            for (i in 0 until jsonArray.length()) {
                val f = jsonArray.getJSONObject(i)
                val fileName = f.getString("name")
                if (fileName.lowercase().endsWith(".apk")) {
                    val ver = extractVersionFromApkName(fileName)
                    val dlUrl = f.optString("download_url", "") // ✅ LAGING STRING — HINDI NULL!
                    if (ver.isNotEmpty()) {
                        if (latestVer.isEmpty() || isNewerVersion(ver, latestVer)) {
                            latestVer = ver
                            latestName = fileName
                            latestDownload = dlUrl
                        }
                    } else if (fileName > latestName) {
                        latestName = fileName
                        latestDownload = dlUrl
                    }
                }
            }
            return@withContext if (latestName.isNotEmpty()) Triple(latestName, latestDownload, latestVer) else null
        } catch (e: Exception) {
            Log.e("UPDATE", "Error: ${e.message}")
            null
        }
    }

    private fun extractVersionFromApkName(name: String): String {
        val match = Regex("v[\\d.]+").find(name)
        return match?.value ?: ""
    }

    private fun isNewerVersion(latestTag: String, currentTag: String): Boolean {
        if (latestTag.isEmpty()) return false
        fun parse(v: String) = v.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val l = parse(latestTag)
        val c = parse(currentTag)
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrNull(i) ?: 0
            val cv = c.getOrNull(i) ?: 0
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
    }

    private fun showUpdatePrompt(apkName: String, downloadUrl: String, versionTag: String) {
        AlertDialog.Builder(this)
            .setTitle("📢 MAY BAGONG BERSYON!")
            .setMessage("""
                Kasalukuyan: $CURRENT_APP_VERSION
                Bago:        $versionTag
                
                📂 Pinagmulan: /docs/$apkName
                I-update na ba?
            """.trimIndent())
            .setPositiveButton("✅ I-UPDATE AGAD") { _, _ -> startUpdateDownload(downloadUrl, apkName) }
            .setNegativeButton("❌ HUWAG MUNA", null)
            .setCancelable(false)
            .show()
    }

    private fun startUpdateDownload(downloadUrl: String, fileName: String) {
        Toast.makeText(this, "⬇️ Dinadownload mula sa /docs...", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
                if (!outDir.exists()) outDir.mkdirs()
                val apkFile = File(outDir, fileName)
                val conn = URL(downloadUrl).openConnection() as HttpURLConnection
                conn.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output -> input.copyTo(output, 8192) }
                }
                conn.disconnect()
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "✅ Na-download! Ini-install...", Toast.LENGTH_LONG).show()
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Nabigo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun downloadUpdate() {
        if (latestApkUrl.isEmpty() || latestApkName.isEmpty()) {
            Toast.makeText(this, "❌ Walang nakitang APK!", Toast.LENGTH_SHORT).show()
            return
        }
        startUpdateDownload(latestApkUrl, latestApkName)
    }

    private fun installApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            })
            Toast.makeText(this, "⚠️ Payagan muna ang pag-install", Toast.LENGTH_LONG).show()
            return
        }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ==========================================
    // ✅ OPTION 1 — IPADALA ANG CODE
    // ==========================================
    private fun sendCodeMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📤 1 — IPADALA ANG CODE")
        addSubHeader("I-paste ang code sa ibaba:")
        val input = EditText(this)
        input.hint = "I-paste dito ang cat code o buong laman ng file..."
        input.setLines(12)
        input.setBackgroundColor(0xFFFFFFFF.toInt())
        input.setPadding(16, 16, 16, 16)
        mainMenuContainer.addView(input)
        addMenuButton("✅ Ipadala Ngayon") {
            val code = input.text.toString()
            if (code.isNotEmpty()) processCatCode(code)
            else Toast.makeText(this, "❌ Walang nakapasok na code!", Toast.LENGTH_SHORT).show()
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun processCatCode(code: String) {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            tvStatus.text = "🔍 Sinusuri ang code..."
            val files = parseCatCodeIntoFiles(code)
            if (files.isEmpty()) {
                Toast.makeText(this@MainActivity, "❌ Walang matukoy na file!", Toast.LENGTH_SHORT).show()
                tvStatus.text = "❌ Hindi matukoy ang file"
                return@launch
            }
            tvStatus.text = "✅ ${files.size} file na matukoy — ipapadala..."
            var ok = 0
            files.forEach { if (uploadSingleFile(it.path, it.content)) ok++ }
            tvStatus.text = "✅ Tapos — $ok/${files.size} naipadala"
            Toast.makeText(this@MainActivity, "✅ $ok/${files.size} naipadala!", Toast.LENGTH_LONG).show()
        }
    }

    private data class ParsedFile(val path: String, val content: String)

    private fun parseCatCodeIntoFiles(code: String): List<ParsedFile> {
        val result = mutableListOf<ParsedFile>()
        val lines = code.lines().toMutableList()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            when {
                line.startsWith("FILE:") -> {
                    var path = line.substringAfter("FILE:").trim().substringBefore(" ")
                    i++
                    val content = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim() !in setOf("--- END ---", "ENDOFFILE", "EOF")) {
                        content.add(lines[i++])
                    }
                    if (path.isNotEmpty() && content.isNotEmpty()) {
                        result.add(ParsedFile(path, content.joinToString("\n").trimIndent()))
                    }
                    i++
                }
                line.startsWith("cat > ") -> {
                    val path = line.substringAfter("cat > ").substringBefore(" <<").trim()
                    i++
                    val content = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim() !in setOf("ENDOFFILE", "EOF")) {
                        content.add(lines[i++])
                    }
                    if (path.isNotEmpty() && content.isNotEmpty()) {
                        result.add(ParsedFile(path, content.joinToString("\n")))
                    }
                    i++
                }
                else -> i++
            }
        }
        return result
    }

    private suspend fun uploadSingleFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val finalPath = if (!path.startsWith("/") && savedDefaultPath.isNotEmpty()) "$savedDefaultPath/$path" else path
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/${finalPath.replace(" ", "%20")}"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val sha = if (conn.responseCode == 200) JSONObject(readConnectionText(conn)).optString("sha", "") else ""
            conn.disconnect()

            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val body = JSONObject().apply {
                put("message", "📤 Update mula sa App: $finalPath")
                put("content", Base64.encodeToString(content.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                if (sha.isNotEmpty()) put("sha", sha)
            }
            putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val success = putConn.responseCode in 200..299
            putConn.disconnect()
            success
        } catch (e: Exception) {
            Log.e("UPLOAD", "Error: ${e.message}")
            false
        }
    }

    // ==========================================
    // ✅ OPTION 2 — IPADALA ANG ICON
    // ==========================================
    private fun sendIconMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("🖼️ 2 — IPADALA ANG ICON")
        addSubHeader("Piliin ang larawan mula sa Gallery")
        addMenuButton("📂 Pumili ng Larawan") { imagePickerLauncher.launch("image/*") }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun processSelectedIcon(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            tvStatus.text = "🔄 Pinoproseso ang Icon..."
            val bitmap = contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            } ?: return@launch
            val sizes = listOf("mdpi" to 48, "hdpi" to 72, "xhdpi" to 96, "xxhdpi" to 144, "xxxhdpi" to 192)
            var uploaded = 0
            for ((qual, size) in sizes) {
                val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, size, size, true)
                val path = "apps/GitHubUpdater/app/src/main/res/mipmap-$qual/ic_launcher.png"
                if (uploadBitmapFile(scaled, path)) uploaded++
                tvStatus.text = "⏳ $qual — $uploaded/${sizes.size}"
            }
            tvStatus.text = "✅ Tapos — $uploaded/${sizes.size} naipadala"
            Toast.makeText(this@MainActivity, "✅ $uploaded naipadala!", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun uploadBitmapFile(bitmap: android.graphics.Bitmap, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/${path.replace(" ", "%20")}"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val sha = if (conn.responseCode == 200) JSONObject(readConnectionText(conn)).optString("sha", "") else ""
            conn.disconnect()

            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
            val body = JSONObject().apply {
                put("message", "🖼️ Icon: $path")
                put("content", Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP))
                if (sha.isNotEmpty()) put("sha", sha)
            }
            putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val ok = putConn.responseCode in 200..299
            putConn.disconnect()
            ok
        } catch (e: Exception) { false }
    }

    // ==========================================
    // ✅ OPTION 3 — PATCH
    // ==========================================
    private fun patchFileMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔧 3 — PATCH")
        addSubHeader("I-paste ang patch sa ibaba:")
        val input = EditText(this)
        input.hint = "FILE: path/file.ext\n--- HANAPIN ---\n...\n--- PALITAN ---\n...\n--- END ---"
        input.setLines(15)
        input.setBackgroundColor(0xFFFFFFFF.toInt())
        input.setPadding(16,16,16,16)
        mainMenuContainer.addView(input)
        addMenuButton("✅ I-apply ang Patch") {
            val text = input.text.toString()
            if (text.isNotEmpty()) applyPatch(text)
            else Toast.makeText(this, "❌ Walang patch!", Toast.LENGTH_SHORT).show()
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private data class PatchFile(val path: String, val find: String, val replace: String)

    private fun applyPatch(patchText: String) {
        Toast.makeText(this, "🔧 Patch — sinusuri...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            val patches = patchText.split("FILE:").filter { it.isNotEmpty() }.mapNotNull { block ->
                val lines = block.lines()
                val path = lines.firstOrNull()?.trim() ?: return@mapNotNull null
                val find = block.substringAfter("--- HANAPIN ---").substringBefore("--- PALITAN ---").trimIndent()
                val repl = block.substringAfter("--- PALITAN ---").substringBefore("--- END ---").trimIndent()
                if (find.isNotEmpty() && repl.isNotEmpty()) PatchFile(path, find, repl) else null
            }
            if (patches.isEmpty()) {
                Toast.makeText(this@MainActivity, "❌ Hindi matukoy ang patch!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            var ok = 0
            patches.forEach { if (applyPatchToFile(it.path, it.find, it.replace)) ok++ }
            Toast.makeText(this@MainActivity, "✅ $ok/${patches.size} na-apply!", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun applyPatchToFile(path: String, find: String, replace: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val finalPath = if (!path.startsWith("/") && savedDefaultPath.isNotEmpty()) "$savedDefaultPath/$path" else path
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/${finalPath.replace(" ", "%20")}"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            if (conn.responseCode != 200) return@withContext false
            val json = JSONObject(readConnectionText(conn))
            conn.disconnect()
            val content = String(Base64.decode(json.getString("content"), Base64.DEFAULT), StandardCharsets.UTF_8)
            val sha = json.getString("sha")
            if (!content.contains(find)) return@withContext false
            val newContent = content.replace(find, replace)
            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val body = JSONObject().apply {
                put("message", "🔧 Patch: $finalPath")
                put("content", Base64.encodeToString(newContent.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                put("sha", sha)
            }
            putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val success = putConn.responseCode in 200..299
            putConn.disconnect()
            success
        } catch (e: Exception) { false }
    }

    private fun aboutAppMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("ℹ️ TUNGKOL SA APP")
        addSubHeader("GitHub Updater — $VERSION")
        addSubHeader("Binuo ni: MartoDosko © 2026")
        if (repoOwner.isNotEmpty() && repoName.isNotEmpty()) addSubHeader("Repository: $repoOwner/$repoName")
        if (savedDefaultPath.isNotEmpty()) addSubHeader("✅ Daan: $savedDefaultPath")
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun readConnectionText(conn: HttpURLConnection): String =
        BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
}
