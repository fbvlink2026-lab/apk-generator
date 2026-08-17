package com.martodosko.guitarfx

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val CURRENT_VERSION = "v1.0.0"
        const val REQUEST_PERMISSIONS = 101
    }

    private lateinit var mainLayout: LinearLayout
    private var isPowerOn = false
    private val handler = Handler(Looper.getMainLooper())
    private val effects = mutableMapOf(
        "Distortion" to 0.5f,
        "Reverb" to 0.3f,
        "Delay" to 0.2f,
        "Volume" to 0.75f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingScreen()
    }

    private fun showLoadingScreen() {
        val loading = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
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
                text = "Created and Developed by\nMartoDosko © 2026"
                textSize = 16f
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = Gravity.CENTER
            })
        }
        setContentView(loading)

        handler.postDelayed({
            checkPermissions()
        }, 2500)
    }

    private fun checkPermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            initApp()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initApp()
    }

    private fun initApp() {
        AudioWrapper.nativeInit()
        showMainUI()
    }

    private fun showMainUI() {
        mainLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F0F4F8"))
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        mainLayout.addView(TextView(this).apply {
            text = "🎸 GuitarFX — $CURRENT_VERSION"
            textSize = 28f
            setTextColor(Color.parseColor("#1A202C"))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
        })

        // Power Button
        val powerBtn = Button(this).apply {
            text = "⚡ POWER — OFF"
            setBackgroundColor(Color.parseColor("#CC0000"))
            setTextColor(Color.WHITE)
            textSize = 18f
            setOnClickListener {
                isPowerOn = !isPowerOn
                if (isPowerOn) {
                    text = "⚡ POWER — ON"
                    setBackgroundColor(Color.parseColor("#00AA00"))
                    AudioWrapper.setPower(true)
                    AudioWrapper.nativeStart()
                } else {
                    text = "⚡ POWER — OFF"
                    setBackgroundColor(Color.parseColor("#CC0000"))
                    AudioWrapper.setPower(false)
                    AudioWrapper.nativeStop()
                }
            }
        }
        mainLayout.addView(powerBtn)

        // Effect Sliders
        listOf("Distortion", "Reverb", "Delay", "Volume").forEach { name ->
            val label = TextView(this).apply {
                text = "$name: ${(effects[name]!! * 100).toInt()}%"
                textSize = 16f
                setPadding(0, 24, 0, 8)
            }
            mainLayout.addView(label)

            val slider = SeekBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                max = 100
                progress = (effects[name]!! * 100).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                        val v = p / 100f
                        label.text = "$name: $p%"
                        effects[name] = v
                        when (name) {
                            "Distortion" -> AudioWrapper.setDistortion(v)
                            "Reverb" -> AudioWrapper.setReverb(v)
                            "Delay" -> AudioWrapper.setDelay(v)
                            "Volume" -> AudioWrapper.setVolume(v)
                        }
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            mainLayout.addView(slider)
        }

        setContentView(mainLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioWrapper.nativeStop()
    }
}
