package com.blockshift.ui.mainpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blockshift.R
import com.blockshift.model.db.OfflineHighScoreViewModel
import com.blockshift.model.login.UserViewModel

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

        offHighScoreViewModel = activity?.run {
            ViewModelProvider(this).get(OfflineHighScoreViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)
        val username = userViewModel.currentUser.value?.username
        val displayName = userViewModel.currentUser.value?.displayname
        val displayText = "Welcome $displayName!"
        welcomeText.setText(displayText)

        if(username == "lcl") {
            val scores = offHighScoreViewModel.readByUsername
        }


        val continueButton = view.findViewById<Button>(R.id.continue_level_button)
        continueButton.setOnClickListener {
            val nextLevel = getUserLevelProgress(username?:"lcl")
            //Load the level based on the users level progress
        }



        return view
    }

    private fun getUserLevelProgress(username:String): Int {
        //Check if we are using local storage
        if(username == "lcl") {

        } else {

        }
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