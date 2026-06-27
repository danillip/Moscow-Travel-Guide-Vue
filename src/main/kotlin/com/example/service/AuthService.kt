package com.example.service

import com.example.model.AuthResponse
import com.example.model.CurrentUserResponse

internal interface AuthService {
    fun signUp(name: String, email: String, password: String): AuthResponse
    fun signIn(email: String, password: String): AuthResponse
    fun getCurrentUser(userId: String): CurrentUserResponse
    fun signOut(refreshToken: String)
    fun refresh(refreshToken: String): AuthResponse
}
