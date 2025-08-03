// File: presentation/auth/register/RegisterFragment.kt
package com.example.simplenote.presentation.auth.register

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentRegisterBinding // Added import
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRegisterBinding.inflate(inflater, container, false)

    override fun setupViews() {
        with(binding) {
            tvBackToLogin.setOnClickListener {
                findNavController().navigateUp()
            }

            etFirstName.doAfterTextChanged { tilFirstName.error = null }
            etLastName.doAfterTextChanged { tilLastName.error = null }
            etUsername.doAfterTextChanged { tilUsername.error = null }
            etEmail.doAfterTextChanged { tilEmail.error = null }
            etPassword.doAfterTextChanged { tilPassword.error = null }
            etRetypePassword.doAfterTextChanged { tilRetypePassword.error = null }

            btnRegister.setOnClickListener {
                if (validateInput()) {
                    viewModel.register(
                        username = etUsername.text.toString(),
                        password = etPassword.text.toString(),
                        email = etEmail.text.toString(),
                        firstName = etFirstName.text.toString(),
                        lastName = etLastName.text.toString()
                    )
                }
            }

            tvLogin.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.btnRegister.isEnabled = false
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        with(binding) {
            var isValid = true

            if (etFirstName.text.isNullOrEmpty()) {
                tilFirstName.error = "First name is required"
                isValid = false
            }

            if (etLastName.text.isNullOrEmpty()) {
                tilLastName.error = "Last name is required"
                isValid = false
            }

            if (etUsername.text.isNullOrEmpty()) {
                tilUsername.error = "Username is required"
                isValid = false
            }

            if (etEmail.text.isNullOrEmpty()) {
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

            return isValid
        }
    }
}
