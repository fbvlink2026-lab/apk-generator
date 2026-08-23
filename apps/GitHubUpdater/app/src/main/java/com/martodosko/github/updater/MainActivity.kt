package com.martodosko.github.updater

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Xml
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    // ==========================================
    // ✅ DECLARASYON — UNA SA LAHAT!
    // ==========================================
    private lateinit var prefs: SharedPreferences
    private lateinit var mainScrollView: ScrollView       // ← DECLARE FIRST
    private lateinit var menuContainer: LinearLayout      // ← DECLARE FIRST
    private val VERSION = "v5.99 — Fixed Declaration Order"

    private var repoOwner = ""
    private var repoName = ""
    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var savedDefaultPath = ""
    private var detectedPackagePath = ""
    private var detectedJavaRootPath = ""
    private val allComPaths = mutableListOf<String>()

    private val iconSizes = listOf(
        "mipmap-mdpi" to 48,
        "mipmap-hdpi" to 72,
        "mipmap-xhdpi" to 96,
        "mipmap-xxhdpi" to 144,
        "mipmap-xxxhdpi" to 192
    )

    data class GitHubFolder(val path: String, val type: String, val name: String)

    data class CatFileEntry(
        val originalPath: String,
        val fileName: String,
        val content: String,
        val hasExplicitPath: Boolean,
        val explicitFullPath: String?
    )

    private val scannedFolders = mutableListOf<GitHubFolder>()
    private val parsedCatFiles = mutableListOf<CatFileEntry>()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "✅ NAPILI: ${getFileName(uri)}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // ✅ ONCREATE — I-INITIALIZE AGAD ANG MGA VIEW
    // ==========================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ DAPAT UNA — BAGO TUMAWAG NG ANUMANG FUNKSYON
        mainScrollView = findViewById(R.id.mainScrollView)
        menuContainer = findViewById(R.id.menuContainer)

        prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
        loadRepoSettings()
        findViewById<TextView>(R.id.tvCurrentVersion)?.text = "📌 Bersyon: $VERSION"
        findViewById<Button>(R.id.btnCheckVersion)?.setOnClickListener { checkVersionFromGitHub() }
        if (!hasGitHubCredentials()) showGitHubSetupDialog()

        // ✅ BUMUO NG MENU — NAKA-INITIALIZE NA ANG MGA VIEW
        buildMainMenu()
    }

    // ==========================================
    // ✅ LAHAT NG FUNKSYON — SIGURADO NANG NAKA-INITIALIZE ANG VIEW
    // ==========================================

    private fun loadRepoSettings() {
        repoOwner = prefs.getString("github_username", "") ?: ""
        repoName = prefs.getString("github_repo", "apk-generator") ?: ""
        savedDefaultPath = prefs.getString("default_destination_path", "") ?: ""
    }

    private fun saveDefaultPath() {
        prefs.edit().putString("default_destination_path", savedDefaultPath).apply()
    }

    private fun hasGitHubCredentials(): Boolean {
        val token = prefs.getString("github_token", "")
        return !token.isNullOrEmpty() && repoOwner.isNotEmpty()
    }

    private fun getGitHubToken(): String = prefs.getString("github_token", "")!!
    private fun updateStatusDisplay() {
        findViewById<TextView>(R.id.tvStatus)?.text = "✅ $repoOwner/$repoName"
    }
    private fun scrollToTop() { mainScrollView.scrollTo(0, 0) }

    private fun showGitHubSetupDialog() {
        val uIn = EditText(this).apply { hint = "GitHub Username"; setText(repoOwner) }
        val rIn = EditText(this).apply { hint = "Repository Name"; setText(repoName) }
        val tIn = EditText(this).apply { hint = "Personal Access Token"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(48,16,48,8)
            addView(uIn); addView(space(12)); addView(rIn); addView(space(12)); addView(tIn)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("🔑 GITHUB KONEKSYON")
            .setView(c)
            .setPositiveButton("✅ I-SAVE AT SCAN") { _, _ ->
                repoOwner = uIn.text.toString().trim()
                repoName = rIn.text.toString().trim()
                val tok = tIn.text.toString().trim()
                if (repoOwner.isNotEmpty() && repoName.isNotEmpty() && tok.isNotEmpty()) {
                    prefs.edit()
                        .putString("github_username", repoOwner)
                        .putString("github_repo", repoName)
                        .putString("github_token", tok)
                        .apply()
                    updateStatusDisplay()
                    scanRepositoryFolders()
                }
            }
            .setNegativeButton("❌ MAMAYA NA", null)
            .setCancelable(false)
            .show()
    }

    private suspend fun scanDirectory(path: String = "", depth: Int = 0, maxDepth: Int = 12) {
        if (depth > maxDepth) return
        try {
            val apiPath = if (path.isEmpty()) "" else "/$path"
            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents$apiPath").openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token ${getGitHubToken()}")
            val ja = JSONArray(conn.inputStream.bufferedReader().readText())
            for (i in 0 until ja.length()) {
                val o = ja.getJSONObject(i)
                if (o.getString("type") == "dir") {
                    classifyAndAddFolder(o.getString("name"), "${o.getString("path")}/")
                    scanDirectory(o.getString("path"), depth + 1, maxDepth)
                }
            }
        } catch (_: Exception) {}
    }

    private fun classifyAndAddFolder(name: String, fp: String) {
        val disp: String
        val t: String

        if (fp.contains("/java/") && detectedJavaRootPath.isEmpty()) {
            val idx = fp.indexOf("/java/")
            detectedJavaRootPath = fp.substring(0, idx + 6)
        }

        if ("/com/" in fp) {
            val parts = fp.split("/").filter { it.isNotEmpty() }
            val comIndex = parts.indexOf("com")
            if (comIndex >= 0 && comIndex < parts.lastIndex) {
                val packagePath = parts.drop(comIndex).joinToString("/") + "/"
                if (!allComPaths.contains(packagePath)) {
                    allComPaths.add(packagePath)
                }
            }
        }

        when {
            fp.contains("mipmap-") || fp.contains("drawable") -> {
                disp = "🖼️ $fp"
                t = "icon"
            }
            fp.contains("/java/") && "/com/" in fp -> {
                disp = "📦 PACKAGE → $fp"
                t = "package"
            }
            fp.contains("/java/") || fp.contains("kotlin/") -> {
                disp = "💻 $fp"
                t = "code"
            }
            fp.contains("layout/") || fp.contains("values/") || fp.contains("xml/") -> {
                disp = "🎨 LAYOUT/RES → $fp"
                t = "layout"
            }
            else -> {
                disp = "📁 $fp"
                t = "other"
            }
        }
        scannedFolders.add(GitHubFolder(fp, t, disp))
    }

    private fun scanRepositoryFolders() {
        if (!hasGitHubCredentials()) return
        CoroutineScope(Dispatchers.IO).launch {
            scannedFolders.clear()
            allComPaths.clear()
            detectedPackagePath = ""
            detectedJavaRootPath = ""

            scanDirectory("", 0, 12)

            scannedFolders.add(0, GitHubFolder(".", "root", "🏠 ROOT"))
            scannedFolders.add(GitHubFolder("docs/", "docs", "📄 docs/"))
            val seen = mutableSetOf<String>()
            scannedFolders.removeAll { !seen.add(it.path) }

            if (allComPaths.isNotEmpty()) {
                detectedPackagePath = allComPaths.maxByOrNull { it.split("/").size } ?: ""
            }

            val manifestPackage = tryReadManifestForPackage()
            if (manifestPackage.isNotEmpty()) {
                detectedPackagePath = manifestPackage
            }

            if (detectedPackagePath.isNotEmpty() && savedDefaultPath.isEmpty()) {
                val fullPath = if(detectedJavaRootPath.isNotEmpty()) detectedJavaRootPath + detectedPackagePath else detectedPackagePath
                savedDefaultPath = fullPath
                saveDefaultPath()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "✅ AUTO-SET DESTINASYON: $savedDefaultPath", Toast.LENGTH_LONG).show()
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                if (currentScreen.startsWith("PATH")) buildPathCategoryMenu()
            }
        }
    }

    private suspend fun tryReadManifestForPackage(): String {
        return try {
            val searchPaths = listOf(
                "app/src/main/AndroidManifest.xml",
                "src/main/AndroidManifest.xml",
                "AndroidManifest.xml"
            )
            var foundPath = ""
            for (p in searchPaths) {
                try {
                    val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$p").openConnection() as HttpURLConnection
                    conn.setRequestProperty("Authorization", "token ${getGitHubToken()}")
                    if(conn.responseCode == 200) { foundPath = p; break }
                } catch (_: Exception) {}
            }
            if (foundPath.isEmpty()) return ""

            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$foundPath").openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "token ${getGitHubToken()}")
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val contentB64 = json.getString("content").replace("\n", "")
            val manifestXml = String(Base64.decode(contentB64, Base64.DEFAULT))

            val parser: XmlPullParser = Xml.newPullParser()
            parser.setInput(ByteArrayInputStream(manifestXml.toByteArray(Charsets.UTF_8)), null)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "manifest") {
                    val pkg = parser.getAttributeValue(null, "package")
                    if (!pkg.isNullOrEmpty()) {
                        return pkg.replace(".", "/") + "/"
                    }
                }
                parser.next()
            }
            ""
        } catch (_: Exception) { "" }
    }

    private fun buildPathCategoryMenu() {
        currentScreen = "PATH_CATEGORY"; scrollToTop()
        menuContainer.removeAllViews()

        addMenuHeader(menuContainer, "📂 DESTINASYON — $repoOwner/$repoName")

        if (savedDefaultPath.isNotEmpty()) {
            addMenuHeader(menuContainer, "💾 KASALUKUYANG DESTINASYON:")
            addMenuHeader(menuContainer, "   $savedDefaultPath")
            addMenuDivider(menuContainer)
        }

        if (detectedPackagePath.isNotEmpty()) {
            addMenuHeader(menuContainer, "📦 BUONG PACKAGE: $detectedPackagePath")
            val fullPath = if(detectedJavaRootPath.isNotEmpty()) detectedJavaRootPath + detectedPackagePath else detectedPackagePath
            addMenuItem(menuContainer, "P", "📦 GAMITIN ANG PACKAGE → $fullPath") {
                savedDefaultPath = fullPath
                saveDefaultPath()
                Toast.makeText(this, "💾 NA-SAVE: $savedDefaultPath", Toast.LENGTH_LONG).show()
                buildPathCategoryMenu()
            }
            addMenuDivider(menuContainer)
        }

        if (scannedFolders.isEmpty()) {
            addMenuHeader(menuContainer, "🔍 SCANNING...")
            CoroutineScope(Dispatchers.Main).launch {
                scanRepositoryFolders()
                delay(1200)
                buildPathCategoryMenu()
            }
            return
        }
        addMenuItem(menuContainer, "1", "🖼️ ICON FOLDER") { showFilteredPaths("icon") }
        addMenuItem(menuContainer, "2", "💻 CODE FOLDER") { showFilteredPaths("code") }
        addMenuItem(menuContainer, "3", "🎨 LAYOUT/RES FOLDER") { showFilteredPaths("layout") }
        addMenuItem(menuContainer, "4", "📂 LAHAT NG FOLDER") { showAllPaths() }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "0", "✏️ I-type ang sariling Path") { showCustomPathInput() }
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildMainMenu() }
    }

    private fun showFilteredPaths(ft: String) {
        currentScreen = "PATH_LIST"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "📂 $ft — BUONG DAAN")
        scannedFolders.filter { it.type=="icon"||it.type=="code"||it.type=="layout"||it.type=="package"||it.type=="root"||it.type=="docs" }
            .forEachIndexed { i, f ->
                addMenuItem(menuContainer, "${i+1}", f.name) {
                    savedDefaultPath = f.path
                    saveDefaultPath()
                    Toast.makeText(this, "💾 NA-SAVE: $savedDefaultPath", Toast.LENGTH_LONG).show()
                    buildPathCategoryMenu()
                }
            }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "0", "✏️ I-type ang sariling Path") { showCustomPathInput() }
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildPathCategoryMenu() }
    }

    private fun showAllPaths() {
        currentScreen = "PATH_ALL"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "📂 LAHAT NG FOLDER")
        scannedFolders.forEachIndexed { i, f ->
            addMenuItem(menuContainer, "${i+1}", f.name) {
                savedDefaultPath = f.path
                saveDefaultPath()
                Toast.makeText(this, "💾 NA-SAVE: $savedDefaultPath", Toast.LENGTH_LONG).show()
                buildPathCategoryMenu()
            }
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "0", "✏️ I-type ang sariling Path") { showCustomPathInput() }
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildPathCategoryMenu() }
    }

    private fun showCustomPathInput() {
        val exampleText = if(detectedPackagePath.isNotEmpty()) {
            "📦 NAKITA: $detectedPackagePath\n👉 I-type ang buong destination path."
        } else {
            "👉 Halimbawa: com/buong/package/hanggang/dulo/"
        }
        val inp = EditText(this).apply {
            hint = "Ilagay ang buong destination path"
            setText(savedDefaultPath)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG DAAN")
            .setMessage(exampleText)
            .setView(inp)
            .setPositiveButton("I-SAVE") { _, _ ->
                var p = inp.text.toString().trim()
                if(p.isNotEmpty()) {
                    if(!p.endsWith("/") && !p.contains(".")) p = "$p/"
                    savedDefaultPath = p
                    saveDefaultPath()
                    Toast.makeText(this, "💾 NA-SAVE: $savedDefaultPath", Toast.LENGTH_LONG).show()
                    buildPathCategoryMenu()
                }
            }
            .show()
    }

    private fun buildIconMenu() {
        currentScreen = "ICON"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "🖼️ ICON → Pumili → Resize → Ipadala")
        addMenuItem(menuContainer, "1", "📂 Pumili ng Larawan") { pickImage() }
        addMenuItem(menuContainer, "2", "📏 I-resize sa 5 sukat") { resizeSelectedIconWithProcess() }
        addMenuItem(menuContainer, "3", "📤 Ipadala sa GitHub") { pushIconToGitHub() }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildMainMenu() }
    }

    private fun pickImage() = pickImageLauncher.launch("image/*")

    private fun resizeSelectedIconWithProcess() {
        if(selectedImageUri == null) {
            Toast.makeText(this, "⚠️ Pumili muna ng larawan!", Toast.LENGTH_LONG).show()
            return
        }
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "📏 NAGSISIMULA ANG RESIZE...")
        val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max=100; progress=0 }
        menuContainer.addView(pb)
        val pt = TextView(this).apply { text="0%"; gravity=Gravity.CENTER }
        menuContainer.addView(pt)
        CoroutineScope(Dispatchers.Main).launch {
            pt.text = "🔍 Binabasa..."
            delay(400)
            val bm = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri!!))
            if(bm == null) { pt.text = "❌ Hindi mabasa"; return@launch }
            var progress = 10; val step = 90 / iconSizes.size
            iconSizes.forEach { (folderName, sizePx) ->
                pt.text = "📏 $folderName — $sizePx×$sizePx"
                pb.progress = progress
                val resized = Bitmap.createScaledBitmap(bm, sizePx, sizePx, true)
                val outDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed-icons").apply { mkdirs() }
                val outFile = File(outDir, "${folderName}_ic_launcher.png")
                FileOutputStream(outFile).use { resized.compress(Bitmap.CompressFormat.PNG, 100, it) }
                progress += step; pb.progress = progress
                delay(350)
            }
            pb.progress = 100; pt.text = "100% ✅ TAPOS NA LAHAT NG SUKAT!"
            addMenuDivider(menuContainer)
            addMenuItem(menuContainer, "3", "📤 Ipadala sa GitHub") { pushIconToGitHub() }
        }
    }

    private fun pushIconToGitHub() {
        if(!hasGitHubCredentials()) { showGitHubSetupDialog(); return }
        if(savedDefaultPath.isEmpty()) {
            Toast.makeText(this, "⚠️ Piliin muna ang Path sa Option 4", Toast.LENGTH_LONG).show()
            return
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA")
            .setMessage("${iconSizes.size} na icon → $savedDefaultPath")
            .setPositiveButton("✅ IPADALA") { _, _ ->
                val tok = getGitHubToken()
                CoroutineScope(Dispatchers.IO).launch {
                    var okCount = 0
                    val outDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed-icons")
                    iconSizes.forEach { (folderName, _) ->
                        val file = File(outDir, "${folderName}_ic_launcher.png")
                        if(!file.exists()) return@forEach
                        try {
                            val b64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                            val finalPath = if(savedDefaultPath.endsWith("/")) "$savedDefaultPath$folderName/ic_launcher.png" else "$savedDefaultPath/$folderName/ic_launcher.png"
                            val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$finalPath").openConnection() as HttpURLConnection
                            conn.setRequestProperty("Authorization", "token $tok")
                            conn.requestMethod = "PUT"
                            conn.setRequestProperty("Content-Type", "application/json")
                            conn.doOutput = true
                            JSONObject().apply {
                                put("message", "📤 ICON $folderName — MartoPush")
                                put("content", b64)
                                conn.outputStream.writer().use { it.write(toString()) }
                            }
                            if(conn.responseCode in 200..201) okCount++
                        } catch (_: Exception) {}
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "✅ $okCount/${iconSizes.size} naipadala!", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("❌ HINDI MUNA", null)
            .show()
    }

    private fun buildCatCodeMenu() {
        currentScreen = "CATCODE"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "📄 OPTION 2 — CAT CODE / FILE PAGPADALA")
        if(savedDefaultPath.isNotEmpty()) {
            addMenuHeader(menuContainer, "💾 BASE DESTINASYON: $savedDefaultPath")
        }
        addMenuItem(menuContainer, "1", "📋 I-PASTE ANG LAMAN O CAT CODE") { showCatCodeInputDialog() }
        addMenuItem(menuContainer, "2", "📂 Piliin ang Default Path") { buildPathCategoryMenu() }
        addMenuItem(menuContainer, "3", "📤 I-PADALA LAHAT") {
            if(parsedCatFiles.isEmpty()) Toast.makeText(this, "⚠️ Mag-paste muna ng code!", Toast.LENGTH_SHORT).show()
            else showDestinationChoiceAndSend()
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildMainMenu() }
    }

    private fun showCatCodeInputDialog() {
        val hintText = if(savedDefaultPath.isNotEmpty()) {
            "💾 Base Path: $savedDefaultPath\nI-paste ang cat code dito..."
        } else {
            "I-paste ang cat code dito..."
        }
        val inp = EditText(this).apply {
            hint = hintText
            minLines = 16; maxLines = 32
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("📋 I-PASTE ANG CAT CODE")
            .setView(inp)
            .setPositiveButton("✅ BASAHIN") { _, _ ->
                val code = inp.text.toString()
                if(code.isBlank()) Toast.makeText(this, "❌ Walang laman!", Toast.LENGTH_SHORT).show()
                else { parseCatCode(code); showCatCodePreview() }
            }
            .setNegativeButton("❌ KANSILA", null)
            .show()
    }

    private fun parseCatCode(code: String) {
        parsedCatFiles.clear()
        val lines = code.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            var rawTarget: String? = null
            var currentContent = StringBuilder()
            var inContent = false
            var endMarker = "EOF"

            if(line.startsWith("cat >")) {
                val parts = line.split("<<".toRegex()).map { it.trim() }
                if(parts.isNotEmpty()) {
                    rawTarget = parts[0].removePrefix("cat >").trim()
                    if(parts.size > 1 && parts[1].isNotEmpty()) {
                        endMarker = parts[1].removePrefix("'").removePrefix("\"").removeSuffix("'").removeSuffix("\"")
                    }
                    inContent = true
                    i++
                }
            } else { i++; continue }

            while(i < lines.size && inContent) {
                val cl = lines[i]
                if(cl == endMarker) { inContent = false; i++; break }
                if(currentContent.isNotEmpty()) currentContent.append("\n")
                currentContent.append(cl)
                i++
            }

            if(!rawTarget.isNullOrBlank() && currentContent.isNotBlank()) {
                val fileName = rawTarget.split("/").last()
                val hasFullPath = "/" in rawTarget
                val fullPathIfExplicit = if(hasFullPath) rawTarget else null

                parsedCatFiles.add(
                    CatFileEntry(
                        originalPath = rawTarget,
                        fileName = fileName,
                        content = currentContent.toString(),
                        hasExplicitPath = hasFullPath,
                        explicitFullPath = fullPathIfExplicit
                    )
                )
            }
        }
    }

    private fun showCatCodePreview() {
        scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "✅ NABASA — ${parsedCatFiles.size} na file")

        parsedCatFiles.forEachIndexed { i, e ->
            val pathDisplay = if(e.hasExplicitPath) {
                "📂 ${e.explicitFullPath}  ✅(May sariling daan)"
            } else {
                "📂 $savedDefaultPath${e.fileName}  ⏳(Pangalan lang + Base Path)"
            }
            addMenuItem(menuContainer, "${i+1}", "📄 ${e.fileName}") {
                android.app.AlertDialog.Builder(this)
                    .setTitle(e.fileName)
                    .setMessage(pathDisplay + "\n\n" + e.content.take(600))
                    .setPositiveButton("OK", null).show()
            }
            addMenuHeader(menuContainer, pathDisplay)
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "3", "📤 IPADALA — Pumili ng Destinasyon") { showDestinationChoiceAndSend() }
        addMenuItem(menuContainer, "b", "⬅️ Bumalik") { buildCatCodeMenu() }
    }

    private fun showDestinationChoiceAndSend() {
        if(parsedCatFiles.isEmpty()) {
            Toast.makeText(this, "⚠️ Wala pang file!", Toast.LENGTH_SHORT).show()
            return
        }
        if(savedDefaultPath.isEmpty()) {
            Toast.makeText(this, "❌ Piliin muna ang DESTINASYON sa Option 4!", Toast.LENGTH_LONG).show()
            buildPathCategoryMenu()
            return
        }

        val withOwnPath = parsedCatFiles.filter { it.hasExplicitPath }
        val needBasePath = parsedCatFiles.filter { !it.hasExplicitPath }

        val message = buildString {
            if(withOwnPath.isNotEmpty()) {
                append("✅ ${withOwnPath.size} FILE — MAY SARILING DAAN SA CODE:\n")
                withOwnPath.forEach { append("   📄 ${it.fileName} → ${it.explicitFullPath}\n") }
                append("\n")
            }
            if(needBasePath.isNotEmpty()) {
                append("📂 ${needBasePath.size} FILE — PANGALAN LANG — GAGAMITIN ANG BASE PATH:\n")
                needBasePath.forEach { append("   📄 ${it.fileName} → $savedDefaultPath${it.fileName}\n") }
                append("\n")
            }
            append("────────────────────────────────────\n")
            append("[1] ✅ GAMITIN ANG TINUTUKOY\n")
            append("   → May daan = sundin; walang daan = gamitin Option 4\n")
            append("[2] 🧪 PAGSUSUBOK LANG → docs/testing/\n")
            append("   → LAHAT papunta sa docs/testing/\n")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 SAAN MO GUSTO IPADALA?")
            .setMessage(message)
            .setPositiveButton("1 — GAMITIN ANG TINUTUKOY") { _, _ ->
                sendFilesRespectingPaths()
            }
            .setNeutralButton("2 — docs/testing/") { _, _ ->
                sendFilesForceTo("docs/testing/")
            }
            .setNegativeButton("❌ KANSILA", null)
            .show()
    }

    private fun sendFilesRespectingPaths() {
        val tok = getGitHubToken()
        val list = ArrayList(parsedCatFiles)
        Toast.makeText(this, "📤 Pinapadala...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            var ok = 0
            list.forEachIndexed { i, e ->
                try {
                    val finalPath = if(e.hasExplicitPath) {
                        e.explicitFullPath!!
                    } else {
                        val base = if(savedDefaultPath.endsWith("/")) savedDefaultPath else "$savedDefaultPath/"
                        "$base${e.fileName}"
                    }

                    val b64 = Base64.encodeToString(e.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$finalPath"
                    val conn = URL(apiUrl).openConnection() as HttpURLConnection
                    conn.setRequestProperty("Authorization", "token $tok")
                    conn.requestMethod = "PUT"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    JSONObject().apply {
                        put("message", "📤 ${e.fileName} — MartoPush")
                        put("content", b64)
                        conn.outputStream.writer().use { it.write(toString()) }
                    }

                    if(conn.responseCode in 200..201) {
                        ok++
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(this@MainActivity,
                                "✅ [${i+1}/${list.size}] ${e.fileName}\n📂 $finalPath",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch(ex: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "❌ ${e.fileName}: ${ex.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@MainActivity, "✅ TAPOS NA! $ok/${list.size} naipadala", Toast.LENGTH_LONG).show()
                parsedCatFiles.clear()
            }
        }
    }

    private fun sendFilesForceTo(prefix: String) {
        val tok = getGitHubToken()
        val list = ArrayList(parsedCatFiles)
        Toast.makeText(this, "🧪 Pagsusubok — lahat papunta sa $prefix...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            var ok = 0
            list.forEachIndexed { i, e ->
                try {
                    val finalPath = "$prefix${e.fileName}"
                    val b64 = Base64.encodeToString(e.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents/$finalPath"
                    val conn = URL(apiUrl).openConnection() as HttpURLConnection
                    conn.setRequestProperty("Authorization", "token $tok")
                    conn.requestMethod = "PUT"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    JSONObject().apply {
                        put("message", "🧪 TESTING ${e.fileName} — MartoPush")
                        put("content", b64)
                        conn.outputStream.writer().use { it.write(toString()) }
                    }

                    if(conn.responseCode in 200..201) ok++
                } catch(_: Exception) {}
            }
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@MainActivity, "✅ TESTING TAPOS NA! $ok/${list.size}", Toast.LENGTH_LONG).show()
                parsedCatFiles.clear()
            }
        }
    }

    private fun checkVersionFromGitHub() {
        findViewById<TextView>(R.id.tvStatus)?.text = "🔍 Tinitignan..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").openConnection() as HttpURLConnection
                val ver = JSONObject(conn.inputStream.bufferedReader().readText())
                    .optString("tag_name", VERSION).removePrefix("v")
                CoroutineScope(Dispatchers.Main).launch {
                    findViewById<TextView>(R.id.tvStatus)?.text =
                        if(ver == VERSION.removePrefix("v")) "✅ Pinakabago: v$ver" else "⚠️ May Bago: v$ver"
                }
            } catch(_: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    findViewById<TextView>(R.id.tvStatus)?.text = "⚠️ Hindi matignan"
                }
            }
        }
    }

    private fun buildMainMenu() {
        currentScreen = "MAIN"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer, "========================================")
        addMenuHeader(menuContainer, "       📤  M A R T O P U S H  $VERSION")
        addMenuHeader(menuContainer, "    Developed by MartoDosko © 2026")
        addMenuHeader(menuContainer, "========================================")
        if(savedDefaultPath.isNotEmpty()) {
            addMenuHeader(menuContainer, "💾 BASE DESTINASYON: $savedDefaultPath")
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "1", "🖼️ ICON — Pumili, Resize, Ipadala") { buildIconMenu() }
        addMenuItem(menuContainer, "2", "📄 Ipadala ang Cat Code / File") { buildCatCodeMenu() }
        addMenuItem(menuContainer, "3", "📤 Direktang Pagpadala") {
            if(parsedCatFiles.isNotEmpty()) showDestinationChoiceAndSend()
            else Toast.makeText(this, "⚠️ Mag-paste muna ng code sa Option 2 → 1", Toast.LENGTH_SHORT).show()
        }
        addMenuItem(menuContainer, "4", "📂 Piliin / I-set ang Destination Path") { buildPathCategoryMenu() }
        addMenuItem(menuContainer, "5", "🔄 Tumatsek ng Update at Bersyon") { checkVersionFromGitHub() }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer, "0", "↩️ Lumabas") { finish() }
    }

    private fun getFileName(u: Uri): String {
        contentResolver.query(u, null, null, null, null)?.use {
            if(it.moveToFirst()) {
                val i = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(i >= 0) return it.getString(i)
            }
        }
        return "larawan.png"
    }

    private fun space(dp: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp) }
    private fun addMenuHeader(c: LinearLayout, t: String) {
        c.addView(TextView(this).apply {
            text = t; gravity = Gravity.CENTER; setTextColor(0xFF1565C0.toInt())
            setPadding(0, 8, 0, 8); setTypeface(null, android.graphics.Typeface.BOLD)
        })
    }
    private fun addMenuItem(c: LinearLayout, n: String, l: String, a: () -> Unit) {
        c.addView(Button(this).apply {
            text = "[$n]   $l"
            setOnClickListener { a() }
            setPadding(32, 16, 32, 16)
        })
    }
    private fun addMenuDivider(c: LinearLayout) {
        c.addView(View(this).apply {
            setBackgroundColor(0xFFE0E0E0.toInt())
            layoutParams = LinearLayout.LayoutParams(-1, 1)
        })
    }
}
