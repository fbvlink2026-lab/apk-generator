package com.martodosko.guitarfx

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        const val VERSION = "v1.0.0"
    }

    private val handler = Handler(android.os.Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingScreen()
    }

    private fun showLoadingScreen() {
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
                text = "Created & Developed by\nMartoDosko © 2026\n\n$VERSION"
                textSize = 16f
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = Gravity.CENTER
                setLineSpacing(8f, 1f)
            })
        })

        handler.postDelayed({ checkPermissions() }, 3000)
    }

    private fun checkPermissions() {
        val neededPermissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                neededPermissions.toTypedArray(),
                101
            )
        } else {
            initMainApp()
        }
    }

    private fun initMainApp() {
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
                setPadding(0, 24, 0, 24)
                textSize = 18f

                setOnClickListener {
                    val isOn = text.contains("OFF").not()
                    text = if (isOn) "⚡ POWER — OFF" else "⚡ POWER — ON"
                    setBackgroundColor(if (isOn) Color.parseColor("#CC0000") else Color.parseColor("#00AA00"))

                    AudioWrapper.setPower(!isOn)
                    if (!isOn) AudioWrapper.nativeStart() else AudioWrapper.nativeStop()
                }
            })
        })
    }
}
