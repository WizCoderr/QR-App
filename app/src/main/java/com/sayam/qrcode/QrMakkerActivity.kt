package com.sayam.qrcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*

class QrMakkerActivity : AppCompatActivity() {
    private lateinit var link:EditText
    private lateinit var qrImage:ImageView
     private lateinit var generate:Button
     private lateinit var save:Button
     private val REQUEST_CODE:Int = 123
    private lateinit var scan:Button
    private lateinit var outputStream: OutputStream
    private lateinit var oldDrawable:Drawable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_makker)
        link = findViewById(R.id.et_link)
        qrImage = findViewById(R.id.qrImg)
        generate = findViewById(R.id.generate)
        save = findViewById(R.id.save)
        oldDrawable = qrImage.drawable
        scan = findViewById(R.id.scan)
        generate.setOnClickListener {
            val data = link.text.toString()
            if (data.isEmpty()) {
                link.error = "Enter the link"
                Toast.makeText(this, "Enter a url please", Toast.LENGTH_SHORT).show()
            } else {
                val encoder = QRGEncoder(data, null, QRGContents.Type.TEXT, 500)
                try {
                    val qrBitmap: Bitmap = encoder.bitmap
                    qrImage.setImageBitmap(qrBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        scan.setOnClickListener {
            val intent = Intent(this@QrMakkerActivity,QrScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

}