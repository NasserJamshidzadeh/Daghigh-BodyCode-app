package com.example.bodycode

import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.loadsImagesAutomatically = true
        web.setBackgroundColor(android.graphics.Color.WHITE)

        web.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    view?.loadData(
                        "<html><body style='background:#07111E;color:white;text-align:center;padding-top:50px;font-family:sans-serif;'>" +
                        "<h2>خطا در اتصال به سرور</h2>" +
                        "<p>لطفاً مطمئن شوید که سرور پایتون (Flask) در حال اجراست.</p>" +
                        "<p>آدرس: http://127.0.0.1:5000</p>" +
                        "</body></html>",
                        "text/html", "UTF-8"
                    )
                }
            }
        }

        web.webChromeClient = WebChromeClient()
        setContentView(web)

        // آدرس سرور Flask خود را اینجا وارد کنید
        web.loadUrl("http://127.0.0.1:5000")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
