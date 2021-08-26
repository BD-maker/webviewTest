package com.example.webviewtest

import android.app.Application
import android.util.Log
import com.ashokvarma.gander.Gander
import com.ashokvarma.gander.GanderInterceptor
import com.ashokvarma.gander.imdb.GanderIMDB
import net.gotev.cookiestore.InMemoryCookieStore
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.WebKitSyncCookieManager
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

class App : Application() {


    companion object {
        const val baseAPIUrl = "https://proflota.com/"
        const val webViewUrl = "https://proflota.com/"
        const val cookieStoreName = "myCookies"
        const val username = "userdemo"

        lateinit var cookieManager: WebKitSyncCookieManager
        lateinit var cookieAPI: CookieAPI
    }

    private fun createCookieStore(name: String, persistent: Boolean) = if (persistent) {
        SharedPreferencesCookieStore(this, name)
    } else {
        InMemoryCookieStore(name)
    }

    override fun onCreate() {
        super.onCreate()

        cookieManager = WebKitSyncCookieManager(
            store = createCookieStore(name = cookieStoreName, persistent = true),
            cookiePolicy = CookiePolicy.ACCEPT_ALL,
            onWebKitCookieManagerError = { exception ->
                // This gets invoked when there's internal webkit cookie manager exceptions
                Log.e("COOKIE-STORE", "WebKitSyncCookieManager error", exception)
            }
        )

        // Setup for HttpURLConnection
        CookieManager.setDefault(cookieManager)

        Gander.setGanderStorage(GanderIMDB.getInstance())

        // Setup for OkHttp
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addNetworkInterceptor(GanderInterceptor(this).showNotification(true))
            .build()

        cookieAPI = Retrofit.Builder()
            .baseUrl(baseAPIUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CookieAPI::class.java)
    }
}