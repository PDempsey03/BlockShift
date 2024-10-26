package com.blockshift.ui.mainpage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blockshift.R
import com.blockshift.model.HighScoreAdapter
import com.blockshift.model.repositories.HighScoreRepository
import com.blockshift.model.repositories.HighScoreTableNames
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Stack
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * Use the [HighScorePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HighScorePageFragment : Fragment() {

    private val TAG: String = javaClass.simpleName

    private val lastDocumentSnapshots: Stack<DocumentSnapshot?> = Stack<DocumentSnapshot?>()
    private val documentsPerPage: Long = 3
    private var currentStartingRank: Long = 1
    private val maxDisplayRank: Long = 5
    private var selectedLevel = "1"
    private var selectedHighScoreType = HighScoreTableNames.TIME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_high_score_page, container, false)

        // setup recycler view
        val recyclerView: RecyclerView = view.findViewById(R.id.high_scores_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val highScoreTypeTextView = view.findViewById<TextView>(R.id.high_score_score_type)
        updateHighScoreTypeColumnName(highScoreTypeTextView)

        // initialize the starting high score view
        resetHighScorePage(recyclerView)

        val previousButton = view.findViewById<ImageButton>(R.id.high_scores_previous_page_button)
        previousButton.setOnClickListener {
            if(lastDocumentSnapshots.size > 2) {
                loadNewHighScorePage(selectedHighScoreType, recyclerView, false)
            }
        }

        val nextButton = view.findViewById<ImageButton>(R.id.high_scores_next_page_button)
        nextButton.setOnClickListener {
            if(currentStartingRank <= maxDisplayRank) {
                loadNewHighScorePage(selectedHighScoreType, recyclerView, true)
            }
        }

        val firstPageButton = view.findViewById<ImageButton>(R.id.high_scores_first_page_button)
        firstPageButton.setOnClickListener {
            if(lastDocumentSnapshots.size > 2) {
                resetHighScorePage(recyclerView)
            }
        }

        val lastPageButton = view.findViewById<ImageButton>(R.id.high_scores_last_page_button)
        lastPageButton.setOnClickListener {

        }

        val levelSelectSpinner = view.findViewById<Spinner>(R.id.high_scores_level_select_drop_down)
        val levelSelectData = listOf(1, 2, 3, 4, 5, 6, 8, 9, 10) // TODO: change to searching files
        val levelSelectAdapter = ArrayAdapter(requireContext(), R.layout.level_select_drop_down_item, levelSelectData)
        levelSelectSpinner.adapter = levelSelectAdapter
        levelSelectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val newLevel = levelSelectData[position].toString() // TODO: will need to change this as above TODO changes

                if(newLevel != selectedLevel) {
                    selectedLevel = newLevel
                    resetHighScorePage(recyclerView)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        val highScoreTypeSpinner = view.findViewById<Spinner>(R.id.high_scores_high_score_type_drop_down)
        val highScoreTypeData = listOf(HighScoreTableNames.TIME, HighScoreTableNames.MOVES, HighScoreTableNames.DISTANCE)
        val highScoreTypeAdapter = ArrayAdapter(requireContext(), R.layout.level_select_drop_down_item, highScoreTypeData)
        highScoreTypeSpinner.adapter = highScoreTypeAdapter
        highScoreTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, innerView: View?, position: Int, id: Long) {
                val newHighScoreType = highScoreTypeData[position]
                if(newHighScoreType != selectedHighScoreType) {
                    selectedHighScoreType = newHighScoreType
                    updateHighScoreTypeColumnName(highScoreTypeTextView)
                    resetHighScorePage(recyclerView)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        return view
    }

    private fun updateHighScoreTypeColumnName(highScoreTypeTextView: TextView) {
        highScoreTypeTextView.text = when(selectedHighScoreType) {
            HighScoreTableNames.TIME -> getString(R.string.high_score_time)
            HighScoreTableNames.DISTANCE -> getString(R.string.high_score_distance)
            HighScoreTableNames.MOVES -> getString(R.string.high_score_moves)
            else -> "N/A"
        }
    }

    private fun resetHighScorePage(recyclerView: RecyclerView) {
        lastDocumentSnapshots.clear()
        lastDocumentSnapshots.push(null)
        currentStartingRank = 1
        loadNewHighScorePage(selectedHighScoreType, recyclerView, true)
    }

    private fun loadNewHighScorePage(highScoreType: String, recyclerView: RecyclerView, nextPage: Boolean) {
        // if we are going to a previous page, we need to update the most recent document to the old one
        if(!nextPage) {
            lastDocumentSnapshots.pop()
            lastDocumentSnapshots.pop()
            currentStartingRank -= (2 * documentsPerPage)
        }

        val recordsToGet = min(documentsPerPage, maxDisplayRank - currentStartingRank + 1)

        HighScoreRepository.getHighScoresInRange(highScoreType, selectedLevel, recordsToGet, lastDocumentSnapshots.peek(),
            { highScoreList, lastDocumentSnapshot ->
            // only update if there is anything new in the list
            if(highScoreList != null) {
                // update the high score adapter to display the current rankings
                Log.d(TAG, "High scores should be displaying")
                recyclerView.adapter = HighScoreAdapter(highScoreList, highScoreType, currentStartingRank)

                // always push the most recent document snapshot to the stack
                lastDocumentSnapshots.push(lastDocumentSnapshot)

                currentStartingRank += documentsPerPage
            } else {
                if(lastDocumentSnapshots.size <= 1) {
                    // if there were no documents and no previous documents then no data exists
                    recyclerView.adapter = null
                }
                Log.d(TAG, "list of high scores was null")
            }
        }, { exception ->
            Log.e(TAG, "Failed to load high scores", exception)
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment HighScorePageFragment.
         */
        @JvmStatic
        fun newInstance() =
            HighScorePageFragment().apply {
                arguments = Bundle().apply { }
            }
    }
}