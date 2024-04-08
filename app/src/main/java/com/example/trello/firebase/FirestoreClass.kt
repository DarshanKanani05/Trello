package com.example.trello.firebase

import android.util.Log
import com.example.trello.activities.SignInActivity
import com.example.trello.activities.SignUpActivity
import com.example.trello.models.User
import com.example.trello.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(
            userInfo,
            SetOptions.merge()
        ).addOnSuccessListener {
            activity.userRegisteredSuccess()
        }.addOnFailureListener {
            Log.e(
                activity.javaClass.simpleName,
                "Error writing documentation"
            )
        }
    }

    fun signInUser(activity: SignInActivity) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null)
                    activity.signInSuccess(loggedInUser)
            }.addOnFailureListener {
                Log.e(
                    "SignInUser",
                    "Error writing documentation"
                )
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
}