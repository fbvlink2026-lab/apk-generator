package com.martodosko.github.updater

import android.Manifest
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
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

// ==========================================
// 📤 MARTOPUSH — GitHub Updater & Uploader
// ✅ VERSION: v6.0.4 — AYUSIN: Folder List + Auto-Update from /docs
// Developed by MartoDosko © 2026
// ==========================================

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

    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var detectedPackagePath: String = ""
    private var detectedJavaRootPath: String = ""
    private val allComPaths = mutableListOf<String>()
    private val scannedFolders = mutableListOf<GitHubFolder>()

    private val VERSION = "v6.0.4 — ✅ Folder List + Auto-Update"

    data class GitHubFolder(val path: String, val type: String, val displayName: String)
    data class ParsedFile(val path: String, val content: String)
    data class PatchFile(val path: String, val find: String, val replace: String)

    private val iconSizes = listOf(
        "mipmap-mdpi" to 48,
        "mipmap-hdpi" to 72,
        "mipmap-xhdpi" to 96,
        "mipmap-xxhdpi" to 144,
        "mipmap-xxxhdpi" to 192
    )

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { processSelectedIcon(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainMenuContainer = findViewById(R.id.main_menu_container)
        mainScrollView = findViewById(R.id.main_scroll_view)
        tvStatus = findViewById(R.id.tvStatus)
        tvCurrentVersion = findViewById(R.id.tvCurrentVersion)
        btnDownloadUpdate = findViewById(R.id.btnDownloadUpdate)

        tvCurrentVersion.text = "📌 Bersyon: $VERSION"
        btnDownloadUpdate.visibility = View.GONE

        findViewById<Button>(R.id.btnCheckVersion)?.setOnClickListener { checkVersionFromGitHub() }
        btnDownloadUpdate.setOnClickListener { downloadUpdate() }
        findViewById<Button>(R.id.btnCloseDrawer)?.setOnClickListener { buildMainMenu() }

        loadGitHubToken()
        updateStatusDisplay()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen != "MAIN") buildMainMenu() else finish()
            }
        })

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
        repoName = prefs.getString("github_repo", "apk-generator") ?: "apk-generator"
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
        getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
            .edit().putString("default_destination_path", path).apply()
        savedDefaultPath = path
    }

    private fun scrollToTop() = mainScrollView.scrollTo(0, 0)

    private fun readConnectionText(conn: HttpURLConnection): String =
        BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }

    // ==========================================
    // ✅ MAIN MENU
    // ==========================================
    private fun buildMainMenu() {
        currentScreen = "MAIN"
        scrollToTop()
        mainMenuContainer.removeAllViews()
        addMenuHeader("========================================")
        addMenuHeader("   📤 MARTOPUSH   $VERSION")
        addMenuHeader("    Developed by MartoDosko © 2026")
        addMenuHeader("========================================")
        if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 DESTINASYON: $savedDefaultPath")
        addMenuDivider()
        addMenuButton("1️⃣ Ipadala ang Code / Cat File") { sendCodeMenu() }
        addMenuButton("2️⃣ Ipadala ang Icon — Auto Resize") { sendIconMenu() }
        addMenuButton("3️⃣ PATCH — Hanapin → Palitan") { patchFileMenu() }
        addMenuButton("4️⃣ Pumili ng Destinasyon — BUONG LISTAHAN") { selectDestinationMenu() }
        addMenuButton("5️⃣ I-Check ang Update mula sa /docs") { checkVersionFromGitHub() }
        addMenuButton("6️⃣ I-setup ang GitHub — Token Lang") { setupGitHubMenu() }
        addMenuButton("7️⃣ Tungkol sa App") { aboutAppMenu() }
        addMenuDivider()
        addMenuButton("↩️ Lumabas") { finish() }
    }

    // ==========================================
    // ✅ OPTION 6 — GITHUB SETUP: Token → Auto-List Repos
    // ==========================================
    private fun setupGitHubMenu() {
        currentScreen = "SETUP"
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔐 6 — I-SETUP ANG GITHUB")
        addSubHeader("👉 Ilagay lang ang Personal Access Token")
        addSubHeader("   — Awtomatikong ililista ang iyong Repository")
        addSubHeader("")

        val etToken = EditText(this)
        etToken.hint = "GitHub Personal Access Token"
        etToken.setText(GITHUB_TOKEN)
        etToken.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        mainMenuContainer.addView(etToken)

        addSubHeader("💡 Saan makukuha?")
        addSubHeader("   GitHub → Settings → Developer settings →")
        addSubHeader("   Personal access tokens → Tokens (classic)")
        addSubHeader("   Sakop: repo")

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

                val reposList = mutableListOf<Pair<String, String>>()
                for (i in 0 until reposArray.length()) {
                    val repo = reposArray.getJSONObject(i)
                    val fullName = repo.getString("full_name")
                    reposList.add(fullName to fullName)
                }

                launch(Dispatchers.Main) {
                    if (reposList.isEmpty()) {
                        Toast.makeText(this@MainActivity, "⚠️ Walang nakitang Repository!", Toast.LENGTH_LONG).show()
                        tvStatus.text = "⚠️ Walang Repository"
                        return@launch
                    }
                    showRepoSelectionDialog(token, reposList)
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
                    Toast.makeText(this, "✅ NAPILI: $selectedFull", Toast.LENGTH_LONG).show()
                    scannedFolders.clear()
                    buildMainMenu()
                }
            }
            .setCancelable(false)
            .show()
    }

    // ==========================================
    // ✅ OPTION 4 — DESTINASYON: BUONG LISTAHAN NG FOLDER + SUBFOLDER
    // ==========================================
    private fun selectDestinationMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub (Option 6)", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }

        currentScreen = "DESTINATION"
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 4 — PUMILI NG DESTINASYON")

        if (savedDefaultPath.isNotEmpty()) {
            addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
            addMenuDivider()
        }

        if (scannedFolders.size < 3) {
            addSubHeader("🔍 SCANNING NG BUONG REPOSITORY...")
            CoroutineScope(Dispatchers.IO).launch {
                scanAllFoldersRecursive()
                launch(Dispatchers.Main) { showFolderListMenu() }
            }
        } else {
            showFolderListMenu()
        }
    }

    private fun showFolderListMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 BUONG LISTAHAN NG FOLDER — ${scannedFolders.size} na nakita")
        addSubHeader("💾 KASALUKUYANG: $savedDefaultPath")
        addMenuDivider()

        scannedFolders.forEach { folder ->
            addMenuButton(folder.displayName) {
                saveDefaultPath(folder.path)
                Toast.makeText(this, "✅ NAPILI: ${folder.path}", Toast.LENGTH_SHORT).show()
                selectDestinationMenu()
            }
        }

        addMenuDivider()
        addMenuButton("✏️ I-type ang sariling daan") { showCustomPathInput() }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun showCustomPathInput() {
        val inp = EditText(this)
        inp.hint = "hal: docs/  o  app/src/main/java/"
        inp.setText(savedDefaultPath)
        AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG DAAN")
            .setView(inp)
            .setPositiveButton("✅ I-SAVE") { _, _ ->
                var p = inp.text.toString().trim()
                if (p.isNotEmpty()) {
                    if (!p.endsWith("/") && !p.contains(".")) p = "$p/"
                    saveDefaultPath(p)
                    Toast.makeText(this, "💾 NA-SAVE: $p", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("❌ Kanselahin", null)
            .show()
    }

    private suspend fun scanAllFoldersRecursive() {
        scannedFolders.clear()
        allComPaths.clear()
        detectedPackagePath = ""
        detectedJavaRootPath = ""
        scanDirectoryRecursive("")
        scannedFolders.add(0, GitHubFolder(".", "root", "🏠 ROOT"))
        scannedFolders.add(GitHubFolder("docs/", "docs", "📄 docs/"))
        val seen = mutableSetOf<String>()
        scannedFolders.removeAll { !seen.add(it.path) }

        if (allComPaths.isNotEmpty()) {
            detectedPackagePath = allComPaths.maxByOrNull { it.split("/").size } ?: ""
        }
        val manifestPkg = tryReadManifestPackage()
        if (manifestPkg.isNotEmpty()) detectedPackagePath = manifestPkg
    }

    private suspend fun scanDirectoryRecursive(path: String, depth: Int = 0, maxDepth: Int = 15) {
        if (depth > maxDepth) return
        try {
            val apiPath = if (path.isEmpty()) "" else "/$path"
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents$apiPath").openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) { conn.disconnect(); return }
            val arr = JSONArray(readConnectionText(conn))
            conn.disconnect()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("type") == "dir") {
                    val folderPath = obj.getString("path") + "/"
                    classifyAndAddFolder(folderPath)
                    scanDirectoryRecursive(folderPath, depth + 1, maxDepth)
                }
            }
        } catch (_: Exception) {}
    }

    private fun classifyAndAddFolder(fp: String) {
        val type: String
        val displayName: String

        if (fp.contains("/java/") && detectedJavaRootPath.isEmpty()) {
            val idx = fp.indexOf("/java/")
            detectedJavaRootPath = fp.substring(0, idx + 6)
        }
        if ("/com/" in fp) {
            val parts = fp.split("/").filter { it.isNotEmpty() }
            val comIdx = parts.indexOf("com")
            if (comIdx >= 0 && comIdx < parts.lastIndex) {
                val pkgPath = parts.drop(comIdx).joinToString("/") + "/"
                if (!allComPaths.contains(pkgPath)) allComPaths.add(pkgPath)
            }
        }
        when {
            fp.contains("mipmap-") || fp.contains("drawable") -> {
                displayName = "🖼️ $fp"; type = "icon"
            }
            fp.contains("/java/") && "/com/" in fp -> {
                displayName = "📦 PACKAGE → $fp"; type = "package"
            }
            fp.contains("/java/") || fp.contains("kotlin/") -> {
                displayName = "💻 $fp"; type = "code"
            }
            fp.contains("layout/") || fp.contains("values/") -> {
                displayName = "🎨 LAYOUT/RES → $fp"; type = "layout"
            }
            else -> { displayName = "📁 $fp"; type = "other" }
        }
        scannedFolders.add(GitHubFolder(fp, type, displayName))
    }

    private suspend fun tryReadManifestPackage(): String {
        try {
            val paths = listOf("app/src/main/AndroidManifest.xml", "src/main/AndroidManifest.xml", "AndroidManifest.xml")
            for (p in paths) {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$p").openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                if (conn.responseCode == 200) {
                    val json = JSONObject(readConnectionText(conn))
                    conn.disconnect()
                    val xml = String(Base64.decode(json.getString("content").replace("\n", ""), Base64.DEFAULT))
                    val parser = Xml.newPullParser()
                    parser.setInput(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)), null)
                    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                        if (parser.eventType == XmlPullParser.START_TAG && parser.name == "manifest") {
                            val pkg = parser.getAttributeValue(null, "package")
                            if (!pkg.isNullOrEmpty()) return pkg.replace(".", "/") + "/"
                        }
                        parser.next()
                    }
                } else conn.disconnect()
            }
        } catch (_: Exception) {}
        return ""
    }

    // ==========================================
    // ✅ OPTION 1 — SEND CODE / CAT FILE
    // ==========================================
    private fun sendCodeMenu() {
        currentScreen = "CODE"
        mainMenuContainer.removeAllViews()
        addMenuHeader("📤 1 — IPADALA ANG CODE / CAT FILE")
        if (savedDefaultPath.isNotEmpty()) addSubHeader("💾 DESTINASYON: $savedDefaultPath")
        addSubHeader("I-paste ang code sa ibaba:")
        val input = EditText(this)
        input.hint = "--- FILE: path/file.kt ---\n\nLaman ng file dito...\n\n--- END ---"
        input.setLines(14)
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
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub!", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            tvStatus.text = "🔍 Sinusuri ang code..."
            val files = parseCatCodeIntoFiles(code)
            if (files.isEmpty()) {
                Toast.makeText(this@MainActivity, "❌ Walang matukoy na file!", Toast.LENGTH_SHORT).show()
                tvStatus.text = "❌ Hindi matukoy"
                return@launch
            }
            tvStatus.text = "✅ ${files.size} file — ipapadala..."
            var ok = 0
            files.forEach { file ->
                var path = file.path
                if (!path.contains("/") && savedDefaultPath.isNotEmpty()) {
                    path = savedDefaultPath + path
                }
                if (uploadSingleFile(path, file.content)) ok++
            }
            tvStatus.text = "✅ Tapos — $ok/${files.size}"
            Toast.makeText(this@MainActivity, "✅ $ok/${files.size} naipadala!", Toast.LENGTH_LONG).show()
        }
    }

    private fun parseCatCodeIntoFiles(code: String): List<ParsedFile> {
        val result = mutableListOf<ParsedFile>()
        val lines = code.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var path: String? = null
            val content = StringBuilder()
            var reading = false
            when {
                line.startsWith("--- FILE:") -> {
                    path = line.removePrefix("--- FILE:").substringBefore("---").trim()
                    reading = true; i++
                }
                line.startsWith("cat > ") && line.contains("<<") -> {
                    path = line.substringAfter("cat > ").substringBefore("<<").trim()
                    reading = true; i++
                }
                else -> { i++; continue }
            }
            while (i < lines.size && reading) {
                val cl = lines[i]
                val t = cl.trim()
                if (t == "--- END ---" || t == "EOF" || t == "ENDOFFILE") { i++; break }
                if (content.isNotEmpty()) content.append("\n")
                content.append(cl); i++
            }
            if (!path.isNullOrBlank() && content.isNotBlank()) {
                result.add(ParsedFile(path, content.toString().trimEnd()))
            }
        }
        return result
    }

    private suspend fun uploadSingleFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$path"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            var sha: String? = null
            if (conn.responseCode == 200) sha = JSONObject(readConnectionText(conn)).optString("sha", null)
            conn.disconnect()

            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val body = JSONObject()
                .put("message", "📤 Ipadala mula sa App: $path")
                .put("content", Base64.encodeToString(content.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
            if (!sha.isNullOrEmpty()) body.put("sha", sha)
            OutputStreamWriter(putConn.outputStream).use { it.write(body.toString()) }
            val ok = putConn.responseCode in 200..299
            putConn.disconnect()
            ok
        } catch (e: Exception) { Log.e("UPLOAD", e.message); false }
    }

    // ==========================================
    // ✅ OPTION 2 — ICON UPLOAD
    // ==========================================
    private fun sendIconMenu() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub!", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }
        currentScreen = "ICON"
        mainMenuContainer.removeAllViews()
        addMenuHeader("🖼️ 2 — ICON")
        addSubHeader("Pumili ng larawan → Awtomatikong i-resize sa 5 sukat → Ipadala")
        addMenuButton("📂 Pumili ng Larawan") { imagePickerLauncher.launch("image/*") }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun processSelectedIcon(uri: Uri) {
        selectedImageUri = uri
        Toast.makeText(this, "✅ Napili — Pinoproseso...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val bm = contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            } ?: return@launch
            var ok = 0
            for ((folder, size) in iconSizes) {
                val scaled = android.graphics.Bitmap.createScaledBitmap(bm, size, size, true)
                var path = "$folder/ic_launcher.png"
                if (savedDefaultPath.isNotEmpty()) path = savedDefaultPath + path
                if (uploadBitmapFile(scaled, path)) ok++
                withContext(Dispatchers.Main) {
                    tvStatus.text = "⏳ $folder — $ok/${iconSizes.size}"
                }
                delay(300)
            }
            withContext(Dispatchers.Main) {
                tvStatus.text = "✅ Tapos — $ok/${iconSizes.size}"
                Toast.makeText(this@MainActivity, "✅ $ok naipadala!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun uploadBitmapFile(bitmap: android.graphics.Bitmap, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$path"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            var sha: String? = null
            if (conn.responseCode == 200) sha = JSONObject(readConnectionText(conn)).optString("sha", null)
            conn.disconnect()

            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val body = JSONObject().put("message", "🖼️ Icon: $path").put("content", b64)
            if (!sha.isNullOrEmpty()) body.put("sha", sha)
            OutputStreamWriter(putConn.outputStream).use { it.write(body.toString()) }
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
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub!", Toast.LENGTH_SHORT).show()
            setupGitHubMenu()
            return
        }
        currentScreen = "PATCH"
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔧 3 — PATCH")
        addSubHeader("Hanapin → Palitan — Hindi kailangang buong file")
        val input = EditText(this)
        input.hint = "FILE: path/file.ext\n--- HANAPIN ---\nlumang linya\n--- PALITAN ---\nbagong linya\n--- END ---"
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

    private fun applyPatch(patchText: String) {
        Toast.makeText(this, "🔧 Patch — sinusuri...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            val files = parsePatch(patchText)
            if (files.isEmpty()) {
                Toast.makeText(this@MainActivity, "❌ Hindi matukoy ang patch!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            var okCount = 0
            for (pf in files) {
                var path = pf.path
                if (!path.contains("/") && savedDefaultPath.isNotEmpty()) path = savedDefaultPath + path
                if (applyPatchToFile(path, pf.find, pf.replace)) okCount++
            }
            Toast.makeText(this@MainActivity, "✅ $okCount/${files.size} na-apply!", Toast.LENGTH_LONG).show()
        }
    }

    private fun parsePatch(text: String): List<PatchFile> {
        val result = mutableListOf<PatchFile>()
        text.split("FILE:").filter { it.isNotEmpty() }.forEach { block ->
            val path = block.lines().firstOrNull()?.trim() ?: return@forEach
            val find = block.substringAfter("--- HANAPIN ---").substringBefore("--- PALITAN ---").trimIndent()
            val repl = block.substringAfter("--- PALITAN ---").substringBefore("--- END ---").trimIndent()
            if (find.isNotEmpty() && repl.isNotEmpty()) {
                result.add(PatchFile(path, find, repl))
            }
        }
        return result
    }

    private suspend fun applyPatchToFile(path: String, find: String, replace: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$path"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            if (conn.responseCode != 200) return@withContext false
            val json = JSONObject(readConnectionText(conn))
            conn.disconnect()
            val content = String(Base64.decode(json.getString("content").replace("\n",""), Base64.DEFAULT), StandardCharsets.UTF_8)
            val sha = json.getString("sha")
            if (!content.contains(find)) return@withContext false
            val newContent = content.replace(find, replace)
            val putConn = URL(apiUrl).openConnection() as HttpURLConnection
            putConn.requestMethod = "PUT"
            putConn.doOutput = true
            putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            val body = JSONObject()
                .put("message", "🔧 Patch: $path")
                .put("content", Base64.encodeToString(newContent.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                .put("sha", sha)
            OutputStreamWriter(putConn.outputStream).use { it.write(body.toString()) }
            val ok = putConn.responseCode in 200..299
            putConn.disconnect()
            ok
        } catch (e: Exception) { false }
    }

    // ==========================================
    // ✅ AUTO-UPDATE MULA SA /docs FOLDER
    // ==========================================
    private fun checkUpdateOnLaunch() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch { checkLatestApkInDocs() }
    }

    private fun checkVersionFromGitHub() {
        if (GITHUB_TOKEN.isEmpty() || repoOwner.isEmpty()) {
            Toast.makeText(this, "⚠️ I-setup muna ang GitHub!", Toast.LENGTH_SHORT).show()
            return
        }
        tvStatus.text = "🔍 Tinitignan sa /docs..."
        btnDownloadUpdate.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            checkLatestApkInDocs()
        }
    }

    private suspend fun checkLatestApkInDocs() {
        try {
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/docs").openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            if (conn.responseCode != 200) { conn.disconnect(); return }
            val arr = JSONArray(readConnectionText(conn))
            conn.disconnect()
            var latestName = ""
            var latestUrl = ""
            for (i in 0 until arr.length()) {
                val f = arr.getJSONObject(i)
                val n = f.getString("name")
                if (n.lowercase().endsWith(".apk") && n > latestName) {
                    latestName = n
                    latestUrl = f.optString("download_url", "") ?: ""
                }
            }
            withContext(Dispatchers.Main) {
                if (latestName.isNotEmpty()) {
                    latestApkName = latestName
                    latestApkUrl = latestUrl
                    tvStatus.text = "✅ NAKITA SA /docs: $latestName"
                    btnDownloadUpdate.visibility = View.VISIBLE
                    btnDownloadUpdate.text = "⬇️ I-DOWNLOAD: $latestName"
                    checkAndPromptUpdate(latestName, latestUrl)
                } else {
                    tvStatus.text = "⚠️ Walang .apk sa /docs/"
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { tvStatus.text = "❌ Error: ${e.message}" }
        }
    }

    private var alreadyPrompted = false
    private fun checkAndPromptUpdate(apkName: String, downloadUrl: String) {
        if (alreadyPrompted) return
        alreadyPrompted = true
        AlertDialog.Builder(this)
            .setTitle("📢 MAY BAGONG VERSYON!")
            .setMessage("Nakita sa /docs folder:\n📄 $apkName\n\nI-update na ba?")
            .setPositiveButton("✅ OO — I-download at I-install") { _, _ ->
                startUpdateDownload(downloadUrl, apkName)
            }
            .setNegativeButton("❌ HUWAG MUNA", null)
            .setCancelable(false)
            .show()
    }

    private fun downloadUpdate() {
        if (latestApkUrl.isNullOrEmpty() || latestApkName.isNullOrEmpty()) {
            Toast.makeText(this, "❌ Walang nakitang APK!", Toast.LENGTH_SHORT).show()
            return
        }
        startUpdateDownload(latestApkUrl!!, latestApkName!!)
    }

    private fun startUpdateDownload(downloadUrl: String, fileName: String) {
        Toast.makeText(this, "⬇️ Dinadownload ang $fileName...", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val apkFile = File(downloadsDir, fileName)
                val conn = URL(downloadUrl).openConnection() as HttpURLConnection
                conn.inputStream.use { input ->
                    FileOutputStream(apkFile).use { fos ->
                        input.copyTo(fos, 8192)
                    }
                }
                conn.disconnect()
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "✅ Na-download! Sinisimulan ang pag-install...", Toast.LENGTH_LONG).show()
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Nabigo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun installApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            Toast.makeText(this, "⚠️ Payagan muna ang pag-install mula sa app na ito", Toast.LENGTH_LONG).show()
            return
        }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // ==========================================
    // ✅ OPTION 7 — ABOUT
    // ==========================================
    private fun aboutAppMenu() {
        currentScreen = "ABOUT"
        mainMenuContainer.removeAllViews()
        addMenuHeader("ℹ️ TUNGKOL SA APP")
        addSubHeader("📤 MartoPush GitHub Updater")
        addSubHeader("Bersyon: $VERSION")
        addSubHeader("Binuo ni: MartoDosko © 2026")
        if (repoOwner.isNotEmpty() && repoName.isNotEmpty()) {
            addSubHeader("Repository: $repoOwner/$repoName")
        }
        if (savedDefaultPath.isNotEmpty()) {
            addSubHeader("✅ Default Path: $savedDefaultPath")
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    // ==========================================
    // ✅ UI HELPERS
    // ==========================================
    private fun addMenuHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 16f
        tv.setTextColor(0xFF1565C0.toInt())
        tv.setPadding(0, 24, 0, 8)
        tv.setTypeface(null, android.graphics.Typeface.BOLD)
        tv.gravity = Gravity.CENTER
        mainMenuContainer.addView(tv)
    }

    private fun addSubHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 14f
        tv.setTextColor(0xFF555555.toInt())
        tv.setPadding(0, 8, 0, 4)
        mainMenuContainer.addView(tv)
    }

    private fun addMenuButton(text: String, action: () -> Unit) {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 14f
        btn.setPadding(32, 16, 32, 16)
        btn.setBackgroundColor(0xFFE3F2FD.toInt())
        btn.setTextColor(0xFF1565C0.toInt())
        btn.setOnClickListener { action() }
        mainMenuContainer.addView(btn)
    }

    private fun addMenuDivider() {
        val v = View(this)
        v.setBackgroundColor(0xFFE0E0E0.toInt())
        v.layoutParams = LinearLayout.LayoutParams(-1, 1)
        mainMenuContainer.addView(v)
    }
}
