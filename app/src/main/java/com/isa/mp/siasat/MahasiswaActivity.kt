package com.isa.mp.siasat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.isa.mp.siasat.databinding.ActivityMahasiswaBinding

class MahasiswaActivity : BaseAuthenticatedActivity() {
    private lateinit var binding: ActivityMahasiswaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    override fun isValidRole(): Boolean = role == "mahasiswa"

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Dashboard Mahasiswa"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 