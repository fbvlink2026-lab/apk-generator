package com.martodosko.guitarfx

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : Activity() {
    companion object {
        const val CURRENT_VERSION = "v1.0.0"
        const val LATEST_VERSION = "v1.0.1"
        const val HAS_UPDATE = LATEST_VERSION != CURRENT_VERSION
        private const val PERMISSION_REQ = 101
    }

    private lateinit var mainLayout: LinearLayout
    private var isPowerOn = false
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val knobValues = mutableMapOf(
        "Master Volume" to 0.75f,
        "Distortion" to 0.50f,
        "Reverb" to 0.30f,
        "Delay" to 0.20f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AudioWrapper.nativeInit()
        requestAudioPermission()
        showLoadingScreen()
    }

    private fun showLoadingScreen() {
        val loading = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            gravity = Gravity.CENTER
        }
        loading.addView(TextView(this).apply {
            text = "🎸 Guitar FX"
            textSize = 36f
            setTextColor(Color.parseColor("#FFD700"))
            setPadding(0, 0, 0, 80)
        })
        loading.addView(TextView(this).apply {
            text = "Created and developed by MartoDosko"
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(0, 0, 0, 8)
        })
        loading.addView(TextView(this).apply {
            text = "© Copyright 2026"
            textSize = 12f
            setTextColor(Color.parseColor("#777777"))
        })
        loading.addView(TextView(this).apply {
            text = "⚡ Low-Latency C++ Oboe Engine — ~15ms"
            textSize = 11f
            setTextColor(Color.parseColor("#00FF9D"))
            setPadding(0, 40, 0, 0)
        })
        setContentView(loading)
        mainScope.launch { delay(3000); showUpdateScreen() }
    }

    private fun showUpdateScreen() {
        val layout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F3460"))
            setPadding(64, 48, 64, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        card.addView(TextView(this).apply {
            text = "🔄 Guitar FX — Update"
            textSize = 22f
            setTextColor(Color.parseColor("#FFD700"))
            setPadding(0, 0, 0, 32)
        })
        card.addView(TextView(this).apply {
            text = "Kasalukuyang Bersyon: $CURRENT_VERSION"
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(0, 0, 0, 16)
        })
        if (HAS_UPDATE) {
            card.addView(TextView(this).apply {
                text = "✅ May Bagong Bersyon! $LATEST_VERSION"
                textSize = 16f
                setTextColor(Color.parseColor("#FFD700"))
                setPadding(0, 0, 0, 24)
            })
            card.addView(Button(this).apply {
                text = "⬇️ I-update Ngayon"
                setBackgroundColor(Color.parseColor("#00C853"))
                setTextColor(Color.WHITE)
                textSize = 16f
                setOnClickListener { showMainMixer() }
            })
        } else {
            card.addView(TextView(this).apply {
                text = "✅ Nasa pinakabagong bersyon na!"
                textSize = 16f
                setTextColor(Color.parseColor("#00FF9D"))
                setPadding(0, 0, 0, 32)
            })
            card.addView(Button(this).apply {
                text = "✅ Pumunta sa Aplikasyon"
                setBackgroundColor(Color.parseColor("#FFD700"))
                setTextColor(Color.BLACK)
                textSize = 16f
                setOnClickListener { showMainMixer() }
            })
        }
        layout.addView(card)
        setContentView(layout)
    }

    private fun showMainMixer() {
        mainLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            setPadding(32, 32, 32, 32)
        }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            isFillViewport = true
        }
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        
        container.addView(TextView(this).apply {
            text = "🎚️ Pangunahing Mixer"
            textSize = 24f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        })
        container.addView(TextView(this).apply {
            text = "⚡ Low-Latency C++ Engine — ~15ms katulad ng Tonebridge"
            textSize = 11f
            setTextColor(Color.parseColor("#00FF9D"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        val powerBtn = Button(this).apply {
            text = "⚡ PANGUNAHING POWER — OFF"
            setBackgroundColor(Color.parseColor("#CC0000"))
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(0, 24, 0, 24)
            setOnClickListener {
                isPowerOn = !isPowerOn
                AudioWrapper.nativeSetPower(isPowerOn)
                text = if (isPowerOn) "⚡ PANGUNAHING POWER — ON" else "⚡ PANGUNAHING POWER — OFF"
                setBackgroundColor(Color.parseColor(if (isPowerOn) "#00C853" else "#CC0000"))
                if (isPowerOn) AudioWrapper.nativeStart()
            }
        }
        container.addView(powerBtn)

        listOf("Master Volume", "Distortion", "Reverb", "Delay").forEach { name ->
            val panel = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#16213E"))
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 16, 0, 0) }
            }
            val labelRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val label = TextView(this).apply {
                text = name
                textSize = 14f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val value = TextView(this).apply {
                text = "${(knobValues[name]!! * 100).toInt()}%"
                textSize = 13f
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.parseColor("#FFD700"))
                setPadding(24, 6, 24, 6)
            }
            labelRow.addView(label)
            labelRow.addView(value)

            val seekBar = SeekBar(this).apply {
                progress = (knobValues[name]!! * 100).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                        val f = p / 100f
                        knobValues[name] = f
                        value.text = "$p%"
                        when(name) {
                            "Master Volume" -> AudioWrapper.nativeSetVolume(f)
                            "Distortion" -> AudioWrapper.nativeSetDistortion(f)
                            "Reverb" -> AudioWrapper.nativeSetReverb(f)
                            "Delay" -> AudioWrapper.nativeSetDelay(f)
                        }
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            panel.addView(labelRow)
            panel.addView(seekBar)
            container.addView(panel)
        }

        scroll.addView(container)
        mainLayout.addView(scroll)
        setContentView(mainLayout)
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.MODIFY_AUDIO_SETTINGS),
                PERMISSION_REQ
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "✅ Audio — Pinayagan! Mababang latency!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioWrapper.nativeStop()
        mainScope.cancel()
    }
}
