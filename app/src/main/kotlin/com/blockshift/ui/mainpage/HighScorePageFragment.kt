package com.blockshift.ui.mainpage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blockshift.R
import com.blockshift.model.HighScoreAdapter
import com.blockshift.model.repositories.HighScoreRepository
import com.blockshift.model.repositories.HighScoreTableNames
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Stack

/**
 * A simple [Fragment] subclass.
 * Use the [HighScorePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HighScorePageFragment : Fragment() {

    private val TAG: String = javaClass.simpleName

    private val lastDocumentReferences: Stack<DocumentSnapshot?> = Stack<DocumentSnapshot?>()
    private val documentsPerPage: Long = 3
    private var currentStartingRank: Long = 1
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

        // make the last document null to start at the beginning
        lastDocumentReferences.push(null)

        // setup the adapter on page load
        loadNewHighScorePage(selectedHighScoreType, recyclerView, true)

        val previousButton = view.findViewById<Button>(R.id.high_scores_previous_page_button)
        previousButton.setOnClickListener {
            if(lastDocumentReferences.size > 2) {
                loadNewHighScorePage(selectedHighScoreType, recyclerView, false)
            }
        }

        val nextButton = view.findViewById<Button>(R.id.high_scores_next_page_button)
        nextButton.setOnClickListener {
            loadNewHighScorePage(selectedHighScoreType, recyclerView, true)
        }

        val levelSelectSpinner = view.findViewById<Spinner>(R.id.high_scores_level_select_drop_down)
        val levelSelectData = listOf(1, 2, 3, 4, 5, 6, 8, 9, 10)
        val levelSelectAdapter = ArrayAdapter(requireContext(), R.layout.level_select_drop_down_item, levelSelectData)
        levelSelectSpinner.adapter = levelSelectAdapter

        return view
    }

    private fun loadNewHighScorePage(highScoreType: String, recyclerView: RecyclerView, nextPage: Boolean) {
        // if we are going to a previous page, we need to update the most recent document to the old one
        if(!nextPage) {
            lastDocumentReferences.pop()
            lastDocumentReferences.pop()
            currentStartingRank -= (2 * documentsPerPage)
        }

        HighScoreRepository.getHighScoresInRange(highScoreType, documentsPerPage, lastDocumentReferences.peek(),
            { highScoreList, lastDocumentReference ->
            // only update if there is anything new in the list
            if(highScoreList != null) {
                // update the high score adapter to display the current rankings
                Log.d(TAG, "High scores should be displaying")
                recyclerView.adapter = HighScoreAdapter(highScoreList, highScoreType, currentStartingRank)

                // always push the most recent document snapshot to the stack
                lastDocumentReferences.push(lastDocumentReference)

                currentStartingRank += documentsPerPage
            } else {
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