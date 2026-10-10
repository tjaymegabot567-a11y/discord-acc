package com.apexlogistics.app2x9n

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AccountsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accounts = Prefs.accounts(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 100, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Apex Logistics · Accounts (${accounts.size})"
            textSize = 22f
            setPadding(0, 0, 0, 30)
        }
        layout.addView(title)

        for ((idx, line) in accounts.withIndex()) {
            val parts = line.split("\t")
            if (parts.size < 3) continue
            val card = TextView(this).apply {
                text = "${idx + 1}. ${parts[0]}\n   pass: ${parts[1]}\n   user: ${parts[2]}"
                textSize = 14f
                setPadding(20, 20, 20, 20)
                setOnClickListener {
                    val clip = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.setPrimaryClip(ClipData.newPlainText("account", line))
                    Toast.makeText(this@AccountsActivity, "Copied", Toast.LENGTH_SHORT).show()
                }
            }
            layout.addView(card)
        }

        val scroll = ScrollView(this).apply { addView(layout) }
        setContentView(scroll)
    }
}
