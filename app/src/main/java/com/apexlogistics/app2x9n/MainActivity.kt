package com.apexlogistics.app2x9n

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var countView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val scroll = ScrollView(this)

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 120, 60, 60)
            }

            val title = TextView(this).apply {
                text = "Apex Logistics"
                textSize = 28f
                setTextColor(Color.parseColor("#C9A882"))
                setPadding(0, 0, 0, 10)
            }
            layout.addView(title)

            val subtitle = TextView(this).apply {
                text = "2x9n"
                textSize = 12f
                setTextColor(Color.parseColor("#8A8177"))
                setPadding(0, 0, 0, 50)
            }
            layout.addView(subtitle)

            countView = TextView(this).apply {
                text = "Saved accounts: 0"
                textSize = 14f
                setTextColor(Color.parseColor("#E8E1D8"))
                setPadding(0, 0, 0, 60)
            }
            layout.addView(countView)

            val newBtn = Button(this).apply {
                text = "New Account"
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                }
            }
            layout.addView(newBtn)

            val accountsBtn = Button(this).apply {
                text = "Accounts"
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, AccountsActivity::class.java))
                }
            }
            layout.addView(accountsBtn)

            val settingsBtn = Button(this).apply {
                text = "Settings"
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
            }
            layout.addView(settingsBtn)

            scroll.addView(layout)
            setContentView(scroll)

        } catch (e: Throwable) {
            // If anything above crashes, show the error on screen instead of grey
            val errView = TextView(this).apply {
                text = "STARTUP ERROR:\n\n${e.javaClass.simpleName}\n\n${e.message}\n\n${e.stackTrace.take(5).joinToString("\n")}"
                textSize = 12f
                setTextColor(Color.RED)
                setPadding(40, 100, 40, 40)
                gravity = Gravity.START
            }
            val sv = ScrollView(this)
            sv.addView(errView)
            setContentView(sv)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            countView.text = "Saved accounts: ${Prefs.accounts(this).size}"
        } catch (_: Throwable) {
            // countView not initialized yet
        }
    }
}
