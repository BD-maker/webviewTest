package com.example.webviewtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.webviewtest.App.Companion.cookieManager
import com.example.webviewtest.databinding.FragmentWebBinding
import kotlinx.coroutines.*


class WebFragment : Fragment() {

    private var binding_ : FragmentWebBinding? = null
    private val binding get() = binding_!!
    private val BASE_URL = "https://proflota.com/"
    private val args: WebFragmentArgs by navArgs()
    private val scope = MainScope()
    private var logged: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding_ = FragmentWebBinding.inflate(inflater, container, false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.wvWeb.webChromeClient = object : WebChromeClient(){

        }
        binding.wvWeb.webViewClient = object  : WebViewClient(){

        }
        val settings = binding.wvWeb.settings
        settings.javaScriptEnabled = true


        val user = args.user
        val pass= args.pass


        Log.e("COOKIE", "Comienzo corutina")
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
                try {
                    Log.e("COOKIE", "Page Finished $url ")
                    if( !logged ) {
                        val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                        logged = true
                        Log.e("COOKIE", "Cookie guardada [$cookie]")
                        reloadUrlHome()
                        binding.pbLoading.isVisible = false

                    }else{
                        binding.wvWeb.isVisible = true
                    }
                } catch (exc: Throwable) {
                    Log.e("COOKIE", "Android WebKitCookieManager error", exc)
                }
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                Log.e("COOKIE", "Page Started $url ")
                if (url == "https://proflota.com/logout" && findNavController().currentDestination?.id == R.id.webFragment){
                    cookieManager.removeAll()
                    clearWebView()
                    logged = false
                    findNavController().navigate(WebFragmentDirections.actionWebFragmentToLoginFragment("Logged out"))
                }else if( url == "https://proflota.com/home" ){
                    Log.e("COOKIE", "ACA HIZO LOGIN")
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.e("COOKIE", "evaluacion override p/url $url")
                val override = !logged
                if( url == App.webViewUrl && findNavController().currentDestination?.id == R.id.webFragment){
                    cookieManager.removeAll()
                    clearWebView()
                    logged = false
                    findNavController().navigate(WebFragmentDirections.actionWebFragmentToLoginFragment("Usuario/Contraseña incorrectos"))
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

