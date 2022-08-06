package com.sayam.qrcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private lateinit var qrImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        qrImage = findViewById(R.id.iv_qr)
        qrImage.alpha = 0f
        qrImage.animate().setDuration(1500).alpha(1f).withEndAction {
            val i: Intent = Intent(this,QrMakkerActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}