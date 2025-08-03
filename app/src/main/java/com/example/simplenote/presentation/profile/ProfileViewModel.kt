package com.example.simplenote.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.User
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userState: StateFlow<Resource<User>> = _userState

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState

    /**
     * Loads user information from the repository. This operation requires authentication.
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            // Only attempt to load user info if the user is logged in
            if (authRepository.isLoggedIn().first()) {
                _userState.value = Resource.Loading()
                _userState.value = authRepository.getUserInfo()
            } else {
                // If not logged in, set state to error or a default unauthenticated state
                _userState.value = Resource.Error("User not logged in.")
            }
        }
    }

    /**
     * Logs out the current user, clearing local tokens and user data.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutState.value = true
        }
    }
}
