package com.example.trello.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERS: String = "users"

    const val BOARDS: String = "boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO: String = "assignedTo"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "board_detail"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    const val TASK_MASTER_PREFERENCES = "TaskMasterPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String =
        "AAAAcH8P7C4:APA91bHBE8Ngxr3hppK_QOgKIEsUTKA0S4zz2EROke-YCfuKdz9YHBSQvw7mQ7W4nok0F9RGmH-8lKTspJXtjeqAV80qY64FKhXd5BHgXnm9pSlmN5kjKT7M6rDfYHuRlS-E3FyO36lz"
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"
    const val FCM_KEY_REGISTRATION_IDS: String = "registration ids"
    const val FCM_KEY_IS_DUE_DATE_NOTIFICATION: String = "due date notification"
    const val FCM_KEY_BOARD_ID: String = "fcm board id"
    const val FCM_KEY_TASK_ID: String = "fcm task id"
    const val FCM_KEY_CARD_ID: String = "fcm card id"
    const val TASK_ID: String = "task id"
    const val CARD_ID: String = "card id"

    fun showImageChooser(activity: Activity) {
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}