package com.martodosko.guitarfx
import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object { const val VERSION = "v1.0.0" }
    private val handler = Handler(android.os.Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoading()
    }

    private fun showLoading() {
        setContentView(LinearLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(TextView(this@MainActivity).apply {
                text = "🎸 GuitarFX"
                textSize = 48f
                setTextColor(Color.parseColor("#FFD700"))
                setPadding(0, 0, 0, 40)
            })
            addView(TextView(this@MainActivity).apply {
                text = "Created & Developed by\nMartoDosko © 2026\n$VERSION"
                textSize = 16f
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = Gravity.CENTER
            })
        })
        handler.postDelayed({
            val needed = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.RECORD_AUDIO)
            if (needed.isNotEmpty()) androidx.core.app.ActivityCompat.requestPermissions(this@MainActivity, needed.toTypedArray(), 101)
            else initApp()
        }, 2500)
    }

    private fun initApp() {
        AudioWrapper.nativeInit()
        setContentView(LinearLayout(this).apply {
            setBackgroundColor(Color.parseColor("#F0F4F8"))
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            addView(TextView(this@MainActivity).apply {
                text = "🎸 GuitarFX — $VERSION"
                textSize = 28f
                setTextColor(Color.parseColor("#1A202C"))
                setPadding(0, 0, 0, 24)
            })
            addView(Button(this@MainActivity).apply {
                text = "⚡ POWER — OFF"
                setBackgroundColor(Color.parseColor("#CC0000"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    val on = text.contains("OFF")
                    text = if (on) "⚡ POWER — ON" else "⚡ POWER — OFF"
                    setBackgroundColor(if (on) Color.parseColor("#00AA00") else Color.parseColor("#CC0000"))
                    AudioWrapper.setPower(on)
                    if (on) AudioWrapper.nativeStart() else AudioWrapper.nativeStop()
                }
            })
        })
    }
}
