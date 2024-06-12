package com.example.trello.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trello.R
import com.example.trello.adapters.MemberListItemsAdapter
import com.example.trello.databinding.ActivityMembersBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.Board
import com.example.trello.models.User
import com.example.trello.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Suppress("DEPRECATION")
class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    lateinit var mAssignedMembersList: ArrayList<User>

    private lateinit var mRemovedByMemberName: String
    private var anyChangesMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMembersBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        binding.rvMembersList.adapter = adapter

        adapter.setOnDeleteClickListener(object : MemberListItemsAdapter.OnDeleteClickListener {
            override fun onDeleteClick(activity: MembersActivity, position: Int, user: User) {
                alertDialogForRemoveMember(user)
            }
        })

    }

    private fun alertDialogForRemoveMember(user: User) {
        val currentUserName = FirestoreClass().getCurrentUserName()
        val boardCreatorName = mBoardDetails.createdBy
        Log.e(
            "FirestoreClass",
            "alertDialogForRemoveMember: in starting fetching board creator name $boardCreatorName"
        )
        Log.e(
            "FirestoreClass",
            "alertDialogForRemoveMember:  fetching current user name $currentUserName"
        )
//        if (currentUserName == boardCreatorName) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_member,
                user.name
            )
        )

        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            Log.e(
                "FirestoreClass",
                "alertDialogForRemoveMember:  fetching board creator name $boardCreatorName"
            )
            deleteMember(user)
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
//        } else {
//            Toast.makeText(this, "Only the creator can remove other members", Toast.LENGTH_SHORT)
//                .show()
//        }
    }

    private fun deleteMember(user: User) {
        Log.e(
            "FirestoreClass",
            "deleteMember:  fetching board creator name ${mBoardDetails.createdBy.toString()}"
        )
        showProgressDialog(resources.getString(R.string.please_wait))
        mRemovedByMemberName = FirestoreClass().getCurrentUserName()
        val flag = FirestoreClass().removeMemberFromBoard(this, mBoardDetails, user)
        hideProgressDialog()
        if (flag) {
            sendNotificationToRemovedUser(user)
        }

    }


    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please Enter Email Address!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)

        anyChangesMade = true

        setupMembersList(mAssignedMembersList)
        val token = user.fcmToken
        val boardName = mBoardDetails.name
        val notificationType = "add"
        val notificationTitle = "Assigned to the board $boardName"
        val notificationBy = FirestoreClass().getCurrentUserName()
        val notificationMessage =
            "You have been assigned to the new Board $boardName by $notificationBy"

        val notificationTask = SendNotificationToUserAsyncTask(
            boardName,
            token,
            notificationType,
            notificationTitle,
            notificationMessage
//            notificationBy
        )
        notificationTask.execute()
    }

    private fun sendNotificationToRemovedUser(user: User) {
        val token = user.fcmToken
        val boardName = mBoardDetails.name
        val notificationType = "remove"
        val notificationTitle = "Removed from the board $boardName"
        val notificationBy = FirestoreClass().getCurrentUserName()
        val notificationMessage =
            "You have been removed from the Board $boardName by $notificationBy"

        val notificationTask = SendNotificationToUserAsyncTask(
            boardName,
            token,
            notificationType,
            notificationTitle,
            notificationMessage
//            notificationBy
        )
        notificationTask.execute()
    }

    private inner class SendNotificationToUserAsyncTask(
        val boardName: String,
        val token: String,
        val notificationType: String,
        val notificationTitle: String,
        val notificationMessage: String
    ) :
        AsyncTask<Any, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, notificationTitle)
                dataObject.put(Constants.FCM_KEY_MESSAGE, notificationMessage)
                dataObject.put(Constants.FCM_KEY_NOTIFICATION_TYPE, notificationType)
                dataObject.put(Constants.FCM_KEY_BOARD_NAME, boardName)
//                dataObject.put(Constants.FCM_KEY_BY, mAssignedMembersList[0].name.toString())
//                dataObject.put(Constants.FCM_KEY_BY, FirestoreClass().getCurrentUserName())
//                dataObject.put(Constants.FCM_KEY_BY, notificationBy)
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpsURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            Log.e("JSON Response Result", "onPostExecute: $result")
        }
    }
}