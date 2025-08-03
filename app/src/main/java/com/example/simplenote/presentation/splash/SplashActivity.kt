package com.example.simplenote.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.simplenote.databinding.ActivitySplashBinding
import com.example.simplenote.presentation.auth.AuthActivity
import com.example.simplenote.presentation.base.BaseActivity
import com.example.simplenote.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun setupViews() {
        // Splash screen setup
    }

    override fun observeData() {
        lifecycleScope.launch {
            delay(2000) // Show splash for 2 seconds
            viewModel.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                }
                finish()
            }
        }
    }
}