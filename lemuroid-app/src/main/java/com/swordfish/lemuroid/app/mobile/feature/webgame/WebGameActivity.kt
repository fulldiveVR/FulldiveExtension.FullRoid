/*
 * WebGameActivity.kt
 *
 * Copyright (C) 2026 FullDive
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.mobile.feature.webgame

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.catalog.WebCatalogConfig
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject

/**
 * Plays one GameHub web game, fully offline: the game zip is downloaded + unpacked
 * (see [WebGameRepository]) and served from the fake `gamehub.local` host through
 * [WebView.shouldInterceptRequest]. External hosts are blocked; the shared loader
 * chain is served from the cached `/__cdn` bundle. Ported from the `gamehub/android`
 * prototype `GameActivity` (bucket/prod path only — no QA marking chrome).
 */
class WebGameActivity : RetrogradeComponentActivity() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private lateinit var loading: TextView
    private var slug: String = ""
    private var zipUrl: String = ""
    private var zipSha: String? = null
    private var orientation: String = "any"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        loading = TextView(this).apply { setTextColor(0xFFECEBF2.toInt()); textSize = 16f }
        root.addView(loading, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))

        web = WebView(this).apply {
            visibility = View.GONE
            settings.javaScriptEnabled = true
            // Persistent save data: localStorage/IndexedDB (DOM storage) + WebSQL. These
            // survive across launches as long as the origin is stable — and we now serve
            // over https (see loadGame) so the page is a secure context, which some games
            // require before they'll persist saves and which lets the browser treat the
            // storage as durable rather than best-effort/evictable.
            settings.domStorageEnabled = true
            @Suppress("DEPRECATION")
            settings.databaseEnabled = true
            // The page is https but intercepted local/CDN assets may be requested over
            // http; allow it (everything is served offline via shouldInterceptRequest).
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            // Some games persist progress via cookies (this = the WebView).
            android.webkit.CookieManager.getInstance().setAcceptCookie(true)
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = client()
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    Log.i("WebGameConsole", "[$slug] ${m.messageLevel()} ${m.message()} @${m.sourceId()}:${m.lineNumber()}")
                    return true
                }

                // Block every JS dialog so nothing interrupts the game — including error
                // alerts. We consume the event (return true) and auto-resolve so the page
                // keeps running instead of hanging on a dismissed dialog.
                override fun onJsAlert(v: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    result?.confirm()
                    return true
                }

                override fun onJsConfirm(v: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    result?.confirm() // treat as "OK" so game flows continue
                    return true
                }

                override fun onJsPrompt(
                    v: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?,
                ): Boolean {
                    result?.confirm(defaultValue ?: "")
                    return true
                }

                override fun onJsBeforeUnload(v: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    result?.confirm()
                    return true
                }
            }
        }
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        val close = closeButton()
        root.addView(close)
        setContentView(root)

        // Keep the close button clear of the status bar / notch so it stays tappable.
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            (close.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = bars.top + dp(6)
                rightMargin = bars.right + dp(6)
            }
            close.requestLayout()
            insets
        }

        play(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        play(intent)
    }

    private fun play(intent: Intent) {
        slug = intent.getStringExtra(EXTRA_SLUG) ?: run { finish(); return }
        zipUrl = intent.getStringExtra(EXTRA_ZIP_URL) ?: run { finish(); return }
        zipSha = intent.getStringExtra(EXTRA_ZIP_SHA)
        orientation = intent.getStringExtra(EXTRA_ORIENTATION) ?: "any"
        loadGame()
    }

    private fun recordPlayed(slug: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { retrogradeDb.gameDao().touchWebGamePlayed(slug, System.currentTimeMillis()) }
        }
    }

    private fun applyOrientation() {
        requestedOrientation = when (orientation) {
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
    }

    private fun loadGame() {
        applyOrientation()
        web.stopLoading(); web.loadUrl("about:blank")

        loading.text = getString(R.string.web_game_loading)
        loading.visibility = View.VISIBLE
        web.visibility = View.GONE

        val target = slug
        lifecycleScope.launch {
            val ok = WebGameRepository.ensureDownloaded(this@WebGameActivity, target, zipUrl, zipSha) { done, total ->
                val txt = if (total > 0) {
                    getString(R.string.web_game_loading_percent, done * 100 / total)
                } else {
                    getString(R.string.web_game_loading_mb, done / 1048576)
                }
                runOnUiThread { if (slug == target) loading.text = txt }
            }
            // Shared loader bundle (best-effort; non-Unity games don't need it).
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@WebGameActivity)
            WebGameRepository.ensureCdn(
                this@WebGameActivity,
                prefs.getString(WebCatalogConfig.PREF_CDN_URL, null),
                prefs.getString(WebCatalogConfig.PREF_CDN_SHA, null),
            )
            if (slug != target) return@launch
            if (ok) {
                loading.visibility = View.GONE
                web.visibility = View.VISIBLE
                web.loadUrl("https://$HOST/__play")
                // Record the launch so the game surfaces in Home's "Recent" chip.
                recordPlayed(target)
            } else {
                loading.text = getString(R.string.web_game_load_failed)
            }
        }
    }

    private fun closeButton(): Button {
        return Button(this).apply {
            text = "✕"
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(0xB30F1116.toInt()) }
            layoutParams = FrameLayout.LayoutParams(dp(44), dp(44), Gravity.TOP or Gravity.END).apply {
                setMargins(0, dp(6), dp(6), 0)
            }
            setOnClickListener { finish() }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // ---- WebView serving (offline) ------------------------------------------

    private val dir: File get() = WebGameRepository.gameDir(this, slug)

    private fun client() = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val h = request.url.host ?: return false
            return h != HOST && h != "localhost" && h != "127.0.0.1"
        }

        // We serve the game from the fake https host `gamehub.local` purely through
        // shouldInterceptRequest (no real TLS), so a cert error can only be the synthetic
        // one for our own offline host — proceed for it, block anything else.
        override fun onReceivedSslError(
            view: WebView,
            handler: android.webkit.SslErrorHandler,
            error: android.net.http.SslError,
        ) {
            val h = android.net.Uri.parse(error.url).host
            if (h == HOST || (h != null && sha256Hex(h) == CDN_HOST_HASH)) {
                handler.proceed()
            } else {
                handler.cancel()
            }
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            val url = request.url
            val host = url.host
            if (host != HOST) {
                if (host == "localhost" || host == "127.0.0.1") return null
                if (host != null && sha256Hex(host) == CDN_HOST_HASH) {
                    return serveCdn((url.path ?: "").removePrefix("/"))
                }
                return WebResourceResponse(
                    "text/plain", "utf-8", 404, "Blocked (offline)", emptyMap(), ByteArrayInputStream(ByteArray(0)),
                )
            }
            var p = url.path ?: "/"
            if (p == "/__play") {
                val html =
                    """<!doctype html><html><head><meta charset="utf-8">
                    |<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover">
                    |<style>html,body{margin:0;height:100%;background:#000;overflow:hidden}
                    |iframe{border:0;width:100vw;height:100vh;display:block}</style></head>
                    |<body><iframe src="/index.html" allow="autoplay;fullscreen;gamepad;accelerometer;gyroscope" allowfullscreen></iframe></body></html>""".trimMargin()
                return WebResourceResponse("text/html", "utf-8", ByteArrayInputStream(html.toByteArray()))
            }
            if (p.startsWith("/__cdn/")) return serveCdn(p.removePrefix("/__cdn/"))
            if (p.endsWith("/")) p += "index.html"
            val f = File(dir, p.removePrefix("/"))
            if (!f.exists() || f.isDirectory) {
                return WebResourceResponse(
                    "text/plain", "utf-8", 404, "Not Found", emptyMap(), ByteArrayInputStream(ByteArray(0)),
                )
            }
            val name = f.name.lowercase()
            if (name.endsWith(".html")) {
                var html = f.readText()
                html = if (HEAD_RE.containsMatchIn(html)) {
                    HEAD_RE.replace(html) { it.value + UNITY_SHIM }
                } else {
                    UNITY_SHIM + html
                }
                return WebResourceResponse("text/html", "utf-8", ByteArrayInputStream(html.toByteArray()))
            }
            // Compressed Unity payloads: Android WebView ignores Content-Encoding on
            // intercepted responses, so decompress here and serve plain bytes.
            if (name.endsWith("gz") || name.endsWith(".unityweb")) {
                val raw = f.readBytes()
                val isGzip = raw.size >= 2 && raw[0] == 0x1f.toByte() && raw[1] == 0x8b.toByte()
                val body = if (isGzip) java.util.zip.GZIPInputStream(raw.inputStream()).use { it.readBytes() } else raw
                val base = when {
                    name.endsWith(".unityweb") -> null
                    name.endsWith(".gz") -> name.removeSuffix(".gz")
                    else -> name.removeSuffix("gz")
                }
                val mime2 = if (base == null) "application/octet-stream" else mimeOf(base)
                return WebResourceResponse(mime2, null, 200, "OK", HashMap(), ByteArrayInputStream(body))
            }
            return WebResourceResponse(mimeOf(name), null, 200, "OK", HashMap(), f.inputStream())
        }
    }

    private fun serveCdn(rel: String): WebResourceResponse {
        val cf = File(WebGameRepository.cdnDir(this), rel)
        return if (!cf.exists() || cf.isDirectory) {
            WebResourceResponse("text/plain", "utf-8", 404, "not found", emptyMap(), ByteArrayInputStream(ByteArray(0)))
        } else {
            WebResourceResponse(mimeOf(cf.name.lowercase()), null, 200, "OK", HashMap(), cf.inputStream())
        }
    }

    private fun mimeOf(name: String): String {
        val ext = name.substringAfterLast('.', "")
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { return it }
        return when (ext) {
            "js", "mjs" -> "text/javascript"
            "wasm" -> "application/wasm"
            "data", "bin", "mem", "pck" -> "application/octet-stream"
            "json" -> "application/json"
            "html" -> "text/html"
            "css" -> "text/css"
            else -> "application/octet-stream"
        }
    }

    override fun onPause() {
        super.onPause()
        // Persist cookies to disk; localStorage/IndexedDB are flushed by the WebView's
        // storage subsystem when the page goes to the background.
        runCatching { android.webkit.CookieManager.getInstance().flush() }
    }

    override fun onDestroy() {
        runCatching { android.webkit.CookieManager.getInstance().flush() }
        web.destroy()
        super.onDestroy()
    }

    companion object {
        private const val HOST = "gamehub.local"
        private val HEAD_RE = Regex("(?i)<head[^>]*>")

        private const val EXTRA_SLUG = "slug"
        private const val EXTRA_ZIP_URL = "zipUrl"
        private const val EXTRA_ZIP_SHA = "zipSha"
        private const val EXTRA_ORIENTATION = "orientation"

        // sha256 of the external asset-CDN host, matched by hash so the vendor
        // name isn't embedded in the app.
        private const val CDN_HOST_HASH = "ad99135f33dc0e8ccf4fb9843e1c4959f9bd44bd16f0cd1026d808483aed798b"

        private fun sha256Hex(s: String): String =
            java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
                .joinToString("") { "%02x".format(it) }

        fun newIntent(
            context: Context,
            slug: String,
            zipUrl: String,
            zipSha: String?,
            orientation: String,
        ): Intent = Intent(context, WebGameActivity::class.java).apply {
            putExtra(EXTRA_SLUG, slug)
            putExtra(EXTRA_ZIP_URL, zipUrl)
            putExtra(EXTRA_ZIP_SHA, zipSha)
            putExtra(EXTRA_ORIENTATION, orientation)
        }

        // Injected into game HTML (parity with the prototype): reveal the game once
        // the canvas is up, auto-click the Unity mobile "Press OK" gate, and
        // neutralize sitelock redirects so the game keeps running offline.
        private const val UNITY_SHIM =
            // Block popups/dialogs in JS too (belt-and-suspenders with the native
            // WebChromeClient overrides): alert/print become no-ops so error alerts
            // never even reach the native layer.
            "<script>(function(){try{window.alert=function(){};window.print=function(){};}catch(e){}})();</script>" +
            "<script>(function(){function reveal(){var l=document.getElementById('loader'),g=document.getElementById('game-container');" +
            "var c=g&&g.querySelector('canvas');if(l&&g&&c&&c.width>1&&c.height>1){l.style.display='none';g.style.display='block';return true;}return false;}" +
            "var n=0,iv=setInterval(function(){if(reveal()||++n>240)clearInterval(iv);},500);})();</script>" +
            "<script>(function(){var n=0;function kill(){var t=(document.body&&document.body.innerText)||'';" +
            "if(!/supported on mobiles|continue anyway/i.test(t))return false;" +
            "var b=[].slice.call(document.querySelectorAll('button,input[type=button],input[type=submit]')).filter(function(x){return /^\\s*OK\\s*\$/i.test(x.textContent||x.value||'');})[0];" +
            "if(b){b.click();return true;}return false;}var iv=setInterval(function(){if(kill()||++n>200)clearInterval(iv);},250);})();</script>" +
            "<script>(function(){var oc=window.confirm;window.confirm=function(m){if(/supported on mobiles|continue anyway/i.test(String(m||'')))return true;return oc?oc.call(window,m):true;};})();</script>" +
            "<script>(function(){function bad(u){return /po\\.ki|sitelock/i.test(String(u||''));}" +
            "var o=window.open;window.open=function(u){return bad(u)?null:o.apply(window,arguments);};" +
            "try{var a=Location.prototype.assign;Location.prototype.assign=function(u){if(!bad(u))a.call(this,u);};}catch(e){}" +
            "try{var r=Location.prototype.replace;Location.prototype.replace=function(u){if(!bad(u))r.call(this,u);};}catch(e){}" +
            "try{var d=Object.getOwnPropertyDescriptor(Location.prototype,'href');Object.defineProperty(Location.prototype,'href',{configurable:true,get:function(){return d.get.call(this);},set:function(u){if(!bad(u))d.set.call(this,u);}});}catch(e){}" +
            "})();</script>"
    }
}
