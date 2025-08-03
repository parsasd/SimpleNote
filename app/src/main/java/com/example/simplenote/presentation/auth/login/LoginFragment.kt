// File: presentation/auth/login/LoginFragment.kt
package com.example.simplenote.presentation.auth.login

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.R
import com.example.simplenote.databinding.FragmentLoginBinding // Added import
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.presentation.main.MainActivity
import com.example.simplenote.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentLoginBinding.inflate(inflater, container, false)

    override fun setupViews() {
        with(binding) {
            etEmail.doAfterTextChanged { /* it: Editable? */
                tilEmail.error = null
            }

            etPassword.doAfterTextChanged { /* it: Editable? */
                tilPassword.error = null
            }

            btnLogin.setOnClickListener {
                val username = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (validateInput(username, password)) {
                    viewModel.login(username, password)
                }
            }

            tvRegister.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.btnLogin.isEnabled = false
                    }
                    is Resource.Success -> {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                    is Resource.Error -> {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.tilEmail.error = "Username is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }
}
