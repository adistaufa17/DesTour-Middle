package com.adista.destour_middle.data.request

data class LoginRequest (
    val endpoint: String = "login",
    val email: String,
    val password: String
)