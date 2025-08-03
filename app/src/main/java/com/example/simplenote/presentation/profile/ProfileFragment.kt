package com.example.simplenote.presentation.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.simplenote.databinding.FragmentProfileBinding
import com.example.simplenote.presentation.auth.AuthActivity
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileBinding.inflate(inflater, container, false)

    override fun setupViews() {
        binding.itemLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.itemChangePassword.setOnClickListener {
            // Navigate to change password screen
            Toast.makeText(context, "Change password coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun observeData() {
        // Initially load user info when the fragment is created/resumed
        viewModel.loadUserInfo()

        lifecycleScope.launch {
            viewModel.userState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator (e.g., a ProgressBar or disable UI elements)
                        // For simplicity, we'll just handle success/error for now.
                    }
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            // Update UI with user information
                            binding.tvUserName.text = "${user.firstName} ${user.lastName}"
                            binding.tvEmail.text = user.email
                        }
                    }
                    is Resource.Error -> {
                        // Show error message
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        // If the error indicates "User not logged in" or similar authentication issue,
                        // redirect to the AuthActivity to force re-authentication.
                        if (resource.message == "User not logged in." || resource.message?.contains("Unauthorized", ignoreCase = true) == true) {
                            // Trigger logout to clear any stale tokens and then navigate to AuthActivity
                            viewModel.logout() // This will set _logoutState.value to true
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.logoutState.collect { isLoggedOut ->
                if (isLoggedOut) {
                    // If logout is successful (or triggered by an auth error), navigate to AuthActivity
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                    requireActivity().finish() // Finish MainActivity so user can't go back
                }
            }
        }
    }

    /**
     * Shows a confirmation dialog for logging out.
     */
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out from the application?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
