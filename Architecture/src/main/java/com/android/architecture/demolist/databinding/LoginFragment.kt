package com.android.architecture.demolist.databinding

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.android.architecture.R
import com.android.architecture.databinding.FragmentDataBinding1Binding

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // 此名根据布局文件名得来
        val binding: FragmentDataBinding1Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_data_binding1, container, false)
        // 生成Binding的另外一种方式
        /*val binding = FragmentLoginBinding.inflate(inflater, container, false)*/
        onSubscribeUi(binding)
        return binding.root
//        return inflater.inflate(R.layout.fragment_data_binding1, container, false)
    }

    private fun onSubscribeUi(binding: FragmentDataBinding1Binding) {
        val loginModel = LoginModel("","",context!!)
        binding.model = loginModel
        binding.isEnable = true
        binding.activity = activity

        binding.btnLogin.setOnClickListener {
            loginModel.login()
        }

        binding.btnRegister.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_data_binding_1_to_data_binding_2)
        }
    }
}
