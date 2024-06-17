package com.example.trello.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trello.databinding.ItemNotificationCardBinding
import com.example.trello.models.Card
import java.text.SimpleDateFormat
import java.util.Locale

class CardNotificationAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemNotificationCardBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.binding.tvCardTitle.text = model.name
            holder.binding.tvCardDueDate.text = formatDate(model.dueDate)

            holder.itemView.setOnClickListener {
                onItemClickListener?.onClick(position, model)
            }
        }
    }

    private fun formatDate(date: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }

    fun setList(list: ArrayList<Card>) {
        this.list = list
        notifyDataSetChanged()
    }

    private class MyViewHolder(val binding: ItemNotificationCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onClick(position: Int, model: Card)
    }
}