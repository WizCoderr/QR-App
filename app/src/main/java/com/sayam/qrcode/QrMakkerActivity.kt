package com.sayam.qrcode

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


@Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
class QrMakkerActivity : AppCompatActivity() {
     private lateinit var link:AppCompatEditText
     private lateinit var qrImage:AppCompatImageView
     private lateinit var generate:AppCompatButton
     private lateinit var save:AppCompatButton
     private lateinit var share:AppCompatButton
     val code: Int = 123
     private lateinit var scan:AppCompatButton
     private lateinit var outputStream: OutputStream
     private  lateinit var mAdView:AdView
     private lateinit var mBitmap :Bitmap
    private var saveInterstitialAd: InterstitialAd? = null
    private var shareInterstitialAd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_makker)
        //Ads
        mAdView = findViewById(R.id.ads)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        link = findViewById(R.id.et_link)
        qrImage = findViewById(R.id.qrImg)
        generate = findViewById(R.id.generate)
        save = findViewById(R.id.save)
        share = findViewById(R.id.share)
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
                    mBitmap = qrBitmap
                    qrImage.setImageBitmap(qrBitmap)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        share.setOnClickListener{
            if (shareInterstitialAd == null){
                shareImage(mBitmap, this@QrMakkerActivity)
            }else{
                shareInterstitialAd!!.show(this)
            }
        }
        save.setOnClickListener {
            if (saveInterstitialAd == null){
                if (ContextCompat.checkSelfPermission(
                        this@QrMakkerActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveImage()
                } else {
                    ActivityCompat.requestPermissions(
                        this@QrMakkerActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        code
                    )
                }
            }else{
                saveInterstitialAd!!.show(this)
            }
        }
        scan.setOnClickListener {
            val intent = Intent(this@QrMakkerActivity,QrScannerActivity::class.java)
            startActivity(intent)
        }

        // InterstitialAd to save
       InterstitialAd.load(this,getString(R.string.Inter_Ad_Unit_Id_Save),adRequest,object: InterstitialAdLoadCallback() {
           override fun onAdFailedToLoad(loadError: LoadAdError) {
               Log.d("TAG", "onAdFailedToLoad: Error: ${loadError.toString()}")
               saveInterstitialAd = null;
           }

           override fun onAdLoaded(interstitialAd: InterstitialAd) {
               saveInterstitialAd = interstitialAd
               saveInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {
                   override fun onAdDismissedFullScreenContent() {
                       if(ContextCompat.checkSelfPermission(this@QrMakkerActivity,
                               Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                           saveImage()
                       }else{
                           ActivityCompat.requestPermissions(this@QrMakkerActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),code)
                       }
                   }

                   override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                       saveInterstitialAd = null;
                       Log.d("TAG", "onAdFailedToShowFullScreenContent: Error: ${adError.message}")
                   }
               }
           }
       })
        // InterstitialAd to share
       InterstitialAd.load(this,getString(R.string.Inter_Ad_Unit_Id_Save),adRequest,object: InterstitialAdLoadCallback() {
           override fun onAdFailedToLoad(loadError: LoadAdError) {
               Log.d("TAG", "onAdFailedToLoad: Error: ${loadError.toString()}")
               shareInterstitialAd = null;
           }

           override fun onAdLoaded(interstitialAd: InterstitialAd) {
               shareInterstitialAd = interstitialAd
               shareInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {
                   override fun onAdDismissedFullScreenContent() {
                       shareImage(mBitmap,this@QrMakkerActivity)
                   }

                   override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                       shareInterstitialAd = null;
                       Log.d("TAG", "onAdFailedToShowFullScreenContent: Error: ${adError.message}")
                   }
               }
           }
       })
        // Ad Functions
        mAdView.adListener = object: AdListener() {
            override fun onAdFailedToLoad(adError : LoadAdError) {
                super.onAdFailedToLoad(adError)
                mAdView.loadAd(adRequest)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        if (requestCode == code){
            if (grantResults.isNotEmpty() &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage()
            }else{
                Toast.makeText( this@QrMakkerActivity, "Please provide the permission",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun saveImage() {
//        mAdManagerInterstitialAd!!.show(this@QrMakkerActivity)
        val contentResolver:ContentResolver = contentResolver
        val image:Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues()
        contentValues.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            System.currentTimeMillis().toString() + ".jpg"
        )
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/"
        )
        val uri: Uri = contentResolver.insert(image,contentValues)!!
        try {
            val bitmapDrawable = qrImage.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            outputStream = contentResolver.openOutputStream(uri)!!
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(
                this@QrMakkerActivity,
                "Qr Saved",
                Toast.LENGTH_LONG
            ).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun shareImage(bitmap:Bitmap,context:Context){
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val file = File(externalCacheDir.toString() + "/" + " QrCode.png")
        try{
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }catch (e:Exception){
            throw  RuntimeException(e)
        }
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                applicationContext.applicationContext,
                "com.sayam.qrcode" + ".provider", file
            ))
            type = "image/png"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Shae Via..")

        if (shareIntent.resolveActivity(context.packageManager) != null)
            context.startActivity(shareIntent)
         else Toast.makeText(context, "Error Occurred Please Try Again", Toast.LENGTH_SHORT).show()
    }
}