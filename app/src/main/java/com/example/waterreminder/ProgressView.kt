package com.example.waterreminder

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val ASSET_BASE = "file:///android_asset/"

/**
 * Εμφανίζει τον κυκλικό δείκτη (assets/progress.html + .css + .js).
 * Τα δεδομένα περνούν μέσω του hash του URL, ώστε να μη χρειάζεται JavaScript μέσα στο Kotlin.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProgressView(drunkMl: Int, goalMl: Int, modifier: Modifier = Modifier) {
    val url = "${ASSET_BASE}progress.html#drunk=$drunkMl&goal=$goalMl"
    AndroidView(
        factory = { c ->
            WebView(c).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false      // δεν επηρεάζει τα assets της εφαρμογής
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    // Επιτρέπονται μόνο σελίδες από τα assets της ίδιας της εφαρμογής.
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        !request.url.toString().startsWith(ASSET_BASE)
                }
                loadUrl(url)
            }
        },
        update = { if (it.url != url) it.loadUrl(url) },
        modifier = modifier
    )
}
