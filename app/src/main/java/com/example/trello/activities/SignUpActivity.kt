package com.example.trello.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trello.R
import com.example.trello.databinding.ActivitySignUpBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar()
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this, "you have successfully registered the email address", Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun registerUser() {
        val name: String = binding.etName.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
//                        FirestoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }

            else -> {
                true
            }

        }
    }
}