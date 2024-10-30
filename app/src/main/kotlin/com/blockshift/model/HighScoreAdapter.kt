package com.blockshift.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blockshift.R
import com.blockshift.model.repositories.HighScoreDisplayData
import com.blockshift.model.repositories.HighScoreTableNames

class HighScoreAdapter(private val highScores: List<HighScoreDisplayData>, private val highScoreType: String, private val startingRank: Int) : RecyclerView.Adapter<HighScoreAdapter.HighScoreViewHolder>() {

    class HighScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.high_score_row_item_rank)
        val displayNameTextView: TextView = itemView.findViewById(R.id.high_score_row_item_display_name)
        val usernameTextView: TextView = itemView.findViewById(R.id.high_score_row_item_username)
        val highScoreTypeTextView: TextView = itemView.findViewById(R.id.high_score_row_item_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.high_score_row_item, parent, false)

        return HighScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighScoreViewHolder, position: Int) {
        // access data
        val highScoreItem = highScores[position]
        val rank = startingRank + position

        // update text based on data
        holder.rankTextView.text = rank.toString()
        holder.displayNameTextView.text = highScoreItem.userData.displayname
        holder.usernameTextView.text = "@" + highScoreItem.userData.username
        holder.highScoreTypeTextView.text = when(highScoreType) {
            HighScoreTableNames.TIME -> highScoreItem.highScoreData.time.toString()
            HighScoreTableNames.DISTANCE -> highScoreItem.highScoreData.distance.toString()
            HighScoreTableNames.MOVES -> highScoreItem.highScoreData.moves.toString()
            else -> "N/A"
        }
    }

    override fun getItemCount(): Int {
        return highScores.size
    }
}
