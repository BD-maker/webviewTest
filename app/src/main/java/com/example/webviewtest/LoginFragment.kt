package com.example.webviewtest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.webviewtest.App.Companion.cookieManager
import com.example.webviewtest.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {


    private var binding_ : FragmentLoginBinding? = null
    private val binding get() = binding_!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding_ = FragmentLoginBinding.inflate(inflater, container, false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etUser.setText("demo@proflota.com")
        binding.etPass.setText("demo123proflota")
        //cookieManager.removeAll()

        binding.btnLogin.setOnClickListener(View.OnClickListener {
            if( binding.etUser.text.isNotEmpty()){
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToWebFragment(
                            binding.etUser.text.toString(), binding.etPass.text.toString()))
            }

        })
    }
}