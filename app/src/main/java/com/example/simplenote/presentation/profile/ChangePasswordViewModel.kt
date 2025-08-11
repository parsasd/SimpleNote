package com.example.simplenote.presentation.profile

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
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class ChangePasswordState {
        object Idle : ChangePasswordState()
        object Loading : ChangePasswordState()
        object Success : ChangePasswordState()
        data class Error(val message: String) : ChangePasswordState()
    }

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            val result = authRepository.changePassword(oldPassword, newPassword)
            _changePasswordState.value = when (result) {
                is Resource.Success -> ChangePasswordState.Success
                is Resource.Error -> ChangePasswordState.Error(result.message ?: "An unknown error occurred")
                else -> ChangePasswordState.Error("An unexpected error occurred")
            }
        }
    }
}