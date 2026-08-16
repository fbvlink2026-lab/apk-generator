package com.guitarfx.app;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.*;
public class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.txt)).setText("✅ GuitarFX — Handang Gumamit!\nBersyon: 1.0.0");
    }
}
