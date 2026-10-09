package com.eni.discordcreator

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class RegisterActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var statusBar: TextView
    private lateinit var inbox: MailTm.Inbox
    private lateinit var password: String
    private lateinit var username: String

    private var saved = false
    private var verifyPolling = false
    private var submitClicked = false
    private var captchaAttempts = 0

    private val ui = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        password = Generator.password()
        username = Generator.username()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        statusBar = TextView(this).apply {
            text = "starting…"
            textSize = 13f
            setPadding(40, 50, 40, 30)
            setBackgroundColor(0xFF1D1A17.toInt())
            setTextColor(0xFFC9A882.toInt())
        }

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        root.addView(statusBar)
        root.addView(webView)
        setContentView(root)

        setupWebView()

        setStatus("creating inbox…")
        thread {
            try {
                inbox = MailTm.createInbox()
                ui.post {
                    setStatus("inbox: ${inbox.address}")
                    webView.loadUrl("https://discord.com/register")
                }
            } catch (e: Exception) {
                ui.post {
                    setStatus("mail.tm failed: ${e.message}")
                    Toast.makeText(
                        this@RegisterActivity,
                        "mail.tm failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun setStatus(s: String) {
        ui.post { statusBar.text = s }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url == null) return
                if (url.contains("/channels/@me") || url.contains("discord.com/app")) {
                    if (!saved) {
                        saved = true
                        Prefs.addAccount(
                            this@RegisterActivity,
                            inbox.address,
                            password,
                            username
                        )
                        setStatus("account created")
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created!",
                            Toast.LENGTH_LONG
                        ).show()
                        ui.postDelayed({ finish() }, 1500)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url == null) return
                if (url.contains("discord.com/register")) {
                    setStatus("filling form…")
                    injectAutofill()
                }
                if (url.contains("click.discord.com") || url.contains("discord.com/verify")) {
                    setStatus("verifying…")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                val msg = m.message()
                when {
                    msg.contains("ENI_FORM_FILLED") -> setStatus("form filled")
                    msg.contains("ENI_SUBMIT_CLICKED") -> {
                        if (!submitClicked) {
                            submitClicked = true
                            setStatus("submitted, polling email…")
                            startVerifyPolling()
                        }
                    }
                    msg.contains("ENI_CAPTCHA_PRESENT") ->
                        setStatus("captcha present — tap it")
                    msg.contains("ENI_CAPTCHA_SOLVED") ->
                        setStatus("captcha solved")
                }
                return true
            }
        }
    }

    private fun injectAutofill() {
        val (month, day, year) = Generator.dob()
        val js = """
            (function() {
                if (window.__eni_installed) return;
                window.__eni_installed = true;

                const EMAIL = '${inbox.address}';
                const USER  = '$username';
                const PASS  = '$password';
                const M = '$month';
                const D = '$day';
                const Y = '$year';

                function log(m) { console.log(m); }

                function setVal(el, v) {
                    if (!el) return;
                    const setter = Object.getOwnPropertyDescriptor(
                        el.constructor.prototype, 'value'
                    ).set;
                    setter.call(el, v);
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                }

                let tries = 0;
                const fillTimer = setInterval(() => {
                    tries++;
                    const e = document.querySelector('input[name="email"]');
                    const g = document.querySelector('input[name="global_name"]');
                    const u = document.querySelector('input[name="username"]');
                    const p = document.querySelector('input[name="password"]');

                    if (e && u && p) {
                        setVal(e, EMAIL);
                        setVal(g, USER);
                        setVal(u, USER);
                        setVal(p, PASS);

                        fillDob('Month', M);
                        fillDob('Day', D);
                        fillDob('Year', Y);

                        const boxes = document.querySelectorAll('input[type="checkbox"]');
                        for (const b of boxes) {
                            if (b.offsetParent !== null && !b.checked) {
                                b.click();
                                break;
                            }
                        }

                        log('ENI_FORM_FILLED');
                        clearInterval(fillTimer);
                        hookSubmit();
                        detectCaptcha();
                    }
                    if (tries > 60) clearInterval(fillTimer);
                }, 500);

                function fillDob(label, val) {
                    const el = document.querySelector('div[aria-label="' + label + '"]');
                    if (!el || el.dataset.done) return;
                    el.dataset.done = '1';
                    el.click();
                    setTimeout(() => {
                        const opts = document.querySelectorAll('div[role="option"]');
                        for (const o of opts) {
                            if (o.textContent.trim() === val) { o.click(); break; }
                        }
                    }, 300);
                }

                function hookSubmit() {
                    const btn = document.querySelector('button[type="submit"]');
                    if (!btn || btn.dataset.hooked) return;
                    btn.dataset.hooked = '1';
                    btn.addEventListener('click', () => { log('ENI_SUBMIT_CLICKED'); });
                    const form = btn.closest('form');
                    if (form && !form.dataset.hooked) {
                        form.dataset.hooked = '1';
                        form.addEventListener('submit', () => { log('ENI_SUBMIT_CLICKED'); });
                    }
                }

                function detectCaptcha() {
                    let seen = false;
                    const check = setInterval(() => {
                        const frame = document.querySelector('iframe[src*="hcaptcha"]');
                        if (frame && !seen) {
                            seen = true;
                            log('ENI_CAPTCHA_PRESENT');
                        }
                        const ta = document.querySelector('textarea[name="h-captcha-response"]');
                        if (ta && ta.value && ta.value.length > 20) {
                            log('ENI_CAPTCHA_SOLVED');
                            clearInterval(check);
                        }
                    }, 1000);
                }

                window.__eni_getSitekey = function() {
                    const f = document.querySelector('iframe[src*="hcaptcha"]');
                    if (!f) return null;
                    const m = f.src.match(/sitekey=([^&]+)/);
                    return m ? decodeURIComponent(m[1]) : null;
                };

                window.__eni_injectToken = function(t) {
                    const setter = Object.getOwnPropertyDescriptor(
                        HTMLTextAreaElement.prototype, 'value'
                    ).set;
                    document.querySelectorAll(
                        'textarea[name="h-captcha-response"],' +
                        'textarea[name="g-recaptcha-response"]'
                    ).forEach(el => {
                        setter.call(el, t);
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                    });
                    log('ENI_CAPTCHA_SOLVED');
                };
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
        ui.postDelayed({ maybeAutoCaptcha() }, 3000)
    }

    private fun maybeAuto
