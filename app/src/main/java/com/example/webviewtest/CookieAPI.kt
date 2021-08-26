package com.example.webviewtest

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginPayload(val email: String, val password: String)

interface CookieAPI {
    @POST("mobile/auth")
    suspend fun login(@Body payload: LoginPayload)

    @GET("")
    suspend fun home(): String
}
