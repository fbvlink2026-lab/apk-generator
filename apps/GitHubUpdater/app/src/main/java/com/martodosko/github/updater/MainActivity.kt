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

    private val VERSION = "v6.0.7 — Fixed Duplicate Label"
    private val CURRENT_APP_VERSION = "v6.0.7"

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processSelectedIcon(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainMenuContainer = findViewById(R.id.main_menu_container)
        mainScrollView = findViewById(R.id.main_scroll_view)
        tvStatus = findViewById(R.id.tv_status)
        tvCurrentVersion = findViewById(R.id.tv_current_version)
        btnDownloadUpdate = findViewById(R.id.btn_download_update)

        tvCurrentVersion.text = "📌 Bersyon: $CURRENT_APP_VERSION"
        btnDownloadUpdate.visibility = View.GONE

        findViewById<Button>(R.id.btn_check_version)?.setOnClickListener { checkVersionFromGitHub() }
        btnDownloadUpdate.setOnClickListener { downloadUpdate() }
        findViewById<Button>(R.id.btn_close_drawer)?.setOnClickListener { buildMainMenu() }

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
        addMenuButton("1️⃣ Ipadala ang Code") { sendCodeMenu() }
        addMenuButton("2️⃣ Ipadala ang Icon") { sendIconMenu() }
        addMenuButton("3️⃣ PATCH — Ayusin ang File") { patchFileMenu() }
        addMenuButton("4️⃣ Pumili ng Destinasyon") { selectDestinationMenu() }
        addMenuButton("5️⃣ Tungkol sa App") { aboutAppMenu() }
        addMenuButton("6️⃣ I-setup ang GitHub — Token Lang") { setupGitHubMenu() }
        addMenuButton("7️⃣ I-Check ang Update") { checkVersionFromGitHub() }
    }

    private fun addMenuHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 18f
        tv.setTextColor(0xFF1565C0.toInt())
        tv.setPadding(0, 24, 0, 12)
        tv.setTypeface(null, android.graphics.Typeface.BOLD)
        mainMenuContainer.addView(tv)
    }

    private fun addMenuButton(text: String, action: () -> Unit) {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 14f
        btn.setPadding(48, 24, 48, 24)
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
        tv.setPadding(16, 8, 16, 4)
        mainMenuContainer.addView(tv)
    }

    // ==========================================
    // ✅ OPTION 6 — TOKEN + AUTO-DETECT REPO
    // ==========================================
    private fun setupGitHubMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔐 6 — I-SETUP ANG GITHUB")
        addSubHeader("Ilagay ang Personal Access Token mula sa GitHub")
        addSubHeader("Pahintulot na kailangan: repo")

        val etToken = EditText(this)
        etToken.hint = "ghp_xxxxxxxxxxxxxx"
        etToken.setPadding(32, 24, 32, 24)
        mainMenuContainer.addView(etToken)

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

                // ✅ UNIQUE NA PANGALAN — WALANG DOBLE!
                val userRepoList = mutableListOf<Pair<String, String>>()
                for (index in 0 until reposArray.length()) {
                    val repo = reposArray.getJSONObject(index)
                    val fullName = repo.getString("full_name")
                    val name = repo.getString("name")
                    userRepoList.add(fullName to name)
                }

                launch(Dispatchers.Main) {
                    if (userRepoList.isEmpty()) {
                        Toast.makeText(this@MainActivity, "⚠️ Walang nakitang Repository!", Toast.LENGTH_LONG).show()
                        tvStatus.text = "⚠️ Walang Repository"
                        return@launch
                    }
                    showRepoSelectionDialog(token, userRepoList)
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
        val repoNames = repos.map { it.second }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("📂 PUMILI NG REPOSITORY")
            .setItems(repoNames) { _, which ->
                val selectedFullName = repos[which].first
                val parts = selectedFullName.split("/", limit = 2)
                if (parts.size == 2) {
                    saveGitHubInfo(token, parts[0], parts[1])
                    updateStatusDisplay()
                    Toast.makeText(this, "✅ NAPILI: $selectedFullName", Toast.LENGTH_LONG).show()
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
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 4 — PUMILI NG DESTINASYON")
        if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
        addSubHeader("🔍 Kinukuha ang lahat ng folder...")

        CoroutineScope(Dispatchers.IO).launch {
            val folders = fetchAllFoldersRecursive()
            launch(Dispatchers.Main) {
                mainMenuContainer.removeAllViews()
                addMenuHeader("📂 4 — PUMILI NG DESTINASYON")
                if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
                if (folders.isEmpty()) {
                    addSubHeader("⚠️ Walang nakitang folder!")
                    addSubHeader("💡 Suriin ang koneksyon o token")
                } else {
                    addSubHeader("✅ ${folders.size} folder — Pumili:")
                    folders.forEach { path ->
                        addMenuButton("📁 $path") {
                            saveDefaultPath(path)
                            Toast.makeText(this@MainActivity, "✅ NAPILI: $path", Toast.LENGTH_SHORT).show()
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
        tvStatus.text = "🔍 Tinitignan ang /docs..."
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
                        Toast.makeText(this@MainActivity, "✅ Ikaw ay nasa pinakabagong bersyon na", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    tvStatus.text = "⚠️ Walang .apk sa /docs"
                }
            }
        }
    }

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
                val fileObj = jsonArray.getJSONObject(i)
                val fileName = fileObj.getString("name")
                if (fileName.lowercase().endsWith(".apk")) {
                    val ver = extractVersionFromApkName(fileName)
                    val dlUrl = fileObj.optString("download_url", "")
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
        fun parseVersion(v: String) = v.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = parseVersion(latestTag)
        val currentParts = parseVersion(currentTag)
        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val lv = latestParts.getOrNull(i) ?: 0
            val cv = currentParts.getOrNull(i) ?: 0
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
    }

    private fun showUpdatePrompt(apkName: String, downloadUrl: String, versionTag: String) {
        AlertDialog.Builder(this)
            .setTitle("📢 MAY BAGONG BERSYON!")
            .setMessage("Kasalukuyan: $CURRENT_APP_VERSION\nBago: $versionTag\n\n📂 $apkName\n\nI-download at i-install na ba?")
            .setPositiveButton("✅ I-INSTALL NGAYON") { _, _ -> startUpdateDownload(downloadUrl, apkName) }
            .setNegativeButton("❌ MAMAYA NA", null)
            .setCancelable(false)
            .show()
    }

    private fun startUpdateDownload(downloadUrl: String, fileName: String) {
        Toast.makeText(this, "⬇️ Dinadownload...", Toast.LENGTH_LONG).show()
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
        addSubHeader("I-paste ang code dito:")

        val etCode = EditText(this)
        etCode.hint = "Ilagay dito ang cat code..."
        etCode.setPadding(32, 24, 32, 24)
        etCode.minLines = 12
        mainMenuContainer.addView(etCode)

        addMenuButton("✅ I-ANALYZE AT IPADALA") {
            val code = etCode.text.toString()
            if (code.isNotEmpty()) processCatCode(code)
            else Toast.makeText(this, "❌ Walang nilagay na code!", Toast.LENGTH_SHORT).show()
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun processCatCode(code: String) {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            return
        }
        tvStatus.text = "🔍 Sinusuri ang code..."
        val files = parseCatCodeIntoFiles(code)
        if (files.isEmpty()) {
            Toast.makeText(this, "❌ Hindi matukoy ang anumang file!", Toast.LENGTH_SHORT).show()
            tvStatus.text = "❌ Hindi matukoy"
            return
        }
        tvStatus.text = "✅ ${files.size} file — ipinapadala..."
        var successCount = 0
        files.forEach { file ->
            if (uploadSingleFile(file.path, file.content)) successCount++
        }
        tvStatus.text = "✅ Tapos — $successCount/${files.size} naipadala"
        Toast.makeText(this, "✅ $successCount/${files.size} naipadala!", Toast.LENGTH_LONG).show()
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
                    if (path.startsWith("./") || path.startsWith("/")) path = path.removePrefix("./").removePrefix("/")
                    i++
                    val contentLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim() !in setOf("--- END ---", "ENDOFFILE", "EOF")) {
                        contentLines.add(lines[i++])
                    }
                    val content = contentLines.joinToString("\n").trimEnd()
                    if (path.isNotEmpty() && content.isNotEmpty()) {
                        result.add(ParsedFile(path, content))
                    }
                    i++
                }
                line.startsWith("cat > ") -> {
                    var path = line.substringAfter("cat > ").substringBefore(" <<").trim()
                    if (path.startsWith("./") || path.startsWith("/")) path = path.removePrefix("./").removePrefix("/")
                    i++
                    val contentLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim() !in setOf("ENDOFFILE", "EOF")) {
                        contentLines.add(lines[i++])
                    }
                    val content = contentLines.joinToString("\n").trimEnd()
                    if (path.isNotEmpty() && content.isNotEmpty()) {
                        result.add(ParsedFile(path, content))
                    }
                    i++
                }
                else -> i++
            }
        }
        return result
    }

    private fun uploadSingleFile(path: String, content: String): Boolean {
        return try {
            val finalPath = if (savedDefaultPath.isNotEmpty() && !path.startsWith("/")) "$savedDefaultPath/$path" else path
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/${finalPath.replace(" ", "%20")}"

            val connGet = URL(apiUrl).openConnection() as HttpURLConnection
            connGet.requestMethod = "GET"
            connGet.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val sha = if (connGet.responseCode == 200) {
                val json = JSONObject(readConnectionText(connGet))
                connGet.disconnect()
                json.optString("sha", "")
            } else {
                connGet.disconnect()
                ""
            }

            val connPut = URL(apiUrl).openConnection() as HttpURLConnection
            connPut.requestMethod = "PUT"
            connPut.doOutput = true
            connPut.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            connPut.setRequestProperty("Content-Type", "application/json")

            val body = JSONObject().apply {
                put("message", "📤 Ipadala mula sa App: $finalPath")
                put("content", Base64.encodeToString(content.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                if (sha.isNotEmpty()) put("sha", sha)
            }
            connPut.outputStream.use { it.write(body.toString().toByteArray()) }
            val success = connPut.responseCode in 200..299
            connPut.disconnect()
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
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("🖼️ 2 — IPADALA ANG ICON")
        addSubHeader("Pumili ng larawan mula sa device:")
        addMenuButton("📂 PUMILI NG LARAWAN") {
            imagePickerLauncher.launch("image/*")
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun processSelectedIcon(uri: Uri) {
        tvStatus.text = "🔄 Pinoproseso ang icon..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sourceStream = contentResolver.openInputStream(uri)
                val original = android.graphics.BitmapFactory.decodeStream(sourceStream)
                sourceStream?.close()
                if (original == null) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "❌ Hindi mabasa ang larawan!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val sizes = listOf("mdpi" to 48, "hdpi" to 72, "xhdpi" to 96, "xxhdpi" to 144, "xxxhdpi" to 192)
                var uploadedCount = 0
                sizes.forEach { (qual, sizePx) ->
                    val scaled = Bitmap.createScaledBitmap(original, sizePx, sizePx, false)
                    val output = java.io.ByteArrayOutputStream()
                    scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
                    val pngBytes = output.toByteArray()
                    val path = "res/mipmap-$qual/ic_launcher.png"

                    val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$path"
                    val connGet = URL(apiUrl).openConnection() as HttpURLConnection
                    connGet.requestMethod = "GET"
                    connGet.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                    val sha = if (connGet.responseCode == 200) {
                        val json = JSONObject(readConnectionText(connGet))
                        connGet.disconnect()
                        json.optString("sha", "")
                    } else { connGet.disconnect(); "" }

                    val connPut = URL(apiUrl).openConnection() as HttpURLConnection
                    connPut.requestMethod = "PUT"
                    connPut.doOutput = true
                    connPut.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                    JSONObject().apply {
                        put("message", "🖼️ I-update icon: $qual")
                        put("content", Base64.encodeToString(pngBytes, Base64.NO_WRAP))
                        if (sha.isNotEmpty()) put("sha", sha)
                    }.toString().also { connPut.outputStream.use { out -> out.write(it.toByteArray()) } }
                    if (connPut.responseCode in 200..299) uploadedCount++
                    connPut.disconnect()
                }
                original.recycle()
                launch(Dispatchers.Main) {
                    tvStatus.text = "✅ $uploadedCount/${sizes.size} naipadala"
                    Toast.makeText(this@MainActivity, "✅ Tapos!", Toast.LENGTH_SHORT).show()
                    buildMainMenu()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ==========================================
    // ✅ OPTION 3 — PATCH
    // ==========================================
    private fun patchFileMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty() || repoName.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            return
        }
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔧 3 — PATCH — PALITAN ANG BAHAGI")
        addSubHeader("I-paste ang patch dito:")

        val etPatch = EditText(this)
        etPatch.hint = "FILE: path/file.kt\n--- HANAPIN ---\nlumang linya\n--- PALITAN ---\nbagong linya\n--- END ---"
        etPatch.setPadding(32, 24, 32, 24)
        etPatch.minLines = 10
        mainMenuContainer.addView(etPatch)

        addMenuButton("✅ I-APPLY ANG PATCH") {
            val patchText = etPatch.text.toString()
            if (patchText.isNotEmpty()) applyPatch(patchText)
            else Toast.makeText(this, "❌ Walang nilagay na patch!", Toast.LENGTH_SHORT).show()
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun applyPatch(patchText: String) {
        Toast.makeText(this, "🔧 Ina-apply...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val blocks = patchText.split("FILE:").filter { it.isNotEmpty() }
            var successCount = 0
            blocks.forEach { block ->
                val lines = block.lines()
                val path = lines.firstOrNull()?.trim() ?: return@forEach
                val findMarker = "--- HANAPIN ---"
                val replaceMarker = "--- PALITAN ---"
                val endMarker = "--- END ---"
                val startFind = block.indexOf(findMarker)
                val startReplace = block.indexOf(replaceMarker)
                val startEnd = block.indexOf(endMarker)
                if (startFind < 0 || startReplace < 0 || startEnd < 0 || startReplace <= startFind || startEnd <= startReplace) return@forEach

                val findPart = block.substring(startFind + findMarker.length, startReplace).trimIndent()
                val replacePart = block.substring(startReplace + replaceMarker.length, startEnd).trimIndent()

                val finalPath = if (savedDefaultPath.isNotEmpty() && !path.startsWith("/")) "$savedDefaultPath/$path" else path
                val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/${finalPath.replace(" ", "%20")}"

                val connGet = URL(apiUrl).openConnection() as HttpURLConnection
                connGet.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                if (connGet.responseCode != 200) { connGet.disconnect(); return@forEach }
                val jsonGet = JSONObject(readConnectionText(connGet))
                connGet.disconnect()
                val sha = jsonGet.getString("sha")
                val oldContent = String(Base64.decode(jsonGet.getString("content"), Base64.DEFAULT), StandardCharsets.UTF_8)

                if (!oldContent.contains(findPart)) return@forEach
                val newContent = oldContent.replace(findPart, replacePart)

                val connPut = URL(apiUrl).openConnection() as HttpURLConnection
                connPut.requestMethod = "PUT"
                connPut.doOutput = true
                connPut.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                JSONObject().apply {
                    put("message", "🔧 Patch: $finalPath")
                    put("content", Base64.encodeToString(newContent.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                    put("sha", sha)
                }.toString().also { connPut.outputStream.use { out -> out.write(it.toByteArray()) } }
                if (connPut.responseCode in 200..299) successCount++
                connPut.disconnect()
            }
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "✅ $successCount/${blocks.size} na-apply!", Toast.LENGTH_LONG).show()
                buildMainMenu()
            }
        }
    }

    // ==========================================
    // ✅ OPTION 5 — TUNGKOL SA APP
    // ==========================================
    private fun aboutAppMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("ℹ️ TUNGKOL SA APP")
        addSubHeader("MartoPush GitHub Updater")
        addSubHeader("Bersyon: $VERSION")
        addSubHeader("")
        addSubHeader("Ginawa ni: MartoDosko © 2026")
        addSubHeader("")
        addSubHeader("Kakayahan:")
        addSubHeader("• Magpadala ng code/file")
        addSubHeader("• Magpadala ng icon sa 5 sukat")
        addSubHeader("• Patch — Hanapin at Palitan")
        addSubHeader("• Auto-update mula sa /docs")
        addSubHeader("• GitHub Token Authentication")
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun readConnectionText(conn: HttpURLConnection): String {
        return BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
