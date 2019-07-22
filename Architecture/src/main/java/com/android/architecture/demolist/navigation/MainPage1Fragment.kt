package com.android.architecture.demolist.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.android.architecture.R
import kotlinx.android.synthetic.main.fragment_main_page1.*

class MainPage1Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main_page1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn.setOnClickListener {
            //点击跳转page2Fragment
            Navigation.findNavController(it).navigate(R.id.action_page1Fragment_to_page2Fragment)
        }
    }
}