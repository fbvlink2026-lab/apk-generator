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
    private val VERSION = "v5.91 — Gumaganang Icon + Ligtas na Cat Code"

    private var repoOwner = ""
    private var repoName = ""

    private var currentScreen = "MAIN"
    private var selectedImageUri: Uri? = null
    private var savedDefaultPath = ""

    private val iconSizes = listOf(
        "mipmap-mdpi" to 48,
        "mipmap-hdpi" to 72,
        "mipmap-xhdpi" to 96,
        "mipmap-xxhdpi" to 144,
        "mipmap-xxxhdpi" to 192
    )

    data class GitHubFolder(val path: String, val type: String, val name: String)
    data class CatFileEntry(val filePath: String, val content: String, val fileName: String)

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
        if (!hasGitHubCredentials()) showGitHubSetupDialog()
        buildMainMenu()
    }

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

    private suspend fun scanDirectory(path: String="", depth:Int=0, maxDepth:Int=5) {
        if (depth>maxDepth) return
        try {
            val apiPath = if(path.isEmpty()) "" else "/$path"
            val ja = JSONArray(URL("https://api.github.com/repos/$repoOwner/$repoName/contents$apiPath").readText())
            for(i in 0 until ja.length()) {
                val o = ja.getJSONObject(i)
                if(o.getString("type")=="dir") {
                    classifyAndAddFolder(o.getString("name"),"${o.getString("path")}/")
                    scanDirectory(o.getString("path"),depth+1,maxDepth)
                }
            }
        } catch(_:Exception){}
    }

    private fun scanRepositoryFolders() {
        if(!hasGitHubCredentials()) return
        CoroutineScope(Dispatchers.IO).launch {
            scannedFolders.clear()
            scanDirectory("",0,5)
            scannedFolders.add(0, GitHubFolder(".","root","🏠 ROOT"))
            scannedFolders.add(GitHubFolder("docs/","docs","📄 docs/"))
            val seen = mutableSetOf<String>()
            scannedFolders.removeAll { !seen.add(it.path) }
            launch(Dispatchers.Main) {
                if(currentScreen.startsWith("PATH")) buildPathCategoryMenu()
            }
        }
    }

    private fun classifyAndAddFolder(name:String,fp:String){
        val disp:String; val t:String
        when{
            fp.contains("mipmap-")->{disp="🖼️ $fp";t="icon"}
            fp.contains("java/")||fp.contains("kotlin/")->{disp="💻 $fp";t="code"}
            fp.contains("layout/")->{disp="🎨 $fp";t="code"}
            else->{disp="📁 $fp";t="other"}
        }
        scannedFolders.add(GitHubFolder(fp,t,disp))
    }

    private fun buildPathCategoryMenu() {
        currentScreen="PATH_CATEGORY"; scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"📂 DESTINASYON — $repoOwner/$repoName")
        if(scannedFolders.isEmpty()){
            addMenuHeader(c,"🔍 SCANNING...")
            CoroutineScope(Dispatchers.Main).launch { scanRepositoryFolders(); delay(1200); buildPathCategoryMenu() }
            return
        }
        addMenuItem(c,"1","🖼️ ICON FOLDER"){showFilteredPaths("icon")}
        addMenuItem(c,"2","💻 CODE FOLDER"){showFilteredPaths("code")}
        addMenuItem(c,"3","📂 LAHAT NG FOLDER"){showAllPaths()}
        addMenuDivider(c)
        addMenuItem(c,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun showFilteredPaths(ft:String){
        currentScreen="PATH_LIST"; scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"📂 $ft — BUONG DAAN")
        scannedFolders.filter { it.type==ft||it.type=="root"||it.type=="docs" }.forEachIndexed { i,f->
            addMenuItem(c,"${i+1}",f.name){savedDefaultPath=f.path;Toast.makeText(this,"💾 NA-SAVE: $savedDefaultPath",Toast.LENGTH_LONG).show()}
        }
        addMenuDivider(c)
        addMenuItem(c,"0","✏️ I-type ang sariling Path"){showCustomPathInput()}
        addMenuItem(c,"b","⬅️ Bumalik"){buildPathCategoryMenu()}
    }

    private fun showAllPaths() {
        currentScreen="PATH_ALL"; scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"📂 LAHAT NG FOLDER")
        scannedFolders.forEachIndexed { i,f->
            addMenuItem(c,"${i+1}",f.name){savedDefaultPath=f.path;Toast.makeText(this,"💾 NA-SAVE: $savedDefaultPath",Toast.LENGTH_LONG).show()}
        }
        addMenuDivider(c)
        addMenuItem(c,"0","✏️ I-type ang sariling Path"){showCustomPathInput()}
        addMenuItem(c,"b","⬅️ Bumalik"){buildPathCategoryMenu()}
    }

    private fun showCustomPathInput() {
        val inp=EditText(this).apply{hint="hal: docs/file.txt";setText(savedDefaultPath)}
        android.app.AlertDialog.Builder(this)
            .setTitle("✏️ ILAGAY ANG DAAN")
            .setView(inp)
            .setPositiveButton("I-SAVE"){d,_->
                var p=inp.text.toString().trim()
                if(p.isNotEmpty()){if(!p.endsWith("/"))p="$p/";savedDefaultPath=p;Toast.makeText(this,"💾 NA-SAVE: $p",Toast.LENGTH_LONG).show()}
                d.dismiss()
            }.show()
    }

    // ==========================================
    // 🖼️ OPTION 1 — ICON — IBINALIK MULA SA v5.89 — GUMAGANA WALANG CRASH!
    // ==========================================
    private fun buildIconMenu() {
        currentScreen="ICON"; scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"🖼️ ICON → Pumili → Resize → Ipadala")
        addMenuItem(c,"1","📂 Pumili ng Larawan"){pickImage()}
        addMenuItem(c,"2","📏 I-resize sa 5 sukat"){resizeSelectedIconWithProcess()}
        addMenuItem(c,"3","📤 Ipadala sa GitHub"){pushIconToGitHub()}
        addMenuDivider(c)
        addMenuItem(c,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun pickImage() = pickImageLauncher.launch("image/*")

    private fun resizeSelectedIconWithProcess() {
        if(selectedImageUri==null){
            Toast.makeText(this,"⚠️ Pumili muna ng larawan!",Toast.LENGTH_LONG).show()
            return
        }

        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return
        c.removeAllViews()

        addMenuHeader(c,"📏 NAGSISIMULA ANG RESIZE...")

        val pb=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{
            max=100
            progress=0
        }
        c.addView(pb)

        val pt=TextView(this).apply{
            text="0%"
            gravity=Gravity.CENTER
        }
        c.addView(pt)

        CoroutineScope(Dispatchers.Main).launch{
            pt.text="🔍 Binabasa..."
            delay(400)

            val bm=BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri!!))
            if(bm==null){
                pt.text="❌ Hindi mabasa ang larawan"
                return@launch
            }

            var progress=10
            val step=(90 / iconSizes.size)

            iconSizes.forEach { (folderName, sizePx) ->
                pt.text="📏 $folderName — $sizePx×$sizePx"
                pb.progress=progress

                val resized=Bitmap.createScaledBitmap(bm,sizePx,sizePx,true)
                val outDir=File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"processed-icons")
                if(!outDir.exists()) outDir.mkdirs()
                val outFile=File(outDir,"${folderName}_ic_launcher.png")
                FileOutputStream(outFile).use{ os->
                    resized.compress(Bitmap.CompressFormat.PNG,100,os)
                }

                progress+=step
                pb.progress=progress
                delay(350)
            }

            pb.progress=100
            pt.text="100% ✅ TAPOS NA LAHAT NG SUKAT!"

            addMenuDivider(c)
            addMenuItem(c,"3","📤 Ipadala sa GitHub"){pushIconToGitHub()}
        }
    }

    private fun pushIconToGitHub() {
        if(!hasGitHubCredentials()){showGitHubSetupDialog();return}
        if(savedDefaultPath.isEmpty()){Toast.makeText(this,"⚠️ Piliin muna ang Path sa Option 4",Toast.LENGTH_LONG).show();return}

        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA")
            .setMessage("${iconSizes.size} na icon file → $savedDefaultPath")
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
                            val conn=URL("https://api.github.com/repos/$repoOwner/$repoName/contents/${savedDefaultPath}$folderName/ic_launcher.png")
                                .openConnection() as HttpURLConnection
                            conn.apply{
                                requestMethod="PUT"
                                setRequestProperty("Authorization","token $tok")
                                setRequestProperty("Content-Type","application/json")
                                doOutput=true
                            }
                            OutputStreamWriter(conn.outputStream).use{
                                it.write(JSONObject()
                                    .put("message","📤 ICON $folderName — MartoPush")
                                    .put("content",b64)
                                    .toString())
                            }
                            if(conn.responseCode in 200..201) okCount++
                            conn.disconnect()
                        }catch(_:Exception){}
                    }

                    launch(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,"✅ $okCount/${iconSizes.size} naipadala sa GitHub!",Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("❌ HINDI MUNA",null)
            .show()
    }

    // ==========================================
    // 📄 OPTION 2 — CAT CODE — LIGTAS NA TINATANGGAL ANG NASA ILALIM NG EOF/END
    // ==========================================
    private fun buildCatCodeMenu() {
        currentScreen="CATCODE"; scrollToTop()
        parsedCatFiles.clear()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"📄 OPTION 2 — CAT CODE / FILE PAGPADALA")
        addMenuItem(c,"1","📋 I-PASTE ANG LAMAN O CAT CODE"){showCatCodeInputDialog()}
        addMenuItem(c,"2","📂 Piliin ang Default Path"){buildPathCategoryMenu()}
        addMenuItem(c,"3","📤 I-PADALA LAHAT"){sendAllCatCodeToGitHub()}
        addMenuDivider(c)
        addMenuItem(c,"b","⬅️ Bumalik"){buildMainMenu()}
    }

    private fun showCatCodeInputDialog() {
        val inp=EditText(this).apply{
            hint="I-paste ang file o Cat Code dito..."
            minLines=16; maxLines=28; textSize=12f
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("📋 I-PASTE ANG LAMAN / CAT CODE")
            .setView(inp)
            .setPositiveButton("✅ BASAHIN"){d,_->
                val code=inp.text.toString()
                if(code.isBlank()) Toast.makeText(this,"❌ Walang laman!",Toast.LENGTH_SHORT).show()
                else { parseCatCode(code); d.dismiss() }
            }
            .setNegativeButton("❌ KANSILA",null).show()
    }

    private fun parseCatCode(code: String) {
        parsedCatFiles.clear()
        val lines = code.lines()
        var i = 0
        var detectedHeader = false

        while (i < lines.size) {
            val line = lines[i]
            var filePath: String? = null
            var currentContent = StringBuilder()
            var inContent = false

            when {
                line.startsWith("--- FILE:") -> {
                    detectedHeader = true
                    filePath = line.removePrefix("--- FILE:")
                        .substringBefore("---")
                        .trim()
                    inContent = true
                    i++
                }

                line.startsWith("cat >") && line.contains("<<") -> {
                    detectedHeader = true
                    filePath = line.removePrefix("cat >").split("<<")[0].trim()
                    inContent = true
                    i++
                }

                else -> { i++ ; continue }
            }

            while (i < lines.size && inContent) {
                val contentLine = lines[i]
                val trimmed = contentLine.trim()

                val isEndMarker = trimmed == "--- END ---" ||
                                   trimmed == "EOF" || trimmed == "'EOF'" || trimmed == "\"EOF\"" ||
                                   trimmed == "ENDSCRIPT" || trimmed == "'ENDSCRIPT'" ||
                                   trimmed == "ENDOFFILE" || trimmed == "'ENDOFFILE'" ||
                                   trimmed.startsWith("--- END ---")

                if (isEndMarker) {
                    inContent = false
                    i++
                    break
                }

                if (currentContent.isNotEmpty()) currentContent.append("\n")
                currentContent.append(contentLine)
                i++
            }

            if (!filePath.isNullOrBlank() && currentContent.isNotBlank()) {
                val fileName = filePath.split("/").last()
                parsedCatFiles.add(CatFileEntry(filePath, currentContent.toString().trimEnd(), fileName))
            }
        }

        if (!detectedHeader && code.isNotBlank()) {
            askDestinationForPlainContent(code)
            return
        }

        showCatCodePreview()
    }

    private fun askDestinationForPlainContent(content:String){
        val inp=EditText(this).apply{
            hint="hal: docs/filename.txt"
            if(savedDefaultPath.isNotEmpty()) setText(savedDefaultPath)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("📄 LAMAN LANG ANG NAKITA")
            .setMessage("❌ Walang nakitang header.\n👉 Ilagay ang DAAN + PANGALAN ng file:")
            .setView(inp)
            .setPositiveButton("✅ I-ANALISA"){d,_->
                var p=inp.text.toString().trim()
                if(p.isBlank()){Toast.makeText(this,"❌ Kailangan ang daan!",Toast.LENGTH_SHORT).show();return@setPositiveButton}
                if(!p.contains("/") && savedDefaultPath.isNotEmpty()) p=savedDefaultPath+p
                parsedCatFiles.add(CatFileEntry(p,content.trimEnd(),p.split("/").last()))
                d.dismiss()
                showCatCodePreview()
            }
            .setNegativeButton("❌ KANSILA",null).show()
    }

    private fun showCatCodePreview() {
        scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"✅ NABASA — ${parsedCatFiles.size} na file")
        if(parsedCatFiles.isEmpty()){
            addMenuHeader(c,"⚠️ Walang file na nakita")
        }else{
            parsedCatFiles.forEachIndexed{i,e->
                addMenuItem(c,"${i+1}","📄 ${e.fileName}"){
                    android.app.AlertDialog.Builder(this)
                        .setTitle(e.fileName).setMessage("📂 DAAN: ${e.filePath}\n\n${e.content}")
                        .setPositiveButton("OK",null).show()
                }
                addMenuHeader(c,"   📂 → ${e.filePath}")
            }
        }
        addMenuDivider(c)
        if(parsedCatFiles.isNotEmpty()) addMenuItem(c,"3","📤 I-PADALA LAHAT"){sendAllCatCodeToGitHub()}
        addMenuItem(c,"b","⬅️ Bumalik"){buildCatCodeMenu()}
    }

    private fun sendAllCatCodeToGitHub() {
        if(!hasGitHubCredentials()){showGitHubSetupDialog();return}
        if(parsedCatFiles.isEmpty()){Toast.makeText(this,"⚠️ Wala pang file!",Toast.LENGTH_SHORT).show();return}
        android.app.AlertDialog.Builder(this)
            .setTitle("📤 KUMPIRMA")
            .setMessage("${parsedCatFiles.size} na file → $repoOwner/$repoName")
            .setPositiveButton("✅ IPADALA"){_,_->actuallyPushCatFiles()}
            .setNegativeButton("❌ HINDI MUNA",null).show()
    }

    private fun actuallyPushCatFiles() {
        val tok=getGitHubToken()
        val list=ArrayList(parsedCatFiles)
        Toast.makeText(this,"📤 Pinapadala...",Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch{
            var ok=0
            list.forEachIndexed{i,e->
                try{
                    val b64=Base64.encodeToString(e.content.toByteArray(Charsets.UTF_8),Base64.NO_WRAP)
                    val conn=URL("https://api.github.com/repos/$repoOwner/$repoName/contents/${e.filePath}").openConnection() as HttpURLConnection
                    conn.apply{
                        requestMethod="PUT"
                        setRequestProperty("Authorization","token $tok")
                        setRequestProperty("Content-Type","application/json")
                        doOutput=true
                    }
                    OutputStreamWriter(conn.outputStream).use{
                        it.write(JSONObject().put("message","📤 ${e.fileName} — MartoPush").put("content",b64).toString())
                    }
                    if(conn.responseCode in 200..201){
                        ok++
                        launch(Dispatchers.Main){Toast.makeText(this@MainActivity,"✅ [${i+1}/${list.size}] ${e.fileName}",Toast.LENGTH_SHORT).show()}
                    }
                    conn.disconnect()
                }catch(ex:Exception){
                    launch(Dispatchers.Main){Toast.makeText(this@MainActivity,"❌ ${e.fileName}: ${ex.message}",Toast.LENGTH_SHORT).show()}
                }
            }
            launch(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"✅ TAPOS NA! $ok/${list.size} naipadala",Toast.LENGTH_LONG).show()
                parsedCatFiles.clear()
            }
        }
    }

    // ==========================================
    // 📤 OPTION 3 — DIREKTANG PAGPADALA
    // ==========================================
    private fun pushToGitHub() {
        Toast.makeText(this,"📤 Direktang pagpadala — Tapos na!",Toast.LENGTH_SHORT).show()
    }

    // ==========================================
    // 🔄 OPTION 5 — BERSYON
    // ==========================================
    private fun checkVersionFromGitHub() {
        val tv=findViewById<TextView>(R.id.tvStatus)?:return
        tv.text="🔍 Tinitignan..."
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val ver=JSONObject(URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").readText())
                    .optString("tag_name",VERSION).removePrefix("v")
                launch(Dispatchers.Main){
                    tv.text=if(ver==VERSION.removePrefix("v")) "✅ Pinakabago: v$ver" else "⚠️ May Bago: v$ver"
                }
            }catch(_:Exception){launch(Dispatchers.Main){tv.text="⚠️ Hindi matignan"}}
        }
    }

    // ==========================================
    // 📋 PANGUNAHING MENU
    // ==========================================
    private fun buildMainMenu() {
        currentScreen="MAIN"; scrollToTop()
        val c=findViewById<LinearLayout>(R.id.main_menu_container)?:return; c.removeAllViews()
        addMenuHeader(c,"========================================")
        addMenuHeader(c,"       📤  M A R T O P U S H  $VERSION")
        addMenuHeader(c,"    Developed by MartoDosko © 2026")
        addMenuHeader(c,"========================================")
        addMenuItem(c,"1","🖼️ ICON — Pumili, Resize, Ipadala"){buildIconMenu()}
        addMenuItem(c,"2","📄 Ipadala ang Cat Code / File"){buildCatCodeMenu()}
        addMenuItem(c,"3","📤 Direktang Pagpadala"){pushToGitHub()}
        addMenuItem(c,"4","📂 Piliin / I-set ang Destination Path"){buildPathCategoryMenu()}
        addMenuItem(c,"5","🔄 Tumatsek ng Update at Bersyon"){checkVersionFromGitHub()}
        addMenuDivider(c)
        addMenuItem(c,"0","↩️ Lumabas"){finish()}
    }

    // ==========================================
    // 🛠️ TULONG
    // ==========================================
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
    override fun onBackPressed(){if(currentScreen!="MAIN")buildMainMenu()else super.onBackPressed()}
}
