package com.isa.mp.siasat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.mp.siasat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("SIASAT", MODE_PRIVATE)
        
        // Clear any existing session
        prefs.edit().clear().apply()
        
        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val userId = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInput(userId, password)) {
                performLogin(userId, password)
            }
        }
    }

    private fun validateInput(userId: String, password: String): Boolean {
        if (userId.isEmpty()) {
            showError("Masukkan NIM/Kode Dosen")
            return false
        }

        if (password.isEmpty()) {
            showError("Masukkan password")
            return false
        }

        // Validasi format NIM/Kode Dosen
        if (!isValidUserId(userId)) {
            showError("Format NIM/Kode Dosen tidak valid")
            return false
        }

        return true
    }

    private fun isValidUserId(userId: String): Boolean {
        // Format Dosen/Kaprogdi: 67xxx (5 digit)
        // Format Mahasiswa: xx2022xxx (9 digit)
        return when (userId.length) {
            5 -> userId.startsWith("67") // Dosen/Kaprogdi
            9 -> userId.substring(2, 6).matches(Regex("20\\d{2}")) // Mahasiswa
            else -> false
        }
    }

    private fun performLogin(userId: String, password: String) {
        showLoading(true)
        hideError()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val dbPassword = document.getString("password")
                    if (dbPassword == password) {
                        // Login berhasil
                        val role = document.getString("role") ?: ""
                        val nama = document.getString("nama") ?: ""
                        handleSuccessfulLogin(userId, nama, role)
                    } else {
                        showError("Password salah")
                    }
                } else {
                    showError("NIM/Kode Dosen tidak ditemukan")
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showError("Terjadi kesalahan: ${e.message}")
                showLoading(false)
            }
    }

    private fun handleSuccessfulLogin(userId: String, nama: String, role: String) {
        // Update lastLogin di Firestore
        db.collection("users").document(userId)
            .update("lastLogin", System.currentTimeMillis())

        // Simpan data user untuk sesi ini saja
        prefs.edit().apply {
            putString("userId", userId)
            putString("nama", nama)
            putString("role", role)
            apply()
        }

        // Navigate to appropriate screen
        navigateToHome()
        finish()
    }

    private fun navigateToHome() {
        val role = prefs.getString("role", "") ?: ""
        val intent = when (role) {
            "kaprogdi" -> Intent(this, KaprogdiActivity::class.java)
            "dosen" -> Intent(this, DosenActivity::class.java)
            "mahasiswa" -> Intent(this, MahasiswaActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
    }

    private fun showError(message: String) {
        binding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
            etUserId.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
        }
    }
} 