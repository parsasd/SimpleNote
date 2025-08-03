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

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val registerState: StateFlow<Resource<Unit>> = _registerState

    fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = authRepository.register(username, password, email, firstName, lastName)
        }
    }
}