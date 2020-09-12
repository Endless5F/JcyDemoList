package com.android.architecture.demolist.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.android.architecture.R
import kotlinx.android.synthetic.main.fragment_main_page2.*

class MainPage2Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_page2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_page2Fragment_to_page1Fragment)
        }

        btn2.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_page2Fragment_to_page3Fragment)
        }
    }
}