package com.blockshift.ui.mainpage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blockshift.android.AndroidLauncher
import com.blockshift.model.db.HighScore
import com.blockshift.model.db.OfflineHighScoreViewModel
import com.blockshift.model.login.UserViewModel
import com.blockshift.ui.settings.SettingsActivity
import com.blockshift.R
import com.blockshift.model.repositories.HighScoreRepository
import com.blockshift.model.repositories.HighScoreTableNames
import com.blockshift.model.repositories.UserTableNames


/**
 * A simple [Fragment] subclass.
 * Use the [MainPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainPageFragment : Fragment() {
    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    */

    private lateinit var userViewModel: UserViewModel
    private lateinit var offHighScoreViewModel: OfflineHighScoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel = activity?.run {
            ViewModelProvider(this).get(UserViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        offHighScoreViewModel = (activity as SettingsActivity).getOfflineViewModel()

        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)
        val username = userViewModel.currentUser.value?.username
        val displayName = userViewModel.currentUser.value?.displayname
        val displayText = getString(R.string.welcome_message, displayName)
        welcomeText.text = displayText

        // update level buttons
        val buttonIds = listOf(
            R.id.level1_button,
            R.id.level2_button,
            R.id.level3_button,
            R.id.level4_button,
            R.id.level5_button,
            R.id.level6_button,
            R.id.level7_button,
            R.id.level8_button,
            R.id.level9_button,
            R.id.level10_button,
            R.id.level11_button,
            R.id.level12_button,
        )

        for(i in buttonIds.indices) {
            val levelButton = view.findViewById<Button>(buttonIds[i])
            levelButton.setOnClickListener {
                loadLevel(i + 1)
            }
        }

        val continueButton = view.findViewById<Button>(R.id.continue_level_button)

        // temp code to make sure we can load the game
        continueButton.setOnClickListener {
            loadLevel(1)
        }

        if(username == "lcl") {
            /*
            for(i in 1..12) {
                offHighScoreViewModel.addHighScore(HighScore("lcl",i,0,0,0))
            }

             */
            offHighScoreViewModel.readByUsername.observe(viewLifecycleOwner, Observer {
                scores ->
                val nextLevel = getUserLevelProgress(scores)
                continueButton.setOnClickListener {
                    //
                    Log.d(javaClass.simpleName,"Starting level $nextLevel")
                    loadLevel(nextLevel)
                }
            })

        } else {
            HighScoreRepository.getHighScoresByUsername(username ?: "???", {
                //On Success
                highScoreList ->
                var nextLevel = 1
                for(score in highScoreList) {
                    if(score.levelid == nextLevel.toString()) {
                        nextLevel++
                    }
                }
                continueButton.setOnClickListener {
                    loadLevel(nextLevel)
                }
            }, {
                //On Failure
                exception ->
                Log.e(javaClass.simpleName,"Unable to Load Scores",exception)
            })
        }


        return view
    }

    private fun loadLevel(level:Int) {
        val intent = Intent(requireContext(), AndroidLauncher::class.java)
        intent.putExtra(HighScoreTableNames.LEVEL_ID, level)
        intent.putExtra(UserTableNames.USERNAME, userViewModel.currentUser.value?.username)
        intent.putExtra(UserTableNames.DISPLAY_NAME, userViewModel.currentUser.value?.displayname)
        startActivity(intent)
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomePageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            MainPageFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

fun getUserLevelProgress(scores:List<HighScore>): Int {
    for(i in 0..minOf(11,scores.size-1)) {
        val scoreAt = scores[i]
        if(scoreAt.distance == Int.MAX_VALUE && scoreAt.time == Long.MAX_VALUE && scoreAt.moves == Int.MAX_VALUE) {
            return i+1
        }
    }

    //Have the first level be the default if all levels are complete
    return 1
}
