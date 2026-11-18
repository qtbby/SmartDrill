package com.example.smartdrill.data

import android.content.Context

// Small SharedPreferences wrapper for restoring the signed-in user's name and role.
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getBoolean("loggedIn", false)
    val name: String get() = prefs.getString("name", "") ?: ""
    val isAdmin: Boolean get() = prefs.getBoolean("isAdmin", false)

    // Persist the account identity and role after a successful login.
    fun save(name: String, isAdmin: Boolean) {
        prefs.edit()
            .putBoolean("loggedIn", true)
            .putString("name", name)
            .putBoolean("isAdmin", isAdmin)
            .apply()
    }

    // Remove all session keys on logout so the next launch requires authentication.
    fun clear() {
        prefs.edit().clear().apply()
    }
}