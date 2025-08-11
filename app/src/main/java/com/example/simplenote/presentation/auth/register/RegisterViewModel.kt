package com.example.simplenote.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            when (val result = authRepository.register(username, password, email, firstName, lastName)) {
                is Resource.Success -> _registerState.value = RegisterState.Success
                is Resource.Error -> _registerState.value = RegisterState.Error(result.message ?: "An unknown error occurred")
                else -> _registerState.value = RegisterState.Error("An unexpected error occurred")
            }
        }
    }
}