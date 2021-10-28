package com.example.webviewtest

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.webviewtest.App.Companion.cookieManager
import com.example.webviewtest.databinding.FragmentWebBinding
import kotlinx.coroutines.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.example.webviewtest.App.Companion.webViewUrl
import com.example.webviewtest.App.Companion.webViewUrlHome
import kotlin.system.exitProcess


class WebFragment : Fragment() {

    private var binding_ : FragmentWebBinding? = null
    private val binding get() = binding_!!
    private val args: WebFragmentArgs by navArgs()
    private var logged: Boolean = false
    private var firstLoading: Boolean = true
    private var exitToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exitToast == null ) {
                    exitToast = Toast.makeText(context, "Presione nuevamente para salir", Toast.LENGTH_LONG)
                    exitToast!!.show()
                } else {
                    exitToast!!.cancel()
                    activity!!.finish()
                    exitProcess(0)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        binding_ = FragmentWebBinding.inflate(inflater, container, false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Conf de webview
        binding.wvWeb.webChromeClient = object : WebChromeClient(){}
        binding.wvWeb.webViewClient = object  : WebViewClient(){}
        val settings = binding.wvWeb.settings
        settings.javaScriptEnabled = true

        // Leo shared prefereces para ver si ya habia cookie
        App.sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return


        val user = args.user
        val pass= args.pass


        Log.e("LOG", "Comienzo corutina")
        binding.pbLoading.isVisible = true
        binding.wvWeb.isVisible = false
        CoroutineScope(Dispatchers.IO).launch(){
            try {
                App.cookieAPI.login(LoginPayload(user, pass))
                withContext(Dispatchers.Main){
                     reloadUrl()
                }
            } catch (exc: Throwable) {
                //toast("Login KO: $exc")
                Log.e("LOG", "Login KO: $exc")
            }
        }

        binding.wvWeb.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {

                Log.e("LOG", "Page finish $url ")
                if (firstLoading){
                    reloadUrlHome()
                firstLoading = false
                }
                try {
                    val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                    if (!logged && url == webViewUrlHome) {
                        logged = true
//                        if( findNavController().currentDestination?.id == R.id.webFragment)
//                            reloadUrlHome()
                        binding.pbLoading.isVisible = false
                        binding.wvWeb.isVisible = true
                        with(App.sharedPref.edit()) {
                            putBoolean(getString(R.string.logged_key), true)
                            apply()
                        }
                    }
                } catch (exc: Throwable) {
                    Log.e("LOG", "Android WebKitCookieManager error", exc)
                }

            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                    Log.e("LOG", "Page Started $url ")
                    if (url == App.webViewUrlLogout && logged ) {
                        cookieManager.removeAll()
                        logged = false

                        with(App.sharedPref.edit()) {
                            putBoolean(getString(R.string.logged_key), false)
                            apply()
                        }
                        if( findNavController().currentDestination?.id == R.id.webFragment)
                            //binding.wvWeb.stopLoading()
                            findNavController().navigate(
                                    WebFragmentDirections.actionWebFragmentToLoginFragment(
                                            "Logged out"
                                    )
                            )
                    }
                super.onPageStarted(view, url, favicon)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                Log.e("LOG", "override a $url")
                val override = !logged
                var message = ""
                // Aca se detecta si se redirige a .com/ es que perdimos el login o fallo login
                if( url == App.webViewUrl ){
                    cookieManager.removeAll()
                    //clearWebView()
                    if( logged ) {
                        logged = false
                        message = "Logout"

                        with (App.sharedPref.edit()) {
                            putBoolean(getString(R.string.logged_key), false )
                            apply()
                        }
                    }else{
                        message = "Usuario/Contraseña incorrectos"
                    }
                    if( findNavController().currentDestination?.id == R.id.webFragment) {
                        findNavController().navigate(WebFragmentDirections.actionWebFragmentToLoginFragment(message))
                    }

                }
                return override
            }


        }





    }

    // load home in webview to check cookie sync
    private fun reloadUrl() {
        binding.wvWeb.loadUrl(App.webViewUrl)
    }
    private fun reloadUrlHome() {
        binding.wvWeb.loadUrl(App.webViewUrlHome)
    }

    private fun clearWebView(){
        binding.wvWeb.loadUrl("about:blank")
    }
}

