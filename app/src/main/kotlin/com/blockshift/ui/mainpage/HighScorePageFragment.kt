package com.blockshift.ui.mainpage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockshift.R

/**
 * A simple [Fragment] subclass.
 * Use the [HighScorePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HighScorePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_high_score_page, container, false)



        return view
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