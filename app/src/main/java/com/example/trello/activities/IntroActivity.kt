package com.example.trello.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trello.R
import com.example.trello.databinding.ActivityIntroBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvSwitchToLogin.setOnClickListener {
            showLoginForm()
        }

        binding.tvSwitchToSignup.setOnClickListener {
            showSignupForm()
        }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }

        binding.etPasswordSignup.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tvSignUpHint.visibility = View.VISIBLE
            } else {
                binding.tvSignUpHint.visibility = View.GONE
            }
        }

        binding.etPasswordSignIn.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tvSignInHint.visibility = View.VISIBLE
            } else {
                binding.tvSignInHint.visibility = View.GONE
            }
        }
    }

    private fun showLoginForm() {
        val slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        val slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        binding.llSignUpForm.startAnimation(slideOut)
        binding.llSignUpForm.visibility = View.GONE

        binding.llSignInForm.startAnimation(slideIn)
        binding.llSignInForm.visibility = View.VISIBLE
    }

    private fun showSignupForm() {
        val slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        val slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        binding.llSignInForm.startAnimation(slideOut)
        binding.llSignInForm.visibility = View.GONE

        binding.llSignUpForm.startAnimation(slideIn)
        binding.llSignUpForm.visibility = View.VISIBLE
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this, "you have successfully registered the email address", Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
    }

    private fun registerUser() {
        val name: String = binding.etNameSignup.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmailSignup.text.toString().trim { it <= ' ' }
        val password: String = binding.etPasswordSignup.text.toString().trim { it <= ' ' }

        if (validateFormSignUp(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateFormSignUp(name: String, email: String, password: String): Boolean {
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

    private fun signInRegisteredUser() {
        val email: String = binding.etEmailSignIn.text.toString().trim { it <= ' ' }
        val password: String = binding.etPasswordSignIn.text.toString().trim { it <= ' ' }

        if (validateFormSignIn(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Log.d("sign in", "signInWithEmail:success")
                        val user = auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Log.w("sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun validateFormSignIn(email: String, password: String): Boolean {
        return when {
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

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}