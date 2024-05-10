package com.example.trello.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trello.R
import com.example.trello.adapters.LabelColorListItemsAdapter
import com.example.trello.adapters.MemberListItemsAdapter
import com.example.trello.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {
    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    @SuppressLint("CutPasteId")
    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tv_title).text = title
        if (list.size > 0) {
            view.findViewById<RecyclerView>(R.id.rv_list).layoutManager =
                LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, list)
            view.findViewById<RecyclerView>(R.id.rv_list).adapter=adapter

            adapter!!.setOnClickListener(object : MemberListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User,action: String)
}