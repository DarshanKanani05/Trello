package com.example.trello.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trello.R
import com.example.trello.databinding.ActivityMyProfileBinding

class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActionBar()
    }

    private fun setupActionBar() {
        val toolBar: Toolbar = binding.root.findViewById(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}