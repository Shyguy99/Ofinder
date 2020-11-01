package com.example.ofind

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    private var info: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val obj = intent.getStringExtra("object")
        info = findViewById<View>(R.id.web_info) as WebView
        info!!.settings.loadWithOverviewMode = true
        info!!.settings.useWideViewPort = true
        info!!.webViewClient = WebViewClient()
        info!!.loadUrl("https://en.wikipedia.org/wiki/$obj")
    }
}