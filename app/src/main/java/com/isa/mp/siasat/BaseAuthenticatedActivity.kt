package com.isa.mp.siasat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseAuthenticatedActivity : AppCompatActivity() {
    protected lateinit var prefs: SharedPreferences
    protected var userId: String = ""
    protected var nama: String = ""
    protected var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = getSharedPreferences("SIASAT", MODE_PRIVATE)
        
        // Check authentication
        if (!isAuthenticated()) {
            logout()
            return
        }

        // Load user data
        userId = prefs.getString("userId", "") ?: ""
        nama = prefs.getString("nama", "") ?: ""
        role = prefs.getString("role", "") ?: ""

        // Check role
        if (!isValidRole()) {
            logout()
            return
        }
    }

    override fun onStop() {
        super.onStop()
        // Clear session when app is stopped
        if (isFinishing) {
            prefs.edit().clear().apply()
        }
    }

    private fun isAuthenticated(): Boolean {
        return prefs.getString("userId", null) != null
    }

    protected abstract fun isValidRole(): Boolean

    protected fun logout() {
        prefs.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
} 