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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun setupViews() {}

    override fun observeData() {
        lifecycleScope.launchWhenStarted {
            delay(1500)

            viewModel.authState.first { it !is SplashViewModel.AuthState.Loading }

            when (viewModel.authState.value) {
                is SplashViewModel.AuthState.Authenticated -> {
                    navigateTo(MainActivity::class.java)
                }
                else -> {
                    navigateTo(AuthActivity::class.java)
                }
            }
        }
    }

    private fun <T> navigateTo(activityClass: Class<T>) {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}