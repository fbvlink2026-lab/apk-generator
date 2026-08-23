package com.martodosko.github.updater

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "✅ TAMA ANG DESTINASYON — Nasa Package Folder!", Toast.LENGTH_LONG).show()
    }
}