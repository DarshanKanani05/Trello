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
import com.example.trello.activities.TaskListActivity
import com.example.trello.models.Board
import com.example.trello.models.Card
import com.example.trello.models.Task
import com.example.trello.models.User
import com.example.trello.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

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

    fun getBoardDetails(activity: TaskListActivity, documented: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documented).get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)

            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while opening a board.", e)

            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
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

//    private fun fetchAllBoards(): List<Board> {
//        val boardsList = mutableListOf<Board>()
//        mFireStore.collection("boards")
//            .get()
//            .addOnSuccessListener { documentsSnapshot ->
//                for (documentSnapshot in documentsSnapshot) {
//                    val board = documentSnapshot.toObject(Board::class.java)
//                    board?.let {
//                        // Fetch tasks and cards for the board
//                        fetchTasksAndCards(it)
//                        boardsList.add(it)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FetchBoards", "Error fetching boards", exception)
//            }
//
//        return boardsList
//    }
//
//    private fun fetchTasksAndCards(board: Board) {
//        mFireStore.collection("boards/${board.documentId}/tasks")
//            .get()
//            .addOnSuccessListener { documentsSnapshot ->
//                for (documentSnapshot in documentsSnapshot) {
//                    val task = documentSnapshot.toObject(Task::class.java)
//                    task?.let {
//                        // Fetch cards for the task
//                        fetchCardsForTask(it)
//                        board.taskList.add(it)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FetchTasks", "Error fetching tasks", exception)
//            }
//    }
//
//    private fun fetchCardsForTask(task: Task) {
//        mFireStore.collection("tasks/${task.documentId}/cards")
//            .get()
//            .addOnSuccessListener { documentsSnapshot ->
//                for (documentSnapshot in documentsSnapshot) {
//                    val card = documentSnapshot.toObject(Card::class.java)
//                    card?.let {
//                        task.cards.add(it)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FetchCards", "Error fetching cards", exception)
//            }
//    }
}