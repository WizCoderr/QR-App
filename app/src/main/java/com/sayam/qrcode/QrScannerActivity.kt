package com.sayam.qrcode

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*
import com.karumi.dexter.Dexter.withContext
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class QrScannerActivity : AppCompatActivity() {
    private lateinit var scannerView: CodeScannerView
    private lateinit var scanner: CodeScanner
    private lateinit var resultData: TextView
    private lateinit var result: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        init()
        scanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                resultData.text = it.text
                result = resultData.text.toString()
            }
        }
        scanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(
                    this@QrScannerActivity,
                    "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            scannerView.setOnClickListener { scanner.startPreview() }

        }
        val openLink:Button = findViewById(R.id.open_link)
        openLink.setOnClickListener {
            if(result.isEmpty())
                Toast.makeText(
                    this@QrScannerActivity,
                    "Please Scan a qr Code",
                    Toast.LENGTH_LONG
                ).show()
            else
                openInChrome(result)
        }
    }

    private fun openInChrome(result: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
    }

    override fun onResume() {
        super.onResume()
        scanner.startPreview()
        requestForCamara()
    }

    private fun requestForCamara() = withContext(this)
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
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {}
        })
        .check()

    private fun init() {
        scannerView = findViewById(R.id.scanner_view)
        scanner = CodeScanner(this, scannerView)
        resultData = findViewById(R.id.result)
        scanner.camera = CodeScanner.CAMERA_BACK
        scanner.formats = CodeScanner.ALL_FORMATS
        scanner.autoFocusMode = AutoFocusMode.SAFE
        scanner.scanMode = ScanMode.SINGLE
        scanner.isAutoFocusEnabled = true
        scanner.isFlashEnabled = true
        result = String()
    }
}