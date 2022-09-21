package com.sayam.qrcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.android.gms.ads.*

class MainActivity : AppCompatActivity() {
    private lateinit var qrImage: ImageView
    private lateinit var mAdView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        qrImage = findViewById(R.id.iv_qr)
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
            }
            override fun onAdClosed() {
                super.onAdClosed()
            }
            override fun onAdFailedToLoad(adError: LoadAdError) {
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
        qrImage.alpha = 0f
        qrImage.animate().setDuration(1500).alpha(1f).withEndAction {
            val i = Intent(this, QrMakkerActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}