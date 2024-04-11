package com.example.trello.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.databinding.ActivityMainBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)
        FirestoreClass().signInUser(this)
    }

    private fun setupActionBar() {
        val toolBar: Toolbar = binding.root.findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolBar)
        toolBar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolBar.setNavigationOnClickListener {
            toggleDrawer()

        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

    }


    private fun handleOnBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }


    private fun onBackClick() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    doubleBackToExit()
                }
            }
        })
    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_my_profile -> {
                startActivity(Intent(this,MyProfileActivity::class.java))
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User) {
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.root.findViewById(R.id.nav_user_image))

        binding.root.findViewById<TextView>(R.id.tv_username).text = user.name
    }
}