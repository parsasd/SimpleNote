package com.example.simplenote.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentChangePasswordBinding
import com.example.simplenote.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChangePasswordBinding {
        return FragmentChangePasswordBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.etOldPassword.doAfterTextChanged { binding.tilOldPassword.error = null }
        binding.etNewPassword.doAfterTextChanged { binding.tilNewPassword.error = null }
        binding.etConfirmPassword.doAfterTextChanged { binding.tilConfirmPassword.error = null }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                viewModel.changePassword(
                    oldPassword = binding.etOldPassword.text.toString(),
                    newPassword = binding.etNewPassword.text.toString()
                )
            }
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.changePasswordState.collect { state ->
                binding.progressBar.isVisible = state is ChangePasswordViewModel.ChangePasswordState.Loading
                binding.btnSave.isEnabled = state !is ChangePasswordViewModel.ChangePasswordState.Loading

                when (state) {
                    is ChangePasswordViewModel.ChangePasswordState.Success -> {
                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is ChangePasswordViewModel.ChangePasswordState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        with(binding) {
            if (etOldPassword.text.isNullOrBlank()) {
                tilOldPassword.error = "Old password is required"
                isValid = false
            }
            if (etNewPassword.text.isNullOrBlank()) {
                tilNewPassword.error = "New password is required"
                isValid = false
            } else if (etNewPassword.text.toString().length < 8) {
                tilNewPassword.error = "Password must be at least 8 characters"
                isValid = false
            }
            if (etConfirmPassword.text.toString() != etNewPassword.text.toString()) {
                tilConfirmPassword.error = "Passwords do not match"
                isValid = false
            }
        }
        return isValid
    }
}