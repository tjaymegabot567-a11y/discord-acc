package com.apexlogistics.app2x9n

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showText("STEP 1: onCreate ran")

        try {
            showText(
                "STEP 2: about to touch resources\n\n" +
                "App name: " + getString(R.string.app_name)
            )
        } catch (e: Throwable) {
            showText("FAILED at STEP 2:\n\n${e.javaClass.name}\n\n${e.message}\n\n${e.stackTrace.take(10).joinToString("\n")}")
            return
        }

        try {
            val prefs = getSharedPreferences("apex_logistics_2x9n", MODE_PRIVATE)
            val c = prefs.all.size
            showText("STEP 3: prefs OK, keys=$c\n\nApp launches fine.\n\nAll good.")
        } catch (e: Throwable) {
            showText("FAILED at STEP 3:\n\n${e.javaClass.name}\n\n${e.message}\n\n${e.stackTrace.take(10).joinToString("\n")}")
        }
    }

    private fun showText(s: String) {
        val tv = TextView(this).apply {
            text = s
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#14110F"))
            setPadding(40, 100, 40, 40)
        }
        val sv = ScrollView(this).apply { addView(tv) }
        setContentView(sv)
    }
}
