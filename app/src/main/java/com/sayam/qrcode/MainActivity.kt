package com.sayam.qrcode

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*


class MainActivity : AppCompatActivity() {
    private lateinit var qrImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        MobileAds.initialize(this) {}
        qrImage = findViewById(R.id.iv_qr)
        startMainActivity()
    }
    private fun startMainActivity() {
        qrImage.alpha = 0f
        qrImage.animate().setDuration(1500).alpha(1f).withEndAction {
            val i = Intent(this, QrMakkerActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}