package com.apexlogistics.app2x9n

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 120, 60, 60)
        }

        val title = TextView(this).apply {
            text = "Apex Logistics · Settings"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }

        val label = TextView(this).apply {
            text = "CapMonster key (leave blank for manual captcha)"
            textSize = 14f
            setPadding(0, 0, 0, 12)
        }

        val keyField = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(Prefs.capmonsterKey(this@SettingsActivity))
            hint = "paste key here"
        }

        val save = Button(this).apply {
            text = "Save"
            setOnClickListener {
                Prefs.setCapmonsterKey(this@SettingsActivity, keyField.text.toString().trim())
                Toast.makeText(this@SettingsActivity, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        layout.addView(title)
        layout.addView(label)
        layout.addView(keyField)
        layout.addView(save)
        setContentView(layout)
    }
}
