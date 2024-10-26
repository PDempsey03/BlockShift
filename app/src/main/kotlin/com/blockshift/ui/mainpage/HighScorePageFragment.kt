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
import com.blockshift.model.repositories.HighScoreData
import com.blockshift.model.repositories.HighScoreRepository
import com.blockshift.model.repositories.HighScoreTableNames
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * Use the [HighScorePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HighScorePageFragment : Fragment() {

    private val TAG: String = javaClass.simpleName

    private lateinit var highScoreDataList: List<HighScoreData>
    private val highScoresPerPage = 3
    private var nextStartingRank = 1
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
        loadNewHighScoreData(recyclerView)

        val previousButton = view.findViewById<ImageButton>(R.id.high_scores_previous_page_button)
        previousButton.setOnClickListener {
            loadNewPage(recyclerView, false)
        }

        val nextButton = view.findViewById<ImageButton>(R.id.high_scores_next_page_button)
        nextButton.setOnClickListener {
            loadNewPage(recyclerView, true)
        }

        val firstPageButton = view.findViewById<ImageButton>(R.id.high_scores_first_page_button)
        firstPageButton.setOnClickListener {
            nextStartingRank = 1
            loadNewPage(recyclerView, true)
        }

        val lastPageButton = view.findViewById<ImageButton>(R.id.high_scores_last_page_button)
        lastPageButton.setOnClickListener {
            val dataSize = highScoreDataList.size
            val elementsOnLastPage = dataSize % highScoresPerPage
            nextStartingRank = dataSize + 1 - if(elementsOnLastPage == 0) highScoresPerPage else elementsOnLastPage
            loadNewPage(recyclerView, true)
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
                    nextStartingRank = 1
                    loadNewHighScoreData(recyclerView)
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
                    nextStartingRank = 1
                    updateHighScoreTypeColumnName(highScoreTypeTextView)
                    loadNewHighScoreData(recyclerView)
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

    private fun loadNewPage(recyclerView: RecyclerView, nextPage: Boolean) {
        // handle case of empty list
        if(highScoreDataList.isEmpty()) {
            recyclerView.adapter = HighScoreAdapter(listOf(), selectedHighScoreType, nextStartingRank)
            // TODO: Do something here to show that no data is available
            return
        }

        // calculate new indices for page
        val startingIndex = nextStartingRank - 1 - (if(nextPage) 0 else 2 * highScoresPerPage)
        val maxAllowedIndex = highScoreDataList.size
        val endingIndex = min(startingIndex + highScoresPerPage, maxAllowedIndex)

        // only load new page if the requested indices are valid
        if(startingIndex in 0..<maxAllowedIndex) {
            if(!nextPage) nextStartingRank = startingIndex + 1

            // get the data elements to show on page and update the adapter to show them
            val subList = highScoreDataList.subList(startingIndex, endingIndex)
            recyclerView.adapter = HighScoreAdapter(subList, selectedHighScoreType, nextStartingRank)

            nextStartingRank += highScoresPerPage
        }
    }

    private fun loadNewHighScoreData(recyclerView: RecyclerView) {
        HighScoreRepository.getHighScoresInRange(selectedHighScoreType, selectedLevel, maxDisplayRank,
            { highScoreList ->
                highScoreDataList = highScoreList
                loadNewPage(recyclerView, true)
        }, { exception ->
            highScoreDataList = listOf()
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