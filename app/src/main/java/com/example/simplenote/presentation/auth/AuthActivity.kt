// File: presentation/auth/AuthActivity.kt
package com.example.simplenote.presentation.auth

import com.example.simplenote.databinding.ActivityAuthBinding // Added import
import com.example.simplenote.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity<ActivityAuthBinding>() {

    override fun getViewBinding() = ActivityAuthBinding.inflate(layoutInflater)

    override fun setupViews() {
        // Navigation will be handled by Navigation Component
    }

    override fun observeData() {
        // No specific data to observe at activity level
    }
}
