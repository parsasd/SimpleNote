package com.example.simplenote.presentation.auth.register

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentRegisterBinding
import com.example.simplenote.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRegisterBinding.inflate(inflater, container, false)

    override fun setupViews() {
        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etFirstName.doAfterTextChanged { binding.tilFirstName.error = null }
        binding.etLastName.doAfterTextChanged { binding.tilLastName.error = null }
        binding.etUsername.doAfterTextChanged { binding.tilUsername.error = null }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
        binding.etRetypePassword.doAfterTextChanged { binding.tilRetypePassword.error = null }

        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                viewModel.register(
                    username = binding.etUsername.text.toString().trim(),
                    password = binding.etPassword.text.toString(),
                    email = binding.etEmail.text.toString().trim(),
                    firstName = binding.etFirstName.text.toString().trim(),
                    lastName = binding.etLastName.text.toString().trim()
                )
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                binding.btnRegister.isEnabled = state !is RegisterViewModel.RegisterState.Loading

                when (state) {
                    is RegisterViewModel.RegisterState.Success -> {
                        Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                    is RegisterViewModel.RegisterState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        with(binding) {
            if (etFirstName.text.isNullOrBlank()) {
                tilFirstName.error = "First name is required"
                isValid = false
            }
            if (etLastName.text.isNullOrBlank()) {
                tilLastName.error = "Last name is required"
                isValid = false
            }
            if (etUsername.text.isNullOrBlank()) {
                tilUsername.error = "Username is required"
                isValid = false
            }
            if (etEmail.text.isNullOrBlank()) {
                tilEmail.error = "Email is required"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
                tilEmail.error = "Invalid email format"
                isValid = false
            }
            if (etPassword.text.isNullOrEmpty()) {
                tilPassword.error = "Password is required"
                isValid = false
            } else if (etPassword.text.toString().length < 8) {
                tilPassword.error = "Password must be at least 8 characters"
                isValid = false
            }
            if (etRetypePassword.text.toString() != etPassword.text.toString()) {
                tilRetypePassword.error = "Passwords do not match"
                isValid = false
            }
        }
        return isValid
    }
}