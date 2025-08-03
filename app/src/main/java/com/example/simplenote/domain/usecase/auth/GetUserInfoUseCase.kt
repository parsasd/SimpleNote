package com.example.simplenote.domain.usecase.auth

import com.example.simplenote.domain.model.User
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.utils.Resource
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<User> {
        return authRepository.getUserInfo()
    }
}
