package com.adista.projekadvance

import com.adista.projekadvance.login.LoginActivityKelasku
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityKelasku : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivityKelasku::class.java))
            finish()
            return
        }

        // Lanjutkan tampilkan UI utama
        setContentView(R.layout.activity_main_kelasku)

    }
}