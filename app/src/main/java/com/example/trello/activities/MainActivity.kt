package com.example.trello.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.adapters.BoardItemsAdapter
import com.example.trello.databinding.ActivityMainBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.Board
import com.example.trello.models.User
import com.example.trello.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences =
            this.getSharedPreferences(Constants.TASK_MASTER_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        } else {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                updateFCMToken(instanceIdResult.token)

            }
        }

        FirestoreClass().loadUserData(this, true)

        var fabCreateBoard: FloatingActionButton = binding.root.findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        val rvBoardsList: RecyclerView = binding.root.findViewById(R.id.rv_boards_list)
        val tvNoBoardsAvailable: TextView =
            binding.root.findViewById(R.id.tv_no_boards_available)
        if (boardsList.size > 0) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.INVISIBLE

            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

//            boardsList.forEach { board ->
//                board.taskList.forEach { task ->
//                    task.cards.forEach { card ->
//                        val cardDueDate = card.dueDate
//                        val currentDate = System.currentTimeMillis()
//                        val oneDayBeforeDueDate = cardDueDate - (24 * 60 * 60 * 1000)
//
//                        if (currentDate in oneDayBeforeDueDate..cardDueDate) {
//                            val assignedToTokens = card.assignedTo.map { userId ->
//                                FirestoreClass().getUserFcmToken(userId)
//                            }
//
//                            SendDueDateNotificationAsyncTask(
//                                board.name,
//                                task.title,
//                                card.name,
//                                card.dueDate,
//                                assignedToTokens
//                            ).execute()
//                        }
//                    }
//                }
//            }
        } else {
            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        } else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.root.findViewById(R.id.nav_user_image))

        binding.root.findViewById<TextView>(R.id.tv_username).text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

//    private inner class SendDueDateNotificationAsyncTask(
//        val boardName: String,
//        val taskName: String,
//        val cardName: String,
//        val dueDate: Long,
//        val assignedToToken: List<String?>
//    ) : AsyncTask<Any, Void, String>() {
//        override fun onPreExecute() {
//            super.onPreExecute()
//            showProgressDialog(resources.getString(R.string.please_wait))
//        }
//
//        override fun doInBackground(vararg params: Any?): String {
//            var result: String
//            var connection: HttpURLConnection? = null
//            try {
//                val url = URL(Constants.FCM_BASE_URL)
//                connection = url.openConnection() as HttpURLConnection
//                connection.doInput = true
//                connection.doOutput = true
//                connection.instanceFollowRedirects = false
//                connection.requestMethod = "POST"
//
//                connection.setRequestProperty("Content-Type", "application/json")
//                connection.setRequestProperty("charset", "utf-8")
//                connection.setRequestProperty("Accept", "application/json")
//
//                connection.setRequestProperty(
//                    Constants.FCM_AUTHORIZATION,
//                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
//                )
//                connection.useCaches = false
//
//                val wr = DataOutputStream(connection.outputStream)
//                val jsonRequest = JSONObject()
//                val dataObject = JSONObject()
//                dataObject.put(Constants.FCM_KEY_TITLE, "Due Date Reminder")
//                dataObject.put(
//                    Constants.FCM_KEY_MESSAGE,
//                    "The Card $cardName in the task $taskName of board $boardName is due on ${
//                        formatDueDate(dueDate)
//                    }"
//                )
//                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
//                jsonRequest.put(Constants.FCM_KEY_REGISTRATION_IDS, JSONArray(assignedToToken))
//
//                wr.writeBytes(jsonRequest.toString())
//                wr.flush()
//                wr.close()
//                val httpResult: Int = connection.responseCode
//                if (httpResult == HttpsURLConnection.HTTP_OK) {
//                    val inputStream = connection.inputStream
//
//                    val reader = BufferedReader(InputStreamReader(inputStream))
//                    val sb = StringBuilder()
//                    var line: String?
//                    try {
//                        while (reader.readLine().also { line = it } != null) {
//                            sb.append(line + "\n")
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    } finally {
//                        try {
//                            inputStream.close()
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//                    }
//                    result = sb.toString()
//                } else {
//                    result = connection.responseMessage
//                }
//            } catch (e: SocketTimeoutException) {
//                result = "Connection Timeout"
//            } catch (e: Exception) {
//                result = "Error : " + e.message
//            } finally {
//                connection?.disconnect()
//            }
//            return result
//        }
//
//        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
//            hideProgressDialog()
//            Log.e("JSON Response Result", "onPostExecute: $result")
//        }
//
//        private fun formatDueDate(dueDate: Long): String {
//            val date = Date(dueDate)
//            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
//            return dateFormat.format(date)
//        }
//    }

}