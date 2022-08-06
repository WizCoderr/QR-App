package com.sayam.qrcode

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.Dexter.*
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener

class QrScannerActivity : AppCompatActivity() {
    private lateinit var scannerView: CodeScannerView
    private lateinit var scanner: CodeScanner
    private lateinit var openLink: Button
    private lateinit var resultData: TextView
    private lateinit var result:String
    private lateinit var uri:Uri
    val  COMPILE_TIME_CONST = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        init()
        scanner.decodeCallback = DecodeCallback { result: Result ->
            runOnUiThread { resultData.text = result.toString() }
        }
        result = resultData.text.toString()
        scannerView.setOnClickListener {scanner.startPreview()}
        openLink.setOnClickListener {
            openUrl(result);
        }
    }
    private fun openUrl(result: String) {
        uri = Uri.parse(result)

    }


    override fun onResume() {
        super.onResume()
        requestForCamara()
    }

    private fun requestForCamara() = Dexter.withContext(this)
        .withPermission(Manifest.permission.CAMERA)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {
                scanner.startPreview()
            }

            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@QrScannerActivity,
                    "Camara permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: com.karumi.dexter.listener.PermissionRequest?,
                p1: PermissionToken?
            ) {}

            fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest?,
                permissionToken: PermissionToken
            ) {
                Log.d(TAG, "onPermissionRationaleShouldBeShown: "+ permissionRequest.toString())
                permissionToken.continuePermissionRequest()
            }
        })
        .check()

    private fun init() {
        openLink = findViewById(R.id.link_btn)
        scannerView = findViewById(R.id.scanner_view)
        scanner = CodeScanner(this, scannerView)
        resultData = findViewById(R.id.result)
    }
}