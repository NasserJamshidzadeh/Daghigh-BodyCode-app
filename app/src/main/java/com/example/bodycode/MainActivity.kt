package com.example.bodycode

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ساخت لایه اصلی (LinearLayout عمودی)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 17, 30)) // رنگ پس‌زمینه تیره
        }

        // ساخت هدر (TextView)
        val header = TextView(this).apply {
            text = "بادی کد و ایموشن کد دقیق"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(13, 27, 44)) // رنگ هدر (کمی روشن‌تر از پس‌زمینه)
            setPadding(0, 30, 0, 30) // فاصله داخلی برای زیبایی
        }
        rootLayout.addView(header, LinearLayout.LayoutParams(-1, -2))

        // ساخت WebView
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            setBackgroundColor(Color.WHITE)
        }

        // تنظیمات WebViewClient
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

        // اضافه کردن WebView به لایه اصلی با وزن 1 (برای پر کردن فضای باقی‌مانده)
        rootLayout.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(rootLayout)

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
