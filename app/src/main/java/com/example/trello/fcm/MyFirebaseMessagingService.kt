package com.example.trello.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.trello.R
import com.example.trello.activities.IntroActivity
import com.example.trello.activities.MainActivity
import com.example.trello.activities.MembersActivity
import com.example.trello.activities.TaskListActivity
import com.example.trello.firebase.FirestoreClass
import com.example.trello.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived: From ${remoteMessage.from} ")

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "onMessageReceived: Message Data Payload : ${remoteMessage.data}")

            //            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!
            val boardName = remoteMessage.data[Constants.FCM_KEY_BOARD_NAME]!!
            val notificationType = remoteMessage.data[Constants.FCM_KEY_NOTIFICATION_TYPE]!!
//            val notificationBy = remoteMessage.data[Constants.FCM_KEY_BY]!!
            //            val token = remoteMessage.data[Constants.FCM_KEY_TO]!!

            val getNotificationMessageResult =
                getNotificationMessage(message, boardName, notificationType)

            getNotificationMessageResult?.let { message ->
                val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
                sendNotification(title, message)
            }

            //            sendNotification(title, message, boardName, notificationType, token)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "onMessageReceived: Message Notification Body : ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, "onNewToken: Refreshed Token : $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        var sharedPreferences =
            this.getSharedPreferences(Constants.TASK_MASTER_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(
        title: String,
        message: String
    ) {
        val intent = if (FirestoreClass().getCurrentUserId().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Task Master",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun getNotificationMessage(
        message: String?,
        boardName: String?,
        notificationType: String
    ): String? {
        return when (notificationType) {
            "add" -> "You have been assigned to the new Board $boardName"
            "remove" -> "You have been removed from the Board $boardName"
            else -> message
        }
    }

//    private fun sendDueDateNotification(
//        title: String,
//        message: String,
//        boardId: String,
//        taskId: String,
//        cardId: String
//    ) {
//        val intent = Intent(this, TaskListActivity::class.java)
//        intent.putExtra(Constants.DOCUMENT_ID, boardId)
//        intent.putExtra(Constants.TASK_ID, taskId)
//        intent.putExtra(Constants.CARD_ID, cardId)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//        val channelId = this.resources.getString(R.string.default_notification_channel_id)
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_stat_ic_notification)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setAutoCancel(true)
//            .setSound(defaultSoundUri)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Channel Task Master",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//        notificationManager.notify(0, notificationBuilder.build())
//
//    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}