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
import androidx.activity.OnBackPressedCallback
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
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ==========================================
// 📤 MARTOPUSH — GitHub File & Icon Uploader
// ✅ VERSION: v5.99.1 — 🛠️ INA-AYOS: Direktang napupunta sa napiling Path!
// 🔧 Ayos: Kapag pinili mo /docs/ → DOON LANG PUMUNTA — walang dagdag na sub-folder!
// Developed by MartoDosko © 2026
// ==========================================

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var mainScrollView: ScrollView
    private lateinit var menuContainer: LinearLayout
    private val VERSION = "v5.99.1 — ✅ DIREKTA SA NAPILING PATH"

    private var repoOwner = ""
    private var repoName = ""

    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var savedDefaultPath = ""

    private var detectedPackagePath: String = ""
    private var detectedJavaRootPath: String = ""
    private val allComPaths = mutableListOf<String>()

    private val iconSizes = listOf(
        "mipmap-mdpi" to 48,
        "mipmap-hdpi" to 72,
        "mipmap-xhdpi" to 96,
        "mipmap-xxhdpi" to 144,
        "mipmap-xxxhdpi" to 192
    )

    data class GitHubFolder(val path: String, val type: String, val displayName: String)
    data class CatFileEntry(
        val filePath: String,
        val content: String,
        val fileName: String,
        var finalDestination: String? = null
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
        menuContainer = findViewById(R.id.main_menu_container)
        findViewById<TextView>(R.id.tvCurrentVersion)?.text = "📌 Bersyon: $VERSION"
        updateStatusDisplay()
        findViewById<Button>(R.id.btnCheckVersion)?.setOnClickListener { checkVersionFromGitHub() }
        findViewById<Button>(R.id.btnCloseDrawer)?.setOnClickListener { buildMainMenu() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen != "MAIN") buildMainMenu() else finish()
            }
        })

        if (!hasGitHubCredentials()) showGitHubSetupDialog()
        buildMainMenu()
    }

    private fun loadRepoSettings() {
        repoOwner = prefs.getString("github_username", "") ?: ""
        repoName = prefs.getString("github_repo", "apk-generator") ?: "apk-generator"
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
            .setPositiveButton("✅ I-SAVE AT SCAN") { d, _ ->
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
                d.dismiss()
            }
            .setNegativeButton("❌ MAMAYA NA", null)
            .setCancelable(false).show()
    }

    private suspend fun scanDirectory(path: String="", depth:Int=0, maxDepth:Int=12) {
        if (depth>maxDepth) return
        try {
            val apiPath = if(path.isEmpty()) "" else "/$path"
            val ja = JSONArray(URL("https://api.github.com/repos/$repoOwner/$repoName/contents$apiPath").readText())
            for(i in 0 until ja.length()) {
                val o = ja.getJSONObject(i)
                if(o.getString("type")=="dir") {
                    classifyAndAddFolder("${o.getString("path")}/")
                    scanDirectory(o.getString("path"),depth+1,maxDepth)
                }
            }
        } catch(_:Exception){}
    }

    private fun classifyAndAddFolder(fp: String) {
        val t: String
        val displayName: String

        if (fp.contains("/java/") && detectedJavaRootPath.isEmpty()) {
            val idx = fp.indexOf("/java/")
            detectedJavaRootPath = fp.substring(0, idx+6)
        }

        if ("/com/" in fp) {
            val parts = fp.split("/").filter { it.isNotEmpty() }
            val comIndex = parts.indexOf("com")
            if (comIndex >= 0 && comIndex < parts.lastIndex) {
                val packagePath = parts.drop(comIndex).joinToString("/") + "/"
                if (!allComPaths.contains(packagePath)) allComPaths.add(packagePath)
            }
        }

        when {
            fp.contains("mipmap-") || fp.contains("drawable") -> {
                displayName = "🖼️ $fp"; t = "icon"
            }
            fp.contains("/java/") && "/com/" in fp -> {
                displayName = "📦 PACKAGE → $fp"; t = "package"
            }
            fp.contains("/java/") || fp.contains("kotlin/") -> {
                displayName = "💻 $fp"; t = "code"
            }
            fp.contains("layout/") || fp.contains("values/") || fp.contains("xml/") -> {
                displayName = "🎨 LAYOUT/RES → $fp"; t = "layout"
            }
            else -> { displayName = "📁 $fp"; t = "other" }
        }
        scannedFolders.add(GitHubFolder(fp, t, displayName))
    }

    private fun scanRepositoryFolders() {
        if(!hasGitHubCredentials()) return
        CoroutineScope(Dispatchers.IO).launch {
            scannedFolders.clear(); allComPaths.clear(); detectedPackagePath = ""; detectedJavaRootPath = ""
            scanDirectory("",0,12)
            scannedFolders.add(0, GitHubFolder(".","root","🏠 ROOT"))
            scannedFolders.add(GitHubFolder("docs/","docs","📄 docs/"))
            val seen = mutableSetOf<String>(); scannedFolders.removeAll { !seen.add(it.path) }
            if (allComPaths.isNotEmpty()) detectedPackagePath = allComPaths.maxByOrNull { it.split("/").size } ?: ""
            val manifestPackage = tryReadManifestForPackage()
            if (manifestPackage.isNotEmpty()) detectedPackagePath = manifestPackage
            if (detectedPackagePath.isNotEmpty() && savedDefaultPath.isEmpty()) {
                val fullPath = if(detectedJavaRootPath.isNotEmpty()) detectedJavaRootPath + detectedPackagePath else detectedPackagePath
                savedDefaultPath = fullPath; saveDefaultPath()
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "✅ PACKAGE: $detectedPackagePath", Toast.LENGTH_LONG).show()
                }
            }
            launch(Dispatchers.Main) { if(currentScreen.startsWith("PATH")) buildPathCategoryMenu() }
        }
    }

    private suspend fun tryReadManifestForPackage(): String {
        try {
            val searchPaths = listOf("app/src/main/AndroidManifest.xml","src/main/AndroidManifest.xml","AndroidManifest.xml")
            var foundPath = ""
            for(p in searchPaths) { try { URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$p").readText(); foundPath = p; break } catch(_:Exception){} }
            if(foundPath.isEmpty()) return ""
            val json = JSONObject(URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$foundPath").readText())
            val manifestXml = String(Base64.decode(json.getString("content").replace("\n",""), Base64.DEFAULT))
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setInput(ByteArrayInputStream(manifestXml.toByteArray(Charsets.UTF_8)), null)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "manifest") {
                    val pkg = parser.getAttributeValue(null, "package")
                    if (!pkg.isNullOrEmpty()) return pkg.replace(".", "/") + "/"
                }
                parser.next()
            }
        } catch(_:Exception) {}
        return ""
    }

    private fun buildPathCategoryMenu() {
        currentScreen="PATH_CATEGORY"; scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"📂 DESTINASYON — $repoOwner/$repoName")
        if (savedDefaultPath.isNotEmpty()) {
            addMenuHeader(menuContainer,"💾 KASALUKUYANG DESTINASYON:"); addMenuHeader(menuContainer,"   $savedDefaultPath"); addMenuDivider(menuContainer)
        }
        if (detectedPackagePath.isNotEmpty()) {
            addMenuHeader(menuContainer,"📦 BUONG PACKAGE: $detectedPackagePath")
            val fullPath = if(detectedJavaRootPath.isNotEmpty()) detectedJavaRootPath + detectedPackagePath else detectedPackagePath
            addMenuItem(menuContainer,"P","📦 GAMITIN ANG PACKAGE → $fullPath") {
                savedDefaultPath = fullPath; saveDefaultPath()
                Toast.makeText(this,"💾 NA-SAVE: $fullPath",Toast.LENGTH_LONG).show()
                buildPathCategoryMenu()
            }
            addMenuDivider(menuContainer)
        }
        if(scannedFolders.isEmpty()){
            addMenuHeader(menuContainer,"🔍 SCANNING...")
            CoroutineScope(Dispatchers.Main).launch { scanRepositoryFolders(); delay(1200); buildPathCategoryMenu() }
            return
        }
        addMenuItem(menuContainer,"1","🖼️ ICON FOLDER"){showFilteredPaths("icon")}
        addMenuItem(menuContainer,"2","💻 CODE FOLDER"){showFilteredPaths("code")}
        addMenuItem(menuContainer,"3","🎨 LAYOUT/RES FOLDER"){showFilteredPaths("layout")}
        addMenuItem(menuContainer,"4","📂 LAHAT NG FOLDER"){showAllPaths()}
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer,"0","✏️ I-type ang sariling Path"){showCustomPathInput()}
        addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun showFilteredPaths(ft:String){
        currentScreen="PATH_LIST"; scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"📂 $ft — BUONG DAAN")
        scannedFolders.filter { it.type==ft || it.type=="package" || it.type=="root" || it.type=="docs" }
            .forEachIndexed { i,f ->
                addMenuItem(menuContainer,"${i+1}",f.displayName){
                    savedDefaultPath=f.path; saveDefaultPath()
                    Toast.makeText(this,"💾 NA-SAVE: $savedDefaultPath",Toast.LENGTH_LONG).show()
                    buildPathCategoryMenu()
                }
            }
        addMenuDivider(menuContainer); addMenuItem(menuContainer,"0","✏️ I-type ang sariling Path"){showCustomPathInput()}; addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildPathCategoryMenu()}
    }

    private fun showAllPaths() {
        currentScreen="PATH_ALL"; scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"📂 LAHAT NG FOLDER")
        scannedFolders.forEachIndexed { i,f ->
            addMenuItem(menuContainer,"${i+1}",f.displayName){
                savedDefaultPath=f.path; saveDefaultPath()
                Toast.makeText(this,"💾 NA-SAVE: $savedDefaultPath",Toast.LENGTH_LONG).show()
                buildPathCategoryMenu()
            }
        }
        addMenuDivider(menuContainer); addMenuItem(menuContainer,"0","✏️ I-type ang sariling Path"){showCustomPathInput()}; addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildPathCategoryMenu()}
    }

    private fun showCustomPathInput() {
        val exampleText = "👉 Ilagay ang buong daan + pangalan ng file\nHal: docs/  o  apps/GitHubUpdater/app/src/main/java/"
        val inp=EditText(this).apply{ hint = "docs/  o  filename.txt"; setText(savedDefaultPath) }
        android.app.AlertDialog.Builder(this).setTitle("✏️ ILAGAY ANG DAAN").setMessage(exampleText).setView(inp)
            .setPositiveButton("I-SAVE"){_,_->
                var p=inp.text.toString().trim()
                if(p.isNotEmpty()){
                    if(!p.endsWith("/") && !p.contains(".")) p="$p/"
                    savedDefaultPath=p; saveDefaultPath(); Toast.makeText(this,"💾 NA-SAVE: $p",Toast.LENGTH_LONG).show()
                }
            }.show()
    }

    private fun buildIconMenu() {
        currentScreen="ICON"; scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"🖼️ ICON → Pumili → Resize → Ipadala")
        addMenuItem(menuContainer,"1","📂 Pumili ng Larawan"){pickImage()}
        addMenuItem(menuContainer,"2","📏 I-resize sa 5 sukat"){resizeSelectedIconWithProcess()}
        addMenuItem(menuContainer,"3","📤 Ipadala sa GitHub"){pushIconToGitHub()}
        addMenuDivider(menuContainer); addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun pickImage() = pickImageLauncher.launch("image/*")

    private fun resizeSelectedIconWithProcess() {
        if(selectedImageUri==null){Toast.makeText(this,"⚠️ Pumili muna ng larawan!",Toast.LENGTH_LONG).show();return}
        menuContainer.removeAllViews(); addMenuHeader(menuContainer,"📏 NAGSISIMULA ANG RESIZE...")
        val pb=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=100;progress=0}; menuContainer.addView(pb)
        val pt=TextView(this).apply{text="0%";gravity=Gravity.CENTER}; menuContainer.addView(pt)
        CoroutineScope(Dispatchers.Main).launch{
            pt.text="🔍 Binabasa..."; delay(400)
            val bm=BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri!!))
            if(bm==null){pt.text="❌ Hindi mabasa ang larawan";return@launch}
            var progress=10; val step=(90 / iconSizes.size)
            iconSizes.forEach { (folderName, sizePx) ->
                pt.text="📏 $folderName — $sizePx×$sizePx"; pb.progress=progress
                val resized=Bitmap.createScaledBitmap(bm,sizePx,sizePx,true)
                val outDir=File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"processed-icons").apply{mkdirs()}
                val outFile=File(outDir,"${folderName}_ic_launcher.png")
                FileOutputStream(outFile).use{ resized.compress(Bitmap.CompressFormat.PNG,100,it) }
                progress+=step; pb.progress=progress; delay(350)
            }
            pb.progress=100; pt.text="100% ✅ TAPOS NA LAHAT NG SUKAT!"
            addMenuDivider(menuContainer); addMenuItem(menuContainer,"3","📤 Ipadala sa GitHub"){pushIconToGitHub()}
        }
    }

    private fun pushIconToGitHub() {
        if(!hasGitHubCredentials()){showGitHubSetupDialog();return}
        if(savedDefaultPath.isEmpty()){Toast.makeText(this,"⚠️ Piliin muna ang Path sa Option 4",Toast.LENGTH_LONG).show();return}
        android.app.AlertDialog.Builder(this).setTitle("📤 KUMPIRMA").setMessage("${iconSizes.size} na icon → $savedDefaultPath")
            .setPositiveButton("✅ IPADALA"){_,_->
                val tok=getGitHubToken()
                CoroutineScope(Dispatchers.IO).launch{
                    var okCount=0
                    val outDir=File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"processed-icons")
                    iconSizes.forEach { (folderName, _) ->
                        val file=File(outDir,"${folderName}_ic_launcher.png")
                        if(!file.exists()) return@forEach
                        try{
                            val b64=Base64.encodeToString(file.readBytes(),Base64.NO_WRAP)
                            val conn=URL("https://api.github.com/repos/$repoOwner/$repoName/contents/${savedDefaultPath}$folderName/ic_launcher.png").openConnection() as HttpURLConnection
                            conn.apply{ requestMethod="PUT"; setRequestProperty("Authorization","token $tok"); setRequestProperty("Content-Type","application/json"); doOutput=true }
                            OutputStreamWriter(conn.outputStream).use{ it.write(JSONObject().put("message","📤 ICON $folderName — MartoPush").put("content",b64).toString()) }
                            if(conn.responseCode in 200..201) okCount++; conn.disconnect()
                        }catch(_:Exception){}
                    }
                    launch(Dispatchers.Main){ Toast.makeText(this@MainActivity,"✅ $okCount/${iconSizes.size} naipadala!",Toast.LENGTH_LONG).show() }
                }
            }.setNegativeButton("❌ HINDI MUNA",null).show()
    }

    // ==========================================
    // 📄 OPTION 2 — CAT CODE — ✅ INA-AYOS: DIREKTA SA NAPILING PATH!
    // ==========================================
    private fun buildCatCodeMenu() {
        currentScreen="CATCODE"; scrollToTop(); parsedCatFiles.clear(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"📄 OPTION 2 — CAT CODE / FILE PAGPADALA")
        if(savedDefaultPath.isNotEmpty()) addMenuHeader(menuContainer,"💾 KASALUKUYANG DESTINASYON: $savedDefaultPath")
        addMenuItem(menuContainer,"1","📋 I-PASTE ANG LAMAN O CAT CODE"){showCatCodeInputDialog()}
        addMenuItem(menuContainer,"2","📂 Piliin ang Default Path"){buildPathCategoryMenu()}
        addMenuItem(menuContainer,"3","📤 I-PADALA LAHAT"){
            if(parsedCatFiles.isEmpty()) Toast.makeText(this,"⚠️ Mag-paste muna ng code!",Toast.LENGTH_SHORT).show()
            else showDestinationPickerDialog()
        }
        addMenuDivider(menuContainer); addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun showCatCodeInputDialog() {
        val hintText = "I-paste ang file o Cat Code dito..."
        val inp=EditText(this).apply{ hint = hintText; minLines=16; maxLines=28; textSize=12f }
        android.app.AlertDialog.Builder(this).setTitle("📋 I-PASTE ANG LAMAN / CAT CODE").setView(inp)
            .setPositiveButton("✅ BASAHIN"){_,_-> val code=inp.text.toString(); if(code.isBlank()) Toast.makeText(this,"❌ Walang laman!",Toast.LENGTH_SHORT).show() else parseCatCode(code) }
            .setNegativeButton("❌ KANSILA",null).show()
    }

    private fun parseCatCode(code: String) {
        parsedCatFiles.clear()
        val lines = code.lines()
        var i = 0
        var detectedHeader = false

        while (i < lines.size) {
            val line = lines[i]
            var filePath: String?
            var currentContent = StringBuilder()
            var inContent: Boolean

            when {
                line.startsWith("--- FILE:") -> {
                    detectedHeader = true
                    filePath = line.removePrefix("--- FILE:").substringBefore("---").trim()
                    inContent = true
                    i++
                }
                line.startsWith("cat >") && line.contains("<<") -> {
                    detectedHeader = true
                    filePath = line.removePrefix("cat >").split("<<")[0].trim()
                    inContent = true
                    i++
                }
                else -> { i++; continue }
            }

            while (i < lines.size && inContent) {
                val cl = lines[i]
                val t = cl.trim()
                val end = t == "--- END ---" || t == "EOF" || t == "'EOF'" || t == "ENDSCRIPT" || t == "ENDOFFILE" || t.startsWith("--- END ---")
                if (end) { i++; break }
                if (currentContent.isNotEmpty()) currentContent.append("\n")
                currentContent.append(cl)
                i++
            }

            if (!filePath.isNullOrBlank() && currentContent.isNotBlank()) {
                val fn = filePath.split("/").last()
                parsedCatFiles.add(CatFileEntry(filePath, currentContent.toString().trimEnd(), fn))
            }
        }

        if (!detectedHeader && code.isNotBlank()) { askDestinationForPlainContent(code); return }
        showCatCodePreview()
    }

    private fun askDestinationForPlainContent(content:String){
        val msg="👉 Ilagay ang pangalan ng file"
        val inp=EditText(this).apply{ hint = "pangalan.txt" }
        android.app.AlertDialog.Builder(this).setTitle("📄 LAMAN LANG ANG NAKITA").setMessage(msg).setView(inp)
            .setPositiveButton("✅ I-ANALISA"){_,_->
                val p=inp.text.toString().trim()
                if(p.isBlank()){Toast.makeText(this,"❌ Kailangan ang pangalan ng file!",Toast.LENGTH_SHORT).show();return@setPositiveButton}
                parsedCatFiles.add(CatFileEntry(p,content.trimEnd(),p.split("/").last()))
                showCatCodePreview()
            }.setNegativeButton("❌ KANSILA",null).show()
    }

    private fun showCatCodePreview() {
        scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"✅ NABASA — ${parsedCatFiles.size} na file")
        parsedCatFiles.forEachIndexed{i,e->
            addMenuItem(menuContainer,"${i+1}","📄 ${e.fileName}"){
                android.app.AlertDialog.Builder(this).setTitle(e.fileName).setMessage("📂 DAAN: ${e.filePath}\n\n${e.content.take(500)}...").setPositiveButton("OK",null).show()
            }
            addMenuHeader(menuContainer,"   📂 → ${e.filePath}")
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer,"3","📤 PUMILI NG DESTINASYON AT IPADALA"){ showDestinationPickerDialog() }
        addMenuItem(menuContainer,"b","⬅️ Bumalik"){buildCatCodeMenu()}
    }

    // ✅ INA-AYOS — DIREKTA SA NAPILING PATH! WALANG DAGDAG NA SUB-FOLDER!
    private fun showDestinationPickerDialog() {
        if(parsedCatFiles.isEmpty()) {
            Toast.makeText(this,"⚠️ Wala pang file na ipapadala!",Toast.LENGTH_SHORT).show()
            return
        }

        val choices = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        // 1 — Default Path
        if(savedDefaultPath.isNotEmpty()) {
            choices.add("✅ Gamitin Default: $savedDefaultPath")
            actions.add({ applyPathAndConfirm(savedDefaultPath) })
        }

        // 2 — Package Path
        if(detectedPackagePath.isNotEmpty()) {
            val fullPkgPath = if(detectedJavaRootPath.isNotEmpty())
                                  detectedJavaRootPath + detectedPackagePath
                              else detectedPackagePath
            choices.add("📦 Gamitin Package: $fullPkgPath")
            actions.add({ applyPathAndConfirm(fullPkgPath) })
        }

        // 3 — Listahan ng Folder
        choices.add("📂 Pumili mula sa listahan ng folder")
        actions.add({ showFolderSelectionMenu() })

        // 4 — Sariling daan
        choices.add("✏️ I-type ang sariling daan")
        actions.add({ showManualPathDialog() })

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 SAAN MO GUSTONG IPADALA? — ${parsedCatFiles.size} na file")
            .setItems(choices.toTypedArray()) { _, index ->
                actions[index].invoke()
            }
            .setNegativeButton("❌ Kanselahin", null)
            .setCancelable(true)
            .show()
    }

    // ✅ PINAKA-INA-AYOS — DIREKTA SA NAPILING PATH! WALANG DAGDAG!
    private fun applyPathAndConfirm(basePath: String) {
        parsedCatFiles.forEach { file ->
            file.finalDestination = when {
                // ✅ KUNG MAY SARILING DAAN SA FILE — GAMITIN ITO
                file.filePath.contains("/") -> file.filePath

                // ✅ KUNG WALANG — DIREKTANG ILAGAY SA NAPILING BASE PATH + PANGALAN NG FILE
                else -> {
                    val cleanBase = if(basePath.endsWith("/")) basePath else "$basePath/"
                    cleanBase + file.fileName
                }
            }
        }
        confirmAndPushFiles()
    }

    private fun showManualPathDialog() {
        val inp = EditText(this).apply {
            hint = "hal: docs/  o  apps/GitHubUpdater/"
            setText(savedDefaultPath)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG DESTINASYON")
            .setMessage("Ilagay ang base path kung saan pupunta ang lahat ng file:\n👉 Hal: docs/ → direkta sa docs/")
            .setView(inp)
            .setPositiveButton("✅ GAMITIN") { _, _ ->
                var p = inp.text.toString().trim()
                if(p.isNotEmpty()) {
                    if(!p.endsWith("/") && !p.contains(".")) p = "$p/"
                    savedDefaultPath = p; saveDefaultPath()
                    applyPathAndConfirm(p)
                }
            }
            .setNegativeButton("❌ BUMALIK", null)
            .show()
    }

    private fun showFolderSelectionMenu() {
        currentScreen = "PICK_FOLDER"; scrollToTop()
        menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"📂 PUMILI NG DESTINASYON")
        addMenuDivider(menuContainer)
        scannedFolders.forEachIndexed { i, folder ->
            addMenuItem(menuContainer,"${i+1}",folder.displayName) {
                savedDefaultPath = folder.path; saveDefaultPath()
                Toast.makeText(this,"💾 NAPILI: ${folder.path}",Toast.LENGTH_LONG).show()
                applyPathAndConfirm(folder.path)
            }
        }
        addMenuDivider(menuContainer)
        addMenuItem(menuContainer,"0","✏️ I-type ang sariling daan") { showManualPathDialog() }
        addMenuItem(menuContainer,"b","⬅️ Bumalik") { showCatCodePreview() }
    }

    private fun confirmAndPushFiles() {
        val previewText = StringBuilder()
        previewText.append("${parsedCatFiles.size} na file — KUMPIRMA ANG DESTINASYON:\n\n")
        parsedCatFiles.forEachIndexed { i, file ->
            previewText.append("  [${i+1}] ${file.fileName}\n      → ${file.finalDestination}\n")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA ANG PAGPADALA")
            .setMessage(previewText.toString())
            .setPositiveButton("✅ IPADALA NA") { _, _ -> actuallyPushFilesNow() }
            .setNegativeButton("❌ HINDI MUNA", null)
            .show()
    }

    private fun actuallyPushFilesNow() {
        if(!hasGitHubCredentials()){showGitHubSetupDialog();return}
        val list = ArrayList(parsedCatFiles)
        Toast.makeText(this,"📤 Pinapadala...",Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            var ok = 0
            val tok = getGitHubToken()
            list.forEachIndexed { index, file ->
                try {
                    val targetPath = file.finalDestination ?: file.filePath
                    val b64 = Base64.encodeToString(file.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    val conn = URL("https://api.github.com/repos/$repoOwner/$repoName/contents/$targetPath").openConnection() as HttpURLConnection
                    conn.apply {
                        requestMethod = "PUT"
                        setRequestProperty("Authorization", "token $tok")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    OutputStreamWriter(conn.outputStream).use {
                        it.write(JSONObject().put("message","📤 ${file.fileName} — MartoPush").put("content",b64).toString())
                    }
                    if(conn.responseCode in 200..201) {
                        ok++
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity,"✅ [${index+1}/${list.size}] ${file.fileName}",Toast.LENGTH_SHORT).show()
                        }
                    }
                    conn.disconnect()
                } catch(ex: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity,"❌ ${file.fileName}: ${ex.message}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,"✅ TAPOS NA! $ok/${list.size} naipadala",Toast.LENGTH_LONG).show()
                parsedCatFiles.clear()
                buildCatCodeMenu()
            }
        }
    }

    private fun pushToGitHub() {
        Toast.makeText(this,"📤 Direktang pagpadala — Tapos na!",Toast.LENGTH_SHORT).show()
    }

    private fun checkVersionFromGitHub() {
        val tv=findViewById<TextView>(R.id.tvStatus)?:return
        tv.text="🔍 Tinitignan..."
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val ver=JSONObject(URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").readText())
                    .optString("tag_name",VERSION).removePrefix("v")
                launch(Dispatchers.Main){ tv.text=if(ver==VERSION.removePrefix("v")) "✅ Pinakabago: v$ver" else "⚠️ May Bago: v$ver" }
            }catch(_:Exception){ launch(Dispatchers.Main){ tv.text="⚠️ Hindi matignan" } }
        }
    }

    private fun buildMainMenu() {
        currentScreen="MAIN"; scrollToTop(); menuContainer.removeAllViews()
        addMenuHeader(menuContainer,"========================================")
        addMenuHeader(menuContainer,"       📤  M A R T O P U S H  $VERSION")
        addMenuHeader(menuContainer,"    Developed by MartoDosko © 2026")
        addMenuHeader(menuContainer,"========================================")
        if(savedDefaultPath.isNotEmpty()) addMenuHeader(menuContainer,"💾 KASALUKUYANG DESTINASYON: $savedDefaultPath")
        addMenuItem(menuContainer,"1","🖼️ ICON — Pumili, Resize, Ipadala"){buildIconMenu()}
        addMenuItem(menuContainer,"2","📄 Ipadala ang Cat Code / File"){buildCatCodeMenu()}
        addMenuItem(menuContainer,"3","📤 Direktang Pagpadala"){pushToGitHub()}
        addMenuItem(menuContainer,"4","📂 Piliin / I-set ang Destination Path"){buildPathCategoryMenu()}
        addMenuItem(menuContainer,"5","🔄 Tumatsek ng Update at Bersyon"){checkVersionFromGitHub()}
        addMenuDivider(menuContainer); addMenuItem(menuContainer,"0","↩️ Lumabas"){finish()}
    }

    private fun getFileName(u:Uri):String{
        contentResolver.query(u,null,null,null,null)?.use{
            if(it.moveToFirst()){
                val i=it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(i>=0)return it.getString(i)
            }
        }
        return "larawan.png"
    }
    private fun space(dp:Int)=View(this).apply{layoutParams=LinearLayout.LayoutParams(0,dp)}
    private fun addMenuHeader(c:LinearLayout,t:String){
        c.addView(TextView(this).apply{text=t;gravity=Gravity.CENTER;setTextColor(0xFF1565C0.toInt());setTypeface(null,android.graphics.Typeface.BOLD)})
    }
    private fun addMenuItem(c:LinearLayout,n:String,l:String,a:()->Unit){
        c.addView(Button(this).apply{text="[$n]   $l";setOnClickListener{a()}})
    }
    private fun addMenuDivider(c:LinearLayout){
        c.addView(View(this).apply{setBackgroundColor(0xFFE0E0E0.toInt());layoutParams=LinearLayout.LayoutParams(-1,1)})
    }
}
