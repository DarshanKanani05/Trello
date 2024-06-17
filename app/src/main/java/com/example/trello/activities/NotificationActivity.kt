package com.example.trello.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trello.R
import com.example.trello.adapters.CardNotificationAdapter
import com.example.trello.databinding.ActivityNotificationBinding
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.Card
import com.example.trello.utils.Constants

class NotificationActivity : BaseActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var mCardNotificationAdapter: CardNotificationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActionBar()

        initializeUI()
        loadUpcomingCards()

    }

    private fun initializeUI() {
        binding.rvUpcomingCardsList.layoutManager = LinearLayoutManager(this)
        mCardNotificationAdapter = CardNotificationAdapter(this, ArrayList())
        binding.rvUpcomingCardsList.adapter = mCardNotificationAdapter

        mCardNotificationAdapter.onItemClickListener =
            object : CardNotificationAdapter.OnItemClickListener {
                override fun onClick(position: Int, model: Card) {
                    openBoardDetails(model.boardDocumentId)
                }
            }
    }

    private fun openBoardDetails(boardDocumentId: String){
        val intent = Intent(this@NotificationActivity, TaskListActivity::class.java)
        intent.putExtra(Constants.DOCUMENT_ID, boardDocumentId)
        startActivity(intent)
        finish()
    }

    private fun loadUpcomingCards() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUpcomingCards(this)
    }

    fun upcomingCardsLoaded(cards: ArrayList<Card>) {
        hideProgressDialog()
        if (cards.size > 0) {
            binding.rvUpcomingCardsList.visibility = View.VISIBLE
            binding.tvNoCardsAvailable.visibility = View.GONE
            mCardNotificationAdapter.setList(cards)
        } else {
            binding.rvUpcomingCardsList.visibility = View.GONE
            binding.tvNoCardsAvailable.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarNotificationActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Upcoming Due Dates"
        }

        binding.toolbarNotificationActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}