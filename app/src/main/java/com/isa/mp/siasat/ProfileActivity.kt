package com.isa.mp.siasat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.mp.siasat.databinding.ActivityProfileBinding
import com.isa.mp.siasat.databinding.DialogChangePasswordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : BaseAuthenticatedActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserData()
        setupButtons()
    }

    override fun isValidRole(): Boolean = true // Semua role bisa akses profile

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserData() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.apply {
                        tvNama.text = document.getString("nama")
                        tvId.text = userId
                        tvRole.text = when (role) {
                            "kaprogdi" -> "Kepala Program Studi"
                            "dosen" -> "Dosen"
                            "mahasiswa" -> "Mahasiswa"
                            else -> role
                        }
                        tvLastLogin.text = document.getLong("lastLogin")?.let {
                            dateFormat.format(Date(it))
                        } ?: "-"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupButtons() {
        binding.apply {
            btnChangePassword.setOnClickListener {
                showChangePasswordDialog()
            }

            btnLogout.setOnClickListener {
                logout()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.apply {
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val oldPassword = etOldPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (validatePassword(oldPassword, newPassword, confirmPassword, dialogBinding)) {
                    changePassword(oldPassword, newPassword, dialog)
                }
            }
        }

        dialog.show()
    }

    private fun validatePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
        dialogBinding: DialogChangePasswordBinding
    ): Boolean {
        if (oldPassword.isEmpty()) {
            showDialogError("Masukkan password lama", dialogBinding)
            return false
        }

        if (newPassword.isEmpty()) {
            showDialogError("Masukkan password baru", dialogBinding)
            return false
        }

        if (confirmPassword.isEmpty()) {
            showDialogError("Masukkan konfirmasi password", dialogBinding)
            return false
        }

        if (newPassword != confirmPassword) {
            showDialogError("Password baru dan konfirmasi tidak sama", dialogBinding)
            return false
        }

        if (newPassword.length < 6) {
            showDialogError("Password minimal 6 karakter", dialogBinding)
            return false
        }

        return true
    }

    private fun showDialogError(message: String, dialogBinding: DialogChangePasswordBinding) {
        dialogBinding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String, dialog: AlertDialog) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentPassword = document.getString("password")
                    if (currentPassword == oldPassword) {
                        // Update password
                        db.collection("users").document(userId)
                            .update("password", newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Password berhasil diubah",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Gagal mengubah password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Password lama salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Gagal memverifikasi password",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
} 