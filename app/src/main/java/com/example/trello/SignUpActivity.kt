package com.example.trello

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trello.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var bindnig: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        bindnig = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(bindnig.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActionBar()


    }

    private fun setupActionBar() {
        setSupportActionBar(bindnig.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        bindnig.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

    }
}