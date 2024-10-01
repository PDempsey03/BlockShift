package com.blockshift.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.blockshift.R

/**
 * A simple [Fragment] subclass.
 * Use the [StartScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start_screen, container, false)

        val loginButton = view.findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            Log.d("Start Screen", "Login Button Clicked")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment()) // fragment_container is the container for fragments
                .addToBackStack(null)  // This ensures you can go back
                .commit()
        }

        val createAccountButton = view.findViewById<Button>(R.id.start_screen_create_account_button)
        createAccountButton.setOnClickListener {
            Log.d("Start Screen", "Create Account Button Clicked")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment()) // fragment_container is the container for fragments
                .addToBackStack(null)  // This ensures you can go back
                .commit()
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment StartScreenFragment.
         */
        @JvmStatic
        fun newInstance() =
            StartScreenFragment().apply {
                arguments = Bundle().apply{}
            }
    }
}