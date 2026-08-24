package com.martodosko.github.updater

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
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

    private var repoOwner = "fbvlink2026-lab"
    private var repoName = "apk-generator"
    private var savedDefaultPath = ""
    private var latestApkUrl: String? = null
    private var latestApkName: String? = null
    private var GITHUB_TOKEN = ""

    private val VERSION = "v6.0.2 — Auto-Update"

    companion object {
        private const val REQUEST_INSTALL_PERMISSION = 1001
    }

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
        buildMainMenu()

        // ✅ PAGBUKAS NG APP — AGAD TIGNAN ANG UPDATE
        checkUpdateOnLaunch()
    }

    private fun updateStatusDisplay() {
        tvStatus.text = "✅ $repoOwner/$repoName"
    }

    private fun loadGitHubToken() {
        val prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
        GITHUB_TOKEN = prefs.getString("github_token", "") ?: ""
        val savedOwner = prefs.getString("github_owner", "") ?: ""
        val savedRepo = prefs.getString("github_repo", "") ?: ""
        if (savedOwner.isNotEmpty()) repoOwner = savedOwner
        if (savedRepo.isNotEmpty()) repoName = savedRepo
    }

    private fun saveGitHubToken(token: String, owner: String, repo: String) {
        val prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE).edit()
        prefs.putString("github_token", token)
        prefs.putString("github_owner", owner)
        prefs.putString("github_repo", repo)
        prefs.apply()
        GITHUB_TOKEN = token
        repoOwner = owner
        repoName = repo
    }

    private fun getGitHubToken(): String = GITHUB_TOKEN

    private fun buildMainMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📋 PANGUNAHING MENU")
        addMenuButton("1️⃣ Ipadala ang Code sa GitHub") { sendCodeMenu() }
        addMenuButton("2️⃣ Ipadala ang Icon sa GitHub") { sendIconMenu() }
        addMenuButton("3️⃣ PATCH — Ayusin ang File") { patchFileMenu() }
        addMenuButton("4️⃣ Pumili ng Destinasyon") { selectDestinationMenu() }
        addMenuButton("5️⃣ Tungkol sa App") { aboutAppMenu() }
        addMenuButton("6️⃣ I-setup ang GitHub") { setupGitHubMenu() }
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
        CoroutineScope(Dispatchers.Main).launch {
            tvStatus.text = "🔍 Sinusuri ang code..."
            val files = parseCatCodeIntoFiles(code)
            if (files.isEmpty()) {
                Toast.makeText(this@MainActivity, "❌ Walang matukoy na file!", Toast.LENGTH_SHORT).show()
                tvStatus.text = "❌ Hindi matukoy ang file"
                return@launch
            }
            tvStatus.text = "✅ ${files.size} file na matukoy — ipapadala..."
            var successCount = 0
            for (file in files) {
                if (uploadSingleFile(file.path, file.content)) successCount++
            }
            tvStatus.text = "✅ Tapos — $successCount/${files.size} naipadala"
            Toast.makeText(this@MainActivity, "✅ $successCount/${files.size} naipadala!", Toast.LENGTH_LONG).show()
        }
    }

    private data class ParsedFile(val path: String, val content: String)

    private fun parseCatCodeIntoFiles(code: String): List<ParsedFile> {
        val result = mutableListOf<ParsedFile>()
        val lines = code.lines().toMutableList()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("FILE:") || line.startsWith("## FILE:") || line.startsWith("# FILE:")) {
                var path = line.substringAfter("FILE:").trim()
                if (path.contains(" ")) path = path.substringBefore(" ")
                i++
                val contentLines = mutableListOf<String>()
                var endFound = false
                while (i < lines.size && !endFound) {
                    val cl = lines[i]
                    if (cl.trim() == "--- END ---" || cl.trim() == "ENDOFFILE" || cl.trim() == "EOF" || cl.trim() == "---") endFound = true
                    else contentLines.add(cl)
                    i++
                }
                val content = contentLines.joinToString("\n").trim('\n', ' ')
                if (path.isNotEmpty() && content.isNotEmpty()) result.add(ParsedFile(path, content))
            } else if (line.startsWith("cat > ")) {
                val path = line.substringAfter("cat > ").substringBefore(" <<").trim()
                i++
                val contentLines = mutableListOf<String>()
                var endFound = false
                while (i < lines.size && !endFound) {
                    val cl = lines[i]
                    if (cl.trim() == "ENDOFFILE" || cl.trim() == "EOF" || cl.startsWith("<< ")) endFound = true
                    else contentLines.add(cl)
                    i++
                }
                val content = contentLines.joinToString("\n")
                if (path.isNotEmpty() && content.isNotEmpty()) result.add(ParsedFile(path, content))
            } else { i++ }
        }
        return result
    }

    private suspend fun uploadSingleFile(path: String, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val apiPath = path.replace(" ", "%20")
                val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$apiPath"
                val conn = URL(apiUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                var sha: String? = null
                if (conn.responseCode == 200) {
                    val json = JSONObject(readConnectionText(conn))
                    sha = json.optString("sha", null)
                }
                conn.disconnect()
                val putConn = URL(apiUrl).openConnection() as HttpURLConnection
                putConn.requestMethod = "PUT"
                putConn.doOutput = true
                putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                putConn.setRequestProperty("Content-Type", "application/json")
                val encoded = Base64.encodeToString(content.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                val body = JSONObject()
                body.put("message", "📤 Update mula sa App: $path")
                body.put("content", encoded)
                if (!sha.isNullOrEmpty()) body.put("sha", sha)
                putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
                val ok = putConn.responseCode in 200..299
                putConn.disconnect()
                ok
            } catch (e: Exception) { Log.e("UPLOAD", "Error: ${e.message}"); false }
        }
    }

    private fun sendIconMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🖼️ 2 — IPADALA ANG ICON")
        addSubHeader("Piliin ang larawan mula sa Gallery")
        addMenuButton("📂 Pumili ng Larawan") {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            val uri = data.data ?: return
            processSelectedIcon(uri)
        }
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

    private suspend fun uploadBitmapFile(bitmap: android.graphics.Bitmap, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val apiPath = path.replace(" ", "%20")
                val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$apiPath"
                val conn = URL(apiUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                val sha = if (conn.responseCode == 200) {
                    JSONObject(readConnectionText(conn)).optString("sha", null)
                } else null
                conn.disconnect()
                val putConn = URL(apiUrl).openConnection() as HttpURLConnection
                putConn.requestMethod = "PUT"
                putConn.doOutput = true
                putConn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                val baos = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
                val encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                val body = JSONObject()
                body.put("message", "🖼️ Icon: $path")
                body.put("content", encoded)
                if (!sha.isNullOrEmpty()) body.put("sha", sha)
                putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
                val ok = putConn.responseCode in 200..299
                putConn.disconnect()
                ok
            } catch (e: Exception) { false }
        }
    }

    private fun patchFileMenu() {
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
                if (applyPatchToFile(pf.path, pf.find, pf.replace)) okCount++
            }
            Toast.makeText(this@MainActivity, "✅ $okCount/${files.size} na-apply!", Toast.LENGTH_LONG).show()
        }
    }

    private data class PatchFile(val path: String, val find: String, val replace: String)

    private fun parsePatch(text: String): List<PatchFile> {
        val result = mutableListOf<PatchFile>()
        val blocks = text.split("FILE:").filter { it.isNotEmpty() }
        for (block in blocks) {
            val lines = block.lines()
            val path = lines.firstOrNull()?.trim() ?: continue
            val findPart = block.substringAfter("--- HANAPIN ---").substringBefore("--- PALITAN ---")
            val replPart = block.substringAfter("--- PALITAN ---").substringBefore("--- END ---")
            if (findPart.isNotEmpty() && replPart.isNotEmpty()) {
                result.add(PatchFile(path, findPart.trimIndent(), replPart.trimIndent()))
            }
        }
        return result
    }

    private suspend fun applyPatchToFile(path: String, find: String, replace: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val apiPath = path.replace(" ", "%20")
                val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$apiPath"
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
                val body = JSONObject()
                body.put("message", "🔧 Patch: $path")
                body.put("content", Base64.encodeToString(newContent.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                body.put("sha", sha)
                putConn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
                val ok = putConn.responseCode in 200..299
                putConn.disconnect()
                ok
            } catch (e: Exception) { false }
        }
    }

    private fun selectDestinationMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("📂 4 — PUMILI NG DESTINASYON")
        addSubHeader("I-scan ang repository...")
        CoroutineScope(Dispatchers.Main).launch {
            tvStatus.text = "🔍 Binabasa ang folder..."
            val folders = fetchRepoFolders()
            if (folders.isEmpty()) {
                addSubHeader("⚠️ Walang nakitang folder — i-setup muna ang GitHub")
            } else {
                folders.forEachIndexed { i, folder ->
                    addMenuButton("${i+1}. $folder") {
                        savedDefaultPath = folder
                        Toast.makeText(this@MainActivity, "✅ Napili: $folder", Toast.LENGTH_SHORT).show()
                        tvStatus.text = "✅ Destinasyon: $folder"
                    }
                }
            }
            addMenuButton("🔙 Bumalik") { buildMainMenu() }
        }
    }

    private suspend fun fetchRepoFolders(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents").openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                conn.setRequestProperty("Accept", "application/json")
                if (conn.responseCode != 200) return@withContext emptyList()
                val arr = JSONArray(readConnectionText(conn))
                conn.disconnect()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    if (obj.optString("type") == "dir") list.add(obj.getString("name"))
                }
                list
            } catch (e: Exception) { emptyList() }
        }
    }

    private fun aboutAppMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("ℹ️ TUNGKOL SA APP")
        addSubHeader("GitHub Updater — $VERSION")
        addSubHeader("Binuo ni: MartoDosko © 2026")
        addSubHeader("Repository: $repoOwner/$repoName")
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    private fun setupGitHubMenu() {
        mainMenuContainer.removeAllViews()
        addMenuHeader("🔐 6 — I-SETUP ANG GITHUB")
        val etToken = EditText(this)
        etToken.hint = "GitHub Token"
        etToken.setText(GITHUB_TOKEN)
        mainMenuContainer.addView(etToken)
        val etOwner = EditText(this)
        etOwner.hint = "Username / Organization"
        etOwner.setText(repoOwner)
        mainMenuContainer.addView(etOwner)
        val etRepo = EditText(this)
        etRepo.hint = "Pangalan ng Repository"
        etRepo.setText(repoName)
        mainMenuContainer.addView(etRepo)
        addMenuButton("✅ I-save") {
            saveGitHubToken(
                etToken.text.toString().trim(),
                etOwner.text.toString().trim(),
                etRepo.text.toString().trim()
            )
            updateStatusDisplay()
            Toast.makeText(this, "✅ Nai-save na!", Toast.LENGTH_SHORT).show()
            buildMainMenu()
        }
        addMenuButton("🔙 Bumalik") { buildMainMenu() }
    }

    // ==========================================
    // ✅ AUTO-UPDATE SYSTEM — TIGNAN SA PAGBUKAS
    // ==========================================

    private fun checkUpdateOnLaunch() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/docs").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/json")
                if (GITHUB_TOKEN.isNotEmpty()) {
                    conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                }

                if (conn.responseCode == 200) {
                    val jsonArray = JSONArray(readConnectionText(conn))
                    conn.disconnect()

                    var latestName = ""
                    var latestDownload = ""

                    for (i in 0 until jsonArray.length()) {
                        val file = jsonArray.getJSONObject(i)
                        val name = file.getString("name")
                        if (name.lowercase().endsWith(".apk")) {
                            if (name > latestName) {
                                latestName = name
                                latestDownload = file.optString("download_url", "") ?: ""
                            }
                        }
                    }

                    launch(Dispatchers.Main) {
                        if (latestName.isNotEmpty()) {
                            latestApkName = latestName
                            latestApkUrl = latestDownload
                            showUpdatePrompt(latestName, latestDownload)
                        }
                    }
                } else {
                    conn.disconnect()
                }
            } catch (_: Exception) {
                // Tahimik lang kung walang internet — hindi isturbo ang user
            }
        }
    }

    private fun showUpdatePrompt(apkName: String, downloadUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("📢 MAY BAGONG VERSYON!")
            .setMessage("Nakita ang bagong bersyon:\n\n📄 $apkName\n\nGusto mo bang i-update na ngayon?")
            .setPositiveButton("✅ OO — I-UPDATE AGAD") { _, _ ->
                startUpdateDownload(downloadUrl, apkName)
            }
            .setNegativeButton("❌ HUWAG MUNA", null)
            .setCancelable(false)
            .show()
    }

    private fun startUpdateDownload(downloadUrl: String, fileName: String) {
        Toast.makeText(this, "⬇️ Dinadownload ang update...", Toast.LENGTH_LONG).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir!!.exists()) downloadsDir.mkdirs()
                val apkFile = File(downloadsDir, fileName)

                val conn = URL(downloadUrl).openConnection() as HttpURLConnection
                conn.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        input.copyTo(output, 8192)
                    }
                }
                conn.disconnect()

                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "✅ Na-download na! Sinisimulan ang pag-install...", Toast.LENGTH_LONG).show()
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "❌ Nabigo: ${e.message}\nBubuksan sa browser...", Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    startActivity(intent)
                }
            }
        }
    }

    private fun installApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                Toast.makeText(this, "⚠️ Payagan muna ang pag-install mula sa app na ito", Toast.LENGTH_LONG).show()
                return
            }
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                apkFile
            )
        } else {
            Uri.fromFile(apkFile)
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // 🔍 MANUAL CHECK — Pindutin ang Button
    private fun checkVersionFromGitHub() {
        tvStatus.text = "🔍 Tinitignan sa docs/ folder..."
        btnDownloadUpdate.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/docs").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/json")
                if (GITHUB_TOKEN.isNotEmpty()) {
                    conn.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
                }

                if (conn.responseCode == 200) {
                    val jsonArray = JSONArray(readConnectionText(conn))
                    conn.disconnect()

                    var latestApkName = ""
                    var latestDownloadLink = ""

                    for (i in 0 until jsonArray.length()) {
                        val file = jsonArray.getJSONObject(i)
                        val name = file.getString("name")
                        if (name.lowercase().endsWith(".apk")) {
                            if (name > latestApkName) {
                                latestApkName = name
                                latestDownloadLink = file.optString("download_url", "") ?: "" ?: ""
                            }
                        }
                    }

                    launch(Dispatchers.Main) {
                        if (latestApkName.isNotEmpty()) {
                            tvStatus.text = "✅ NAKITA: $latestApkName"
                            latestApkUrl = latestDownloadLink
                            latestApkName = latestApkName
                            btnDownloadUpdate.visibility = View.VISIBLE
                            btnDownloadUpdate.text = "⬇️ I-DOWNLOAD: $latestApkName"
                            Toast.makeText(this@MainActivity, "✅ May nakitang APK!", Toast.LENGTH_LONG).show()
                        } else {
                            tvStatus.text = "⚠️ Walang .apk sa docs/"
                            btnDownloadUpdate.visibility = View.GONE
                        }
                    }
                } else {
                    conn.disconnect()
                    launch(Dispatchers.Main) {
                        tvStatus.text = "❌ Hindi mabasa — Code: ${conn.responseCode}"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    tvStatus.text = "❌ Error: ${e.message}"
                }
            }
        }
    }

    // ⬇️ MANUAL DOWNLOAD — Pindutin ang Button
    private fun downloadUpdate() {
        if (latestApkUrl.isNullOrEmpty() || latestApkName.isNullOrEmpty()) {
            Toast.makeText(this, "❌ Walang nakitang APK!", Toast.LENGTH_SHORT).show()
            return
        }
        startUpdateDownload(latestApkUrl!!, latestApkName!!)
    }

    private fun readConnectionText(conn: HttpURLConnection): String {
        return BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
