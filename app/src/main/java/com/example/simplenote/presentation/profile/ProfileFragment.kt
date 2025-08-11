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
import androidx.navigation.fragment.findNavController
import com.example.simplenote.R

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
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
    }

    override fun observeData() {
        viewModel.loadUserInfo()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // You can show a loading indicator here if you want
                    }
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            binding.tvUserName.text = "${user.firstName} ${user.lastName}"
                            binding.tvEmail.text = user.email
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        if (resource.message == "User not logged in." || resource.message?.contains("Unauthorized", ignoreCase = true) == true) {
                            viewModel.logout()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logoutState.collect { isLoggedOut ->
                if (isLoggedOut) {
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

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