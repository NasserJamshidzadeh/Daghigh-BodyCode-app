package com.example.bodycode

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var web: WebView

    // دریافت نتیجه انتخاب فایل
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // لایه اصلی
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 17, 30))
        }

        // هدر برنامه
        val header = TextView(this).apply {
            text = "بادی کد و ایموشن کد دقیق"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(13, 27, 44))
            setPadding(0, 30, 0, 30)
        }

        rootLayout.addView(
            header,
            LinearLayout.LayoutParams(-1, -2)
        )

        // WebView
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            setBackgroundColor(Color.WHITE)
        }

        // مدیریت خطاهای اتصال
        web.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    view?.loadData(
                        """
                        <html>
                        <body style="
                            background:#07111E;
                            color:white;
                            text-align:center;
                            padding-top:50px;
                            font-family:sans-serif;
                        ">
                            <h2>خطا در اتصال به سرور</h2>
                            <p>لطفاً مطمئن شوید که سرور پایتون (Flask) در حال اجراست.</p>
                            <p>آدرس: http://127.0.0.1:5000</p>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        }

        // مدیریت انتخاب فایل در WebView
        web.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                // آزادسازی درخواست انتخاب فایل قبلی
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback

                if (filePathCallback == null || fileChooserParams == null) {
                    fileChooserCallback = null
                    return false
                }

                return try {
                    // ساخت Intent استاندارد انتخاب فایل
                    val intent = fileChooserParams.createIntent()

                    startActivityForResult(
                        intent,
                        FILE_CHOOSER_REQUEST_CODE
                    )

                    true

                } catch (e: ActivityNotFoundException) {

                    // اگر فایل‌پیکر در دسترس نباشد
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null

                    false

                } catch (e: Exception) {

                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null

                    false
                }
            }
        }

        // اضافه کردن WebView به صفحه
        rootLayout.addView(
            web,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(rootLayout)

        // آدرس سرور Flask
        web.loadUrl("http://127.0.0.1:5000")
    }

    // دریافت فایل انتخاب‌شده از اندروید
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {

            val callback = fileChooserCallback
            fileChooserCallback = null

            if (callback != null) {

                val results = if (resultCode == Activity.RESULT_OK) {
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                } else {
                    null
                }

                callback.onReceiveValue(results)
            }
        }
    }

    // مدیریت دکمه بازگشت
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        // آزادسازی درخواست احتمالی انتخاب فایل
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null

        if (::web.isInitialized) {
            web.destroy()
        }

        super.onDestroy()
    }
}
```
