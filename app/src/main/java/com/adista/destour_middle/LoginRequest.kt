package com.adista.destour_middle

data class LoginRequest (
    val endpoint: String = "login",
    val email: String,
    val password: String
)