package com.example.trello.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trello.activities.CardDetailsActivity
import com.example.trello.activities.CreateBoardActivity
import com.example.trello.activities.IntroActivity
import com.example.trello.activities.MainActivity
import com.example.trello.activities.MembersActivity
import com.example.trello.activities.MyProfileActivity
import com.example.trello.activities.NotificationActivity
import com.example.trello.activities.TaskListActivity
import com.example.trello.models.Board
import com.example.trello.models.Card
import com.example.trello.models.User
import com.example.trello.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar
import java.util.Date

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()
    private val boardsListForDueDate: ArrayList<Board> = ArrayList()

    fun registerUser(activity: IntroActivity, userInfo: User) {
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

    fun getCurrentUserName(): String {
        var currentUserName = ""
        val user = mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    currentUserName = user?.name ?: ""
                    Log.e(
                        "FirestoreClass",
                        "getCurrentUserName:  fetching current user name $currentUserName"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreClass", "getCurrentUserName: Error fetching current user name ")
            }
        return currentUserName
    }

//    fun getUserFcmToken(userId: String): String? {
//        var userFcmToken: String? = null
//        val userDocumentRef = mFireStore.collection(Constants.USERS).document(userId)
//
//        val task = userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
//            if (documentSnapshot.exists()) {
//                val user = documentSnapshot.toObject(User::class.java)
//                userFcmToken = user?.fcmToken
//            }
//        }.addOnFailureListener { exception ->
//            Log.e("getUserFcmToken", "Error getting FCM token for user $userId", exception)
//        }
//        return userFcmToken
//    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId).get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    Log.i(activity.javaClass.simpleName, document.toString())
                    val board = document.toObject(Board::class.java)!!
                    if (board != null) {
                        board.documentId = document.id
                        activity.boardDetails(board)
                    } else {
                        activity.hideProgressDialog()
                    }
                } else {
                    activity.hideProgressDialog()
                }

//                Log.i(activity.javaClass.simpleName, document.toString())
//                val board = document.toObject(Board::class.java)!!
//                board.documentId = document.id
//                activity.boardDetails(board)

            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while opening a board.", e)

            }
    }

//    fun createBoard(activity: CreateBoardActivity, board: Board) {
//        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.e(activity.javaClass.simpleName, "Board Created Successfully!")
//                Toast.makeText(activity, "Board Created Successfully!", Toast.LENGTH_SHORT).show()
//                activity.boardCreatedSuccessfully()
//            }.addOnFailureListener { exception ->
//                activity.hideProgressDialog()
//                Log.e(activity.javaClass.simpleName, "Error While Creating A Board.", exception)
//            }
//    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        val documentRef = mFireStore.collection(Constants.BOARDS).document()
        board.documentId = documentRef.id
        documentRef.set(board, SetOptions.merge()).addOnSuccessListener {
            Log.e(activity.javaClass.simpleName, "Board Created Successfully!")
            Toast.makeText(activity, "Board Created Successfully!", Toast.LENGTH_SHORT).show()
            activity.boardCreatedSuccessfully()
        }.addOnFailureListener { exception ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Error While Creating A Board.", exception)
        }
    }


    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
//                    boardsListForDueDate.add(board)
                }
                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)

            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS).document(board.documentId).update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList Updated Successfully.")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener { exception ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error While Creating A TaskList", exception)
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data Updated Successfully")
                Toast.makeText(activity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }

                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error While Updating Profile!", e)
                Toast.makeText(activity, "Error While Updating Profile!", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is IntroActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }.addOnFailureListener {
                when (activity) {
                    is IntroActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
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

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo).get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val userList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if (activity is MembersActivity)
                    activity.setupMembersList(userList)
                else if (activity is TaskListActivity)
                    activity.boardMembersDetailsList(userList)
            }.addOnFailureListener { e ->
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "getAssignedMembersListDetails: Error", e)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL, email).get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No Such Member Found")
                }
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "getMemberDetails: Error While Gating User Details ",
                    e
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS).document(board.documentId).update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "assignMemberToBoard: Error While Updating Board ",
                    e
                )
            }
    }

    fun deleteBoard(activity: TaskListActivity, board: Board) {

        val currentUserId =
            getCurrentUserId()

        mFireStore.collection(Constants.USERS)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { userDocument ->
                val userName =
                    userDocument.getString("name")

                if (userName != null) {
                    mFireStore.collection(Constants.BOARDS)
                        .whereEqualTo(
                            "createdBy",
                            userName
                        )
                        .whereEqualTo("name", board.name)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val boardDocumentRef = querySnapshot.documents[0].reference
                                boardDocumentRef.delete()
                                    .addOnSuccessListener {
                                        activity.hideProgressDialog()
                                        Log.d(
                                            activity.javaClass.simpleName,
                                            "Board Deleted Successfully!!"
                                        )
                                        Toast.makeText(
                                            activity,
                                            "Board Deleted Successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        activity.boardDeletedSuccessfully()
                                    }
                                    .addOnFailureListener { exception ->
                                        activity.hideProgressDialog()
                                        Log.e(
                                            activity.javaClass.simpleName,
                                            "Error While Deleting The Board.",
                                            exception
                                        )
                                    }
                            } else {
                                activity.hideProgressDialog()
                                Toast.makeText(
                                    activity,
                                    "You Are Not Creator Of The Board!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w(
                                    activity.javaClass.simpleName,
                                    "No board found with the specified criteria."
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            activity.hideProgressDialog()
                            Log.e(activity.javaClass.simpleName, "Error Querying Boards", exception)
                        }
                } else {
                    activity.hideProgressDialog()
                    Log.w(activity.javaClass.simpleName, "User name not found.")
                }
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error Fetching User Document", exception)
            }
    }

    fun removeMemberFromBoard(activity: MembersActivity, board: Board, user: User): Boolean {
        if (board.createdBy != user.name) {
            Log.e(
                "FirestoreClass",
                "removeMemberFromBoard:  fetching board creator name ${board.createdBy.toString()}"
            )
            val assignedToList: ArrayList<String> = board.assignedTo
            assignedToList.remove(user.id)

            val docRef = mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)

            docRef.update(Constants.ASSIGNED_TO, assignedToList)
                .addOnSuccessListener {
                    activity.mAssignedMembersList.remove(user)
                    activity.setupMembersList(activity.mAssignedMembersList)
                    activity.hideProgressDialog()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while removing member from the board.",
                        e
                    )
                }
            return true
        } else {
            activity.hideProgressDialog()
            activity.showErrorSnackBar("You cannot remove the creator of the board.")
            return false
        }
    }

    fun getUpcomingCards(activity: NotificationActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)
                    board!!.documentId = i.id
                    boardsList.add(board)
                }
                retrieveCardsFromBoards(activity, boardsList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "getUpcomingCards: Error while getting boards."
                )
            }
    }

    private fun retrieveCardsFromBoards(
        activity: NotificationActivity,
        boardsList: ArrayList<Board>
    ) {
        val upcomingCards = ArrayList<Card>()
        val calendar = Calendar.getInstance()

        // Set to the start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.time

        // Set to the end of the day after tomorrow
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDayAfterTomorrow = calendar.time

        for (board in boardsList) {
            for (task in board.taskList) {
                for (card in task.cards) {
                    val cardDueDate = Date(card.dueDate)
                    if (cardDueDate in startOfToday..endOfDayAfterTomorrow) {
                        upcomingCards.add(card)
                    }
                }
            }
        }
        upcomingCards.sortWith(compareBy { it.dueDate })
        activity.upcomingCardsLoaded(upcomingCards)
    }
}