package com.blockshift.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blockshift.model.repositories.HighScoreDisplayData
import com.blockshift.model.repositories.HighScoreTableNames
import com.blockshift.R

class HighScoreAdapter(private val highScores: List<HighScoreDisplayData>, private val highScoreType: String, private val startingRank: Int) : RecyclerView.Adapter<HighScoreAdapter.HighScoreViewHolder>() {

    private val milliSecondsInSecond = 1000L
    private val secondsInMinute = 60
    private val minutesInHour = 60
    private val maxDisplayTime = (((minutesInHour - 1) * secondsInMinute * milliSecondsInSecond) + ((secondsInMinute - 1) *
        milliSecondsInSecond) +
        (milliSecondsInSecond - 1)) // max mins + max secs + max ms

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
            HighScoreTableNames.TIME -> {
                // need to properly format time
                val totalMilliseconds = highScoreItem.highScoreData.time

                // wont display anything longer than or equal to 1 hour
                if(totalMilliseconds > maxDisplayTime) {
                    holder.itemView.context.getString(R.string.high_score_beyond_max_time).uppercase()
                } else {
                    val totalSeconds = totalMilliseconds / milliSecondsInSecond
                    val remainingMilliseconds = totalMilliseconds % milliSecondsInSecond
                    val totalMinutes = totalSeconds / secondsInMinute
                    val remainingSeconds = totalSeconds % secondsInMinute
                    String.format("%02d:%02d:%03d", totalMinutes, remainingSeconds, remainingMilliseconds)
                }
            }
            HighScoreTableNames.DISTANCE -> highScoreItem.highScoreData.distance.toString()
            HighScoreTableNames.MOVES -> highScoreItem.highScoreData.moves.toString()
            else -> "N/A"
        }
    }

    override fun getItemCount(): Int {
        return highScores.size
    }
}
