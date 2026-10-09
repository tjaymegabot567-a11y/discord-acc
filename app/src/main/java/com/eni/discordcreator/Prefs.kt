package com.eni.discordcreator

import android.content.Context

object Prefs {
    private const val NAME = "discord_creator"

    fun capmonsterKey(ctx: Context): String =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString("capmonster_key", "") ?: ""

    fun setCapmonsterKey(ctx: Context, v: String) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putString("capmonster_key", v).apply()

    fun accounts(ctx: Context): MutableList<String> {
        val raw = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString("accounts", "") ?: ""
        return if (raw.isBlank()) mutableListOf()
               else raw.split("\n").toMutableList()
    }

    fun addAccount(ctx: Context, email: String, pass: String, user: String) {
        val list = accounts(ctx)
        list.add("$email\t$pass\t$user")
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putString("accounts", list.joinToString("\n")).apply()
    }
}
