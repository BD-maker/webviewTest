package com.example.webviewtest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.webviewtest.App.Companion.cookieManager

import com.example.webviewtest.databinding.FragmentLoginBinding
import java.lang.Integer.getInteger

class LoginFragment : Fragment() {


    private var binding_ : FragmentLoginBinding? = null
    private val binding get() = binding_!!
    private val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding_ = FragmentLoginBinding.inflate(inflater, container, false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instancio shared preferences
        App.sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return

        // Leo la bandera si debo cargar el usuario
        val shouldLoadUser = App.sharedPref.getBoolean(getString(R.string.remember_key), false)

        if( shouldLoadUser ){
            val Username = App.sharedPref.getString(getString(R.string.user_key), "")
            val Password = App.sharedPref.getString(getString(R.string.pass_key), "")
            binding.etUser.setText(Username)
            binding.etPass.setText(Password)
            binding.cbRemind.isChecked = true
        }

        val alreadyLogged = App.sharedPref.getBoolean(getString(R.string.logged_key), false)
        if( alreadyLogged ){
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToWebFragment(
                    binding.etUser.text.toString(), binding.etPass.text.toString()))
        }


        // args.message es safe args, y no puede ser nulo, le pongo none como para poner algo
        var message:String = args.message
        if( message == "none"){
            message = ""
        }
        binding.tvMessage.setText(message)
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            if( binding.etUser.text.isNotEmpty()){

                if( binding.cbRemind.isChecked ){
                    // Guardo user, name y bandera de guardar en shared preferences
                    with (App.sharedPref.edit()) {
                        putString(getString(R.string.user_key), binding.etUser.text.toString())
                        putString(getString(R.string.pass_key), binding.etPass.text.toString())
                        putBoolean(getString(R.string.remember_key), true )
                        apply()
                    }
                }
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToWebFragment(
                            binding.etUser.text.toString(), binding.etPass.text.toString()))
            }

        })
    }




}