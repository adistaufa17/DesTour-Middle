package com.adista.destour_middle

data class RegisterRequest(
    val endpoint: String = "register",
    val nama_lengkap: String,
    val email: String,
    val nomor_hp: String,
    val password: String,
    val confirm_password: String
)