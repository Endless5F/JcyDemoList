package com.android.architecture.demolist.databinding


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.android.architecture.R
import com.android.architecture.databinding.FragmentDataBinding2Binding

class RegisterFragment : Fragment() {

    private val registerModel:RegisterModel by lazy{
        RegisterModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding: FragmentDataBinding2Binding = DataBindingUtil.inflate(inflater
                , R.layout.fragment_data_binding2, container, false)

        onSubscribeUi(binding)
        return binding.root
//        return inflater.inflate(R.layout.fragment_data_binding2, container, false)
    }

    private fun onSubscribeUi(binding: FragmentDataBinding2Binding) {
        binding.btnRegister.setOnClickListener {
            registerModel.register()
            val bundle = Bundle()
            bundle.putString("name", registerModel.n.value)
            Navigation.findNavController(it).navigate(R.id.action_data_binding_2_to_data_binding_1, bundle, null)
        }
    }

}
