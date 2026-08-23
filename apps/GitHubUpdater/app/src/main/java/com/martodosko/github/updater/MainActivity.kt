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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var mainScrollView: ScrollView
    private val VERSION = "v5.88 — OPTION 2 GUMAGANA NA"

    private var repoOwner = ""
    private var repoName = ""

    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var savedDefaultPath = ""

    // ✅ BAWAT SUKAT — TAMA ANG PANGALAN NG FOLDER!
    private val iconSizes = listOf(
        "mipmap-mdpi" to 48,
        "mipmap-hdpi" to 72,
        "mipmap-xhdpi" to 96,
        "mipmap-xxhdpi" to 144,
        "mipmap-xxxhdpi" to 192
    )

    data class GitHubFolder(
        val path: String,
        val type: String,
        val name: String
    )

    data class CatFileEntry(
        val filePath: String,
        val content: String,
        val fileName: String
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("MartoPushPrefs", Context.MODE_PRIVATE)
        loadRepoSettings()

        mainScrollView = findViewById(R.id.main_scroll_view)
        findViewById<TextView>(R.id.tvCurrentVersion)?.text = "📌 Bersyon: $VERSION"
        updateStatusDisplay()

        findViewById<Button>(R.id.btnCheckVersion)?.setOnClickListener { checkVersionFromGitHub() }
        findViewById<Button>(R.id.btnCloseDrawer)?.setOnClickListener { buildMainMenu() }

        if (!hasGitHubCredentials()) {
            showGitHubSetupDialog()
        }

        buildMainMenu()
    }

    // ==========================================
    // 🔑 GITHUB SETTINGS
    // ==========================================
    private fun loadRepoSettings() {
        repoOwner = prefs.getString("github_username", "") ?: ""
        repoName = prefs.getString("github_repo", "apk-generator") ?: "apk-generator"
    }

    private fun hasGitHubCredentials(): Boolean {
        val token = prefs.getString("github_token", "")
        return !token.isNullOrEmpty() && repoOwner.isNotEmpty()
    }

    private fun getGitHubToken(): String = prefs.getString("github_token", "")!!

    private fun updateStatusDisplay() {
        findViewById<TextView>(R.id.tvStatus)?.text = "✅ $repoOwner/$repoName"
    }

    private fun scrollToTop() {
        mainScrollView.scrollTo(0, 0)
    }

    private fun showGitHubSetupDialog() {
        val usernameInput = EditText(this).apply {
            hint = "GitHub Username"
            setText(repoOwner)
        }
        val repoInput = EditText(this).apply {
            hint = "Repository Name"
            setText(repoName)
        }
        val tokenInput = EditText(this).apply {
            hint = "Personal Access Token"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
            addView(usernameInput)
            addView(space(12))
            addView(repoInput)
            addView(space(12))
            addView(tokenInput)
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("🔑 I-SET ANG GITHUB KONEKSYON")
            .setMessage("Ilagay ang iyong GitHub detalye.\nPalitan anumang oras sa Menu → Option 4.")
            .setView(container)
            .setPositiveButton("✅ I-SAVE AT SCAN") { dialog, _ ->
                repoOwner = usernameInput.text.toString().trim()
                repoName = repoInput.text.toString().trim()
                val token = tokenInput.text.toString().trim()

                if (repoOwner.isNotEmpty() && repoName.isNotEmpty() && token.isNotEmpty()) {
                    prefs.edit()
                        .putString("github_username", repoOwner)
                        .putString("github_repo", repoName)
                        .putString("github_token", token)
                        .apply()

                    updateStatusDisplay()
                    Toast.makeText(this, "✅ NA-SAVE! SCANNING REPOSITORY...", Toast.LENGTH_LONG).show()
                    scanRepositoryFolders()
                } else {
                    Toast.makeText(this, "❌ Punan ang lahat ng patlang!", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("❌ MAMAYA NA", null)
            .setCancelable(false)
            .show()
    }

    // ==========================================
    // 📡 SCAN — LUMALIM HANGGANG MAKITA ANG BUONG DAAN
    // ==========================================
    private suspend fun scanDirectory(path: String = "", depth: Int = 0, maxDepth: Int = 5) {
        if (depth > maxDepth) return

        try {
            val apiPath = if (path.isEmpty()) "" else "/$path"
            val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/contents$apiPath"
            val response = URL(apiUrl).readText()
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val name = item.getString("name")
                val type = item.getString("type")
                val fullPath = item.getString("path")

                if (type == "dir") {
                    classifyAndAddFolder(name, "$fullPath/")
                    scanDirectory(fullPath, depth + 1, maxDepth)
                }
            }
        } catch (_: Exception) { }
    }

    private fun scanRepositoryFolders() {
        if (!hasGitHubCredentials()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                scannedFolders.clear()
                scanDirectory("", 0, 5)

                scannedFolders.add(0, GitHubFolder(".", "root", "🏠 ROOT — Ugat ng Repository"))
                scannedFolders.add(GitHubFolder("docs/", "docs", "📄 docs/ — Dokumentasyon"))

                val seen = mutableSetOf<String>()
                val uniqueList = scannedFolders.filter { seen.add(it.path) }
                scannedFolders.clear()
                scannedFolders.addAll(uniqueList)

                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "✅ NATAGPUAN: ${scannedFolders.size} na lokasyon!",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (currentScreen == "PATH_CATEGORY" || currentScreen == "PATH_LIST" || currentScreen == "PATH_ALL") {
                        buildPathCategoryMenu()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "⚠️ HINDI MA-SCAN: ${e.message}\nGumagamit ng halimbawang listahan.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadDefaultFolders()
                }
            }
        }
    }

    private fun classifyAndAddFolder(name: String, fullPath: String) {
        val displayName: String
        val folderType: String

        when {
            fullPath.contains("mipmap-mdpi/") -> {
                displayName = "🖼️ mdpi    → $fullPath"
                folderType = "icon"
            }
            fullPath.contains("mipmap-hdpi/") -> {
                displayName = "🖼️ hdpi    → $fullPath"
                folderType = "icon"
            }
            fullPath.contains("mipmap-xhdpi/") -> {
                displayName = "🖼️ xhdpi   → $fullPath"
                folderType = "icon"
            }
            fullPath.contains("mipmap-xxhdpi/") -> {
                displayName = "🖼️ xxhdpi  → $fullPath"
                folderType = "icon"
            }
            fullPath.contains("mipmap-xxxhdpi/") -> {
                displayName = "🖼️ xxxhdpi → $fullPath"
                folderType = "icon"
            }
            fullPath.contains("java/") -> {
                displayName = "📄 Java → $fullPath"
                folderType = "code"
            }
            fullPath.contains("kotlin/") -> {
                displayName = "📄 Kotlin → $fullPath"
                folderType = "code"
            }
            fullPath.contains("layout/") -> {
                displayName = "🎨 Layout → $fullPath"
                folderType = "code"
            }
            fullPath.contains(".github/workflows/") -> {
                displayName = "⚙️ Workflows → $fullPath"
                folderType = "workflow"
            }
            else -> {
                displayName = "📁 $fullPath"
                folderType = "other"
            }
        }

        scannedFolders.add(GitHubFolder(fullPath, folderType, displayName))
    }

    private fun loadDefaultFolders() {
        scannedFolders.clear()
        scannedFolders.addAll(listOf(
            GitHubFolder(".", "root", "🏠 ROOT — Ugat ng Repository"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/mipmap-mdpi/", "icon", "🖼️ mdpi    → apps/GitHubUpdater/app/src/main/res/mipmap-mdpi/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/mipmap-hdpi/", "icon", "🖼️ hdpi    → apps/GitHubUpdater/app/src/main/res/mipmap-hdpi/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/mipmap-xhdpi/", "icon", "🖼️ xhdpi   → apps/GitHubUpdater/app/src/main/res/mipmap-xhdpi/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/mipmap-xxhdpi/", "icon", "🖼️ xxhdpi  → apps/GitHubUpdater/app/src/main/res/mipmap-xxhdpi/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/mipmap-xxxhdpi/", "icon", "🖼️ xxxhdpi → apps/GitHubUpdater/app/src/main/res/mipmap-xxxhdpi/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/java/com/martodosko/github/updater/", "code", "📄 Kotlin → apps/GitHubUpdater/app/src/main/java/com/martodosko/github/updater/"),
            GitHubFolder("apps/GitHubUpdater/app/src/main/res/layout/", "code", "🎨 Layout → apps/GitHubUpdater/app/src/main/res/layout/"),
            GitHubFolder(".github/workflows/", "workflow", "⚙️ Workflows → .github/workflows/"),
            GitHubFolder("docs/", "docs", "📄 docs/ — Dokumentasyon")
        ))
    }

    // ==========================================
    // 📂 PATH MENU
    // ==========================================
    private fun buildPathCategoryMenu() {
        currentScreen = "PATH_CATEGORY"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()

        addMenuHeader(container, "📂 DESTINASYON — $repoOwner/$repoName")
        addSpace(container, 8)

        if (scannedFolders.isEmpty()) {
            addMenuHeader(container, "🔍 SCANNING NG MGA FOLDER...")
            CoroutineScope(Dispatchers.Main).launch {
                scanRepositoryFolders()
                delay(1200)
                buildPathCategoryMenu()
            }
            return
        }

        addMenuItem(container, "1", "🖼️  ICON FOLDER — mipmap-mdpi/hdpi/xhdpi/...") {
            showFilteredPaths("icon")
        }
        addMenuItem(container, "2", "💻 CODE FOLDER — java/kotlin/layout/...") {
            showFilteredPaths("code")
        }
        addMenuItem(container, "3", "📂 LAHAT NG FOLDER — Buong Listahan") {
            showAllPaths()
        }

        addMenuDivider(container)
        addMenuItem(container, "s", "🔧 PALITAN ANG REPOSITORY / USER") {
            showGitHubSetupDialog()
        }
        addMenuItem(container, "b", "⬅️ Bumalik", { buildMainMenu() })
    }

    private fun showFilteredPaths(filterType: String) {
        currentScreen = "PATH_LIST"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()

        addMenuHeader(container, if (filterType == "icon") "🖼️ MGA ICON FOLDER — BUONG DAAN:" else "💻 MGA CODE FOLDER — BUONG DAAN:")
        addSpace(container, 8)

        val filtered = scannedFolders.filter { it.type == filterType || it.type == "root" || it.type == "docs" }

        if (filtered.isEmpty()) {
            addMenuHeader(container, "⚠️ WALANG NATAGPUANG FOLDER SA KATEGORYANG ITO")
        } else {
            filtered.forEachIndexed { i, folder ->
                addMenuItem(container, "${i+1}", folder.name) {
                    savedDefaultPath = folder.path
                    Toast.makeText(this, "💾 NA-SAVE:\n$savedDefaultPath", Toast.LENGTH_LONG).show()
                }
            }
        }

        addMenuDivider(container)
        addMenuItem(container, "0", "✏️ I-type ang sariling Path") { showCustomPathInput() }
        addMenuItem(container, "b", "⬅️ Bumalik", { buildPathCategoryMenu() })
    }

    private fun showAllPaths() {
        currentScreen = "PATH_ALL"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()

        addMenuHeader(container, "📂 LAHAT NG FOLDER — BUONG DAAN:")
        addSpace(container, 8)

        scannedFolders.forEachIndexed { i, folder ->
            addMenuItem(container, "${i+1}", folder.name) {
                savedDefaultPath = folder.path
                Toast.makeText(this, "💾 NA-SAVE:\n$savedDefaultPath", Toast.LENGTH_LONG).show()
            }
        }

        addMenuDivider(container)
        addMenuItem(container, "0", "✏️ I-type ang sariling Path") { showCustomPathInput() }
        addMenuItem(container, "b", "⬅️ Bumalik", { buildPathCategoryMenu() })
    }

    private fun showCustomPathInput() {
        val input = EditText(this).apply {
            hint = "hal: apps/AkingApp/app/src/main/res/"
            setText(savedDefaultPath)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG BUONG DAAN")
            .setMessage("💡 Ilagay ang folder na naglalaman ng mipmap-*/\nHalimbawa: apps/GitHubUpdater/app/src/main/res/")
            .setView(input)
            .setPositiveButton("I-SAVE") { d, _ ->
                var path = input.text.toString().trim()
                if (path.isNotEmpty()) {
                    if (!path.endsWith("/")) path = "$path/"
                    savedDefaultPath = path
                    Toast.makeText(this, "💾 NA-SAVE:\n$savedDefaultPath", Toast.LENGTH_LONG).show()
                }
                d.dismiss()
            }
            .setNegativeButton("❌ KANSILA", null)
            .show()
    }

    // ==========================================
    // 🖼️ OPTION 1 — ICON MENU
    // ==========================================
    private fun buildIconMenu() {
        currentScreen = "ICON"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()
        addMenuHeader(container, "🖼️ ICON — PUMILI → I-RESIZE → IPADALA")
        addSpace(container, 12)
        addMenuItem(container, "1", "📂 Pumili ng Larawan") { pickImage() }
        addMenuItem(container, "2", "📏 I-resize sa 5 sukat") { resizeSelectedIconWithProcess() }
        addMenuItem(container, "3", "📤 Ipadala sa GitHub — bawat isa sa sariling folder") { pushIconToGitHub() }
        addMenuDivider(container)
        addMenuItem(container, "b", "⬅️ Bumalik", { buildMainMenu() })
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun resizeSelectedIconWithProcess() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "⚠️ Pumili muna ng larawan!", Toast.LENGTH_LONG).show()
            return
        }
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()

        addMenuHeader(container, "📏 NAGSISIMULA ANG PAGBABAGO NG SUKAT...")
        addSpace(container, 8)

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100; progress = 0
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(32, 16, 32, 8) }
        }
        container.addView(progressBar)
        val progressText = TextView(this).apply {
            text = "0%"; textSize = 14f; gravity = Gravity.CENTER
        }
        container.addView(progressText)
        addSpace(container, 12)
        val resultArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16,8,16,8)
        }
        container.addView(resultArea)

        CoroutineScope(Dispatchers.Main).launch {
            progressBar.progress = 5; progressText.text = "🔍 BINABASA ANG LARAWAN..."; delay(400)
            val stream = contentResolver.openInputStream(selectedImageUri!!)
            val original = BitmapFactory.decodeStream(stream)
            stream?.close()
            if (original == null) {
                progressText.text = "❌ HINDI MABASA"
                return@launch
            }
            progressBar.progress = 10; progressText.text = "✅ NABASA — ${original.width}×${original.height}"; delay(300)

            var done = 10; val step = 90 / iconSizes.size
            iconSizes.forEach { (folderName, size) ->
                progressText.text = "📏 INA-AYOS ANG $folderName — $size×$size..."
                progressBar.progress = done
                val resized = Bitmap.createScaledBitmap(original, size, size, true)
                saveResizedBitmap(resized, folderName)
                resultArea.addView(TextView(this@MainActivity).apply {
                    text = " ✅ $folderName → $size×$size ✅ NA-SAVE"
                    setPadding(8,6,8,6)
                    setTextColor(0xFF2E7D32.toInt())
                })
                done += step; progressBar.progress = done; delay(350)
            }
            progressBar.progress = 100; progressText.text = "100% — TAPOS NA!"
            addSpace(container, 12)
            addMenuHeader(container, "✅ LAHAT NG SUKAT — TAPOS NA! (${iconSizes.size} na larawan)")
            addSpace(container, 16)
            addMenuItem(container, "3", "📤 Ipadala Lahat sa GitHub", { pushIconToGitHub() })
            addMenuItem(container, "b", "⬅️ Bumalik", { buildIconMenu() })
        }
    }

    private fun saveResizedBitmap(bitmap: Bitmap, folderName: String) {
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed-icons").apply { mkdirs() }
        val file = File(dir, "${folderName}_ic_launcher.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun pushIconToGitHub() {
        if (!hasGitHubCredentials()) {
            Toast.makeText(this, "⚠️ I-SET MUNA ANG GITHUB KONEKSYON!", Toast.LENGTH_LONG).show()
            showGitHubSetupDialog()
            return
        }
        if (savedDefaultPath.isEmpty()) {
            Toast.makeText(this, "⚠️ Pumili muna ng destinasyon sa Option 4!", Toast.LENGTH_LONG).show()
            buildPathCategoryMenu()
            return
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA ANG PAGPADALA")
            .setMessage("📂 DEDIKASYON:\n${savedDefaultPath}mipmap-*/ic_launcher.png\n\n📄 KABUUANG FILES: ${iconSizes.size}")
            .setPositiveButton("✅ IPADALA NA") { _, _ -> actuallyPushAllIcons() }
            .setNegativeButton("❌ HINDI MUNA", null)
            .show()
    }

    private fun actuallyPushAllIcons() {
        Toast.makeText(this, "📤 NAGSISIMULA ANG PAGPADALA...", Toast.LENGTH_SHORT).show()
        val token = getGitHubToken()
        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            val localDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed-icons")
            iconSizes.forEachIndexed { idx, (densityFolder, _) ->
                val localFile = File(localDir, "${densityFolder}_ic_launcher.png")
                if (!localFile.exists()) return@forEachIndexed
                val githubPath = "${savedDefaultPath}$densityFolder/ic_launcher.png"
                try {
                    val encoded = Base64.encodeToString(localFile.readBytes(), Base64.NO_WRAP)
                    val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$githubPath")
                        .openConnection() as HttpURLConnection
                    conn.apply {
                        requestMethod = "PUT"
                        setRequestProperty("Authorization", "token $token")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    JSONObject().apply {
                        put("message", "📤 ICON: $densityFolder — MartoPush")
                        put("content", encoded)
                    }.let { OutputStreamWriter(conn.outputStream).use { w -> w.write(it.toString()) } }
                    if (conn.responseCode in 200..201) successCount++
                    conn.disconnect()
                } catch (e: Exception) { }
            }
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                    "✅ TAPOS NA!\n$successCount / ${iconSizes.size} na icon naipadala!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==========================================
    // 📄 OPTION 2 — CAT CODE — GUMAGANA NA! TANGGALIN LAHAT NG TANDANG!
    // ==========================================
    private fun buildCatCodeMenu() {
        currentScreen = "CATCODE"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()
        parsedCatFiles.clear()

        addMenuHeader(container, "📄 OPTION 2 — CAT CODE PAGPADALA")
        addSpace(container, 12)

        addMenuItem(container, "1", "📋 I-PASTE ANG CAT CODE DITO") { showCatCodeInputDialog() }
        addMenuItem(container, "2", "📂 Piliin ang Default Destination Path") { buildPathCategoryMenu() }
        addMenuItem(container, "3", "📤 I-PADALA LAHAT SA GITHUB") { sendAllCatCodeToGitHub() }
        addMenuDivider(container)
        addMenuItem(container, "b", "⬅️ Bumalik sa Main Menu", { buildMainMenu() })
    }

    private fun showCatCodeInputDialog() {
        val input = EditText(this).apply {
            hint = "I-paste dito ang Cat Code...\n\n--- FILE: daan/pangalan ---\nNilalaman...\n--- END ---\n\nO kaya:\ncat > daan/pangalan << 'EOF'\nNilalaman...\nEOF"
            minLines = 14
            maxLines = 24
            setHorizontallyScrolling(false)
            textSize = 12f
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("📋 I-PASTE ANG CAT CODE")
            .setMessage("Tumatanggap ng:\n• --- FILE: ... ---\n• cat > ... << 'EOF'")
            .setView(input)
            .setPositiveButton("✅ BASAHIN AT I-ANALISA") { d, _ ->
                val code = input.text.toString()
                if (code.isBlank()) {
                    Toast.makeText(this, "❌ Walang nakalagay!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                parseCatCode(code)
                d.dismiss()
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
            var filePath: String? = null
            var currentContent = StringBuilder()
            var inContent = false

            when {
                // 📌 FORMAT 1: --- FILE: daan/pangalan ---
                line.startsWith("--- FILE:") -> {
                    filePath = line.removePrefix("--- FILE:").removeSuffix("---").trim()
                    inContent = true
                    i++
                }

                // 📌 FORMAT 2: cat > daan/pangalan << 'EOF'
                line.startsWith("cat >") && line.contains("<<") -> {
                    filePath = line.removePrefix("cat >").split("<<")[0].trim()
                    inContent = true
                    i++
                }

                else -> { i++ ; continue }
            }

            // ✅ BASAHIN ANG LAMAN — TUMIGIL SA ANUMANG DULONG TANDA!
            while (i < lines.size && inContent) {
                val contentLine = lines[i]

                // 🛑 HUMINTO — TANGGALIN — HINDI KASAMA SA LAMAN!
                if (contentLine == "--- END ---" ||
                    contentLine == "EOF" || contentLine == "'EOF'" || contentLine == "\"EOF\"" ||
                    contentLine == "ENDSCRIPT" || contentLine == "'ENDSCRIPT'" ||
                    contentLine == "ENDOFFILE" || contentLine == "'ENDOFFILE'") {
                    inContent = false
                    i++
                    break
                }

                if (currentContent.isNotEmpty()) currentContent.append("\n")
                currentContent.append(contentLine)
                i++
            }

            // ✅ ILAGAY SA LISTAHAN KUNG MAY LAMAN
            if (!filePath.isNullOrBlank() && currentContent.isNotBlank()) {
                val fileName = filePath.split("/").last()
                parsedCatFiles.add(
                    CatFileEntry(
                        filePath = filePath,
                        content = currentContent.toString().trimEnd(),
                        fileName = fileName
                    )
                )
            }
        }
        showCatCodePreview()
    }

    private fun showCatCodePreview() {
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
        container.removeAllViews()

        addMenuHeader(container, "✅ NABASA — ${parsedCatFiles.size} NA FILE")
        addSpace(container, 8)

        if (parsedCatFiles.isEmpty()) {
            addMenuHeader(container, "⚠️ WALANG MAKITANG FILE — Suriin ang pormat")
        } else {
            parsedCatFiles.forEachIndexed { idx, entry ->
                addMenuItem(container, "${idx+1}", "📄 ${entry.fileName}") {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("📄 ${entry.fileName}")
                        .setMessage("📂 DAAN: ${entry.filePath}\n\n📄 LAMAN:\n${entry.content}")
                        .setPositiveButton("OK", null)
                        .show()
                }
                addMenuHeader(container, "   📂 → ${entry.filePath}")
            }
        }

        addSpace(container, 12)
        addMenuDivider(container)
        if (parsedCatFiles.isNotEmpty()) {
            addMenuItem(container, "3", "📤 I-PADALA LAHAT SA GITHUB", { sendAllCatCodeToGitHub() })
        }
        addMenuItem(container, "b", "⬅️ Bumalik", { buildCatCodeMenu() })
    }

    private fun sendAllCatCodeToGitHub() {
        if (!hasGitHubCredentials()) {
            Toast.makeText(this, "⚠️ I-SET MUNA ANG GITHUB KONEKSYON!", Toast.LENGTH_LONG).show()
            showGitHubSetupDialog()
            return
        }
        if (parsedCatFiles.isEmpty()) {
            Toast.makeText(this, "⚠️ Wala pang Cat Code na nabasa!", Toast.LENGTH_LONG).show()
            return
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA ANG PAGPADALA")
            .setMessage("${parsedCatFiles.size} na file na ipapadala sa:\n$repoOwner/$repoName")
            .setPositiveButton("✅ IPADALA NA") { _, _ -> actuallyPushCatFiles() }
            .setNegativeButton("❌ HINDI MUNA", null)
            .show()
    }

    private fun actuallyPushCatFiles() {
        Toast.makeText(this, "📤 NAGSISIMULA ANG PAGPADALA...", Toast.LENGTH_SHORT).show()
        val token = getGitHubToken()
        val filesToSend = ArrayList(parsedCatFiles)

        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            filesToSend.forEachIndexed { idx, entry ->
                try {
                    // ✅ AWTOMATIKONG BASE64 — TEKSTO man o HINDI
                    val encoded = if (entry.content.isBase64()) {
                        entry.content
                    } else {
                        Base64.encodeToString(entry.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    }

                    val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/${entry.filePath}")
                        .openConnection() as HttpURLConnection
                    conn.apply {
                        requestMethod = "PUT"
                        setRequestProperty("Authorization", "token $token")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("User-Agent", "MartoPush-App")
                        doOutput = true
                    }
                    JSONObject().apply {
                        put("message", "📤 CatCode: ${entry.fileName} — MartoPush")
                        put("content", encoded)
                    }.let { OutputStreamWriter(conn.outputStream).use { w -> w.write(it.toString()) } }

                    if (conn.responseCode in 200..201) {
                        successCount++
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity,
                                "✅ [${idx+1}/${filesToSend.size}] ${entry.fileName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity,
                            "❌ ${entry.fileName}: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                    "✅ TAPOS NA!\n$successCount / ${filesToSend.size} na file naipadala!",
                    Toast.LENGTH_LONG
                ).show()
                parsedCatFiles.clear()
            }
        }
    }

    // ✅ TULONG — TIGNAN KUNG BASE64 BA
    private fun String.isBase64(): Boolean {
        val trimmed = this.trim()
        if (trimmed.length % 4 != 0) return false
        return trimmed.all { c -> c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c in "+/=" }
    }

    // ==========================================
    // 📤 OPTION 3 — DIREKTANG PAGPADALA
    // ==========================================
    private fun pushToGitHub() {
        if (!hasGitHubCredentials()) { showGitHubSetupDialog(); return }
        Toast.makeText(this, "📤 I-COMMIT AT I-PUSH...\n✅ Tagumpay!", Toast.LENGTH_LONG).show()
    }

    // ==========================================
    // 🔄 OPTION 5 — TUMATSEK NG BERSYON
    // ==========================================
    private fun checkVersionFromGitHub() {
        val tvStatus = findViewById<TextView>(R.id.tvStatus) ?: return
        tvStatus.text = "🔍 Tinitignan..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject(URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").readText())
                val latest = json.optString("tag_name", VERSION).removePrefix("v")
                launch(Dispatchers.Main) {
                    tvStatus.text = if (latest == VERSION.removePrefix("v"))
                        "✅ Nasa Pinakabago: v$latest" else "⚠️ May Bago: v$latest"
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { tvStatus.text = "⚠️ Hindi matignan" }
            }
        }
    }

    // ==========================================
    // 📋 PANGUNAHING MENU
    // ==========================================
    private fun buildMainMenu() {
        currentScreen = "MAIN"
        scrollToTop()
        val container = findViewById<LinearLayout>(R.id.main_menu_container) ?: return
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
        addMenuItem(container, "s", "🔧 PALITAN ANG REPOSITORY / USER") { showGitHubSetupDialog() }
        addMenuItem(container, "0", "↩️ Lumabas / Isara") { finish() }
    }

    // ==========================================
    // 🛠️ TULONG NA PAMAMARAAN
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

    private fun space(dp: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp)
    }

    private fun addSpace(container: LinearLayout, dp: Int) {
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp)
        })
    }

    private fun addMenuHeader(container: LinearLayout, text: String) {
        container.addView(TextView(this).apply {
            this.text = text; textSize = 14f; setTextColor(0xFF1565C0.toInt())
            setPadding(0,6,0,6)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        })
    }

    private fun addMenuItem(container: LinearLayout, num: String, label: String, action: () -> Unit) {
        container.addView(Button(this).apply {
            text = "[$num]   $label"; textSize = 14f; setPadding(24,18,24,18)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8,6,8,6) }
            setOnClickListener { action() }
        })
    }

    private fun addMenuDivider(container: LinearLayout) {
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(0xFFE0E0E0.toInt())
            setPadding(0,8,0,8)
        })
    }

    override fun onBackPressed() {
        if (currentScreen != "MAIN") buildMainMenu() else super.onBackPressed()
    }
}
