package com.sayam.qrcode

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
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
     var mAdManagerInterstitialAd:InterstitialAd? = null
     var mInterstitialAd:InterstitialAd? = null
     private lateinit var share:AppCompatButton
     val code: Int = 123
     private lateinit var scan:AppCompatButton
     private lateinit var outputStream: OutputStream
     private  lateinit var mAdView:AdView
     private lateinit var bitmap:Bitmap
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
//        bitmapDrawable = qrImage.drawable as BitmapDrawable
        bitmap = qrImage.drawable.toBitmap()
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
        share.setOnClickListener{
            mInterstitialAd!!.show(this@QrMakkerActivity)
        }
        save.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this@QrMakkerActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                mAdManagerInterstitialAd!!.show(this@QrMakkerActivity)
            }else{
                ActivityCompat.requestPermissions(this@QrMakkerActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),code)
            }
        }
        scan.setOnClickListener {
            val intent = Intent(this@QrMakkerActivity,QrScannerActivity::class.java)
            startActivity(intent)
        }

        // InterstitialAd to save
        InterstitialAd.load(this,getString(R.string.Inter_Ad_Unit_Id_Save),adRequest,object:InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Toast.makeText(this@QrMakkerActivity, "Error $adError",Toast.LENGTH_LONG).show()
                Log.d("TAG", adError.toString())
                mAdManagerInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("TAG", "Ad was loaded.")
                mAdManagerInterstitialAd = interstitialAd
                mAdManagerInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Toast.makeText(this@QrMakkerActivity, "Ad was Clicked.",Toast.LENGTH_LONG).show()
                        Log.d("TAG", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        Toast.makeText(this@QrMakkerActivity, "Ad dismissed full screen.",Toast.LENGTH_LONG).show()
                        Log.d("TAG", "Ad dismissed fullscreen content.")
                        mAdManagerInterstitialAd = null
                       // Save the image
                        saveImage()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        Log.e("TAG", "Ad failed to show fullscreen content.")
                        mAdManagerInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("TAG", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("TAG", "Ad showed fullscreen content.")
                    }
                }
            }
        })

        // InterstitialAd for share
        InterstitialAd.load(this,getString(R.string.Inter_Ad_Unit_Id_Share),adRequest,object:InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Toast.makeText(this@QrMakkerActivity, "Error $adError",Toast.LENGTH_LONG).show()
                Log.d("TAG", adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("TAG", "Ad was loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Toast.makeText(this@QrMakkerActivity, "Ad was clicked.",Toast.LENGTH_LONG).show()
                        Log.d("TAG", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        Log.d("TAG", "Ad dismissed fullscreen content.")
                        mInterstitialAd = null
                        // Share the image
                        shareImage(bitmap,this@QrMakkerActivity)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        Log.e("TAG", "Ad failed to show fullscreen content.")
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("TAG", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("TAG", "Ad showed fullscreen content.")
                    }
                }
            }
        })
        // Ad Functions
        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                super.onAdFailedToLoad(adError)
                mAdView.loadAd(adRequest)
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }

            override fun onAdLoaded() {
               super.onAdLoaded()
            }

            override fun onAdOpened() {
                super.onAdOpened()
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
        val file = File(context.externalCacheDir, "qrcode.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        val fileProviderUri = FileProvider.getUriForFile(applicationContext.applicationContext,
            "com.sayam.qrcode" + ".provider", file,)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, fileProviderUri)
            type = "image/png"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)

        if (shareIntent.resolveActivity(context.packageManager) != null)
            context.startActivity(shareIntent)
         else
            Toast.makeText(context, "Error Occurred Please Try Again", Toast.LENGTH_SHORT).show()
    }
}