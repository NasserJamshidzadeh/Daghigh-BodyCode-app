package com.example.bodycode

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var web: WebView

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var pendingWebPermission: PermissionRequest? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1002
        private const val FLASK_ORIGIN = "http://127.0.0.1:5000"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 17, 30))
        }

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

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            setBackgroundColor(Color.WHITE)
        }

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
                            <p>لطفاً مطمئن شوید سرور Flask در حال اجراست.</p>
                            <p>آدرس: $FLASK_ORIGIN</p>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        }

        web.webChromeClient = object : WebChromeClient() {

            // انتخاب فایل
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback

                if (filePathCallback == null || fileChooserParams == null) {
                    fileChooserCallback = null
                    return false
                }

                return try {
                    val intent = fileChooserParams.createIntent()

                    startActivityForResult(
                        intent,
                        FILE_CHOOSER_REQUEST_CODE
                    )

                    true
                } catch (e: ActivityNotFoundException) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    true
                } catch (e: Exception) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    true
                }
            }

            // مدیریت درخواست میکروفون صفحه وب
            override fun onPermissionRequest(request: PermissionRequest) {

                runOnUiThread {

                    // فقط به صفحه اصلی Flask اجازه می‌دهیم
                    val origin = request.origin

                    val trustedOrigin =
                        origin.scheme == "http" &&
                        origin.host == "127.0.0.1" &&
                        (origin.port == 5000 || origin.port == -1)

                    if (!trustedOrigin) {
                        request.deny()
                        return@runOnUiThread
                    }

                    // فقط درخواست ضبط صدا مجاز است
                    val audioResources = request.resources.filter {
                        it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                    }

                    if (audioResources.isEmpty()) {
                        request.deny()
                        return@runOnUiThread
                    }

                    if (
                        checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        request.grant(
                            audioResources.toTypedArray()
                        )
                    } else {
                        // نگه‌داشتن درخواست تا پاسخ کاربر
                        pendingWebPermission?.deny()
                        pendingWebPermission = request

                        requestPermissions(
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            AUDIO_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }

            override fun onPermissionRequestCanceled(
                request: PermissionRequest
            ) {
                if (pendingWebPermission === request) {
                    pendingWebPermission = null
                }
            }
        }

        rootLayout.addView(
            web,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(rootLayout)

        web.loadUrl(FLASK_ORIGIN)
    }

    // دریافت نتیجه انتخاب فایل
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
                val results =
                    if (resultCode == Activity.RESULT_OK) {
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

    // دریافت پاسخ مجوز میکروفون
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            val request = pendingWebPermission
            pendingWebPermission = null

            if (request != null) {
                val granted =
                    grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (
                    granted &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    request.grant(
                        arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                    )
                } else {
                    request.deny()
                }
            }
        }
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
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null

        pendingWebPermission?.deny()
        pendingWebPermission = null

        if (::web.isInitialized) {
            web.destroy()
        }

        super.onDestroy()
    }
}
