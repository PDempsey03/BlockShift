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
import com.gdxblkshkt.R

/**
 * A simple [Fragment] subclass.
 * Use the [HomePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomePageFragment : Fragment() {
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
        val displayText = "Welcome $displayName!"
        welcomeText.setText(displayText)



        val continueButton = view.findViewById<Button>(R.id.continue_level_button)

        // temp code to make sure we can load the game
        continueButton.setOnClickListener {
            startActivity(Intent(requireContext(), AndroidLauncher::class.java))
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
                }
            })
        }


        return view
    }

    private fun getUserLevelProgress(scores:List<HighScore>): Int {
        for(i in 0..minOf(11,scores.size-1)) {
            val scoreAt = scores[i]
            if(scoreAt.distance == 0 && scoreAt.time == 0 && scoreAt.moves == 0) {
                return i+1
            }
        }

        //Have the first level be the default if all levels are complete
        return 1
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
            HomePageFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
