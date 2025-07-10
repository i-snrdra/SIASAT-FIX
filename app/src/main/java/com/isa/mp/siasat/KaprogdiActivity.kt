package com.isa.mp.siasat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.isa.mp.siasat.databinding.ActivityKaprogdiBinding
import com.isa.mp.siasat.fragment.KelasFragment
import com.isa.mp.siasat.fragment.MataKuliahFragment

class KaprogdiActivity : BaseAuthenticatedActivity() {
    private lateinit var binding: ActivityKaprogdiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKaprogdiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
        setupFab()
    }

    override fun isValidRole(): Boolean = role == "kaprogdi"

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Dashboard Kaprogdi"
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> MataKuliahFragment()
                    1 -> KelasFragment()
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Mata Kuliah"
                1 -> "Kelas"
                else -> ""
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Fragment will handle the click
            val currentFragment = supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")
            when (currentFragment) {
                is MataKuliahFragment -> currentFragment.showAddDialog()
                is KelasFragment -> currentFragment.showEditDialog()
            }
        }
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