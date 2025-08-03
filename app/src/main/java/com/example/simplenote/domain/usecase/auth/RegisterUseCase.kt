package com.example.simplenote.domain.usecase.auth

import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.utils.Resource
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String
    ): Resource<Unit> {
        return authRepository.register(username, password, email, firstName, lastName)
    }
}
