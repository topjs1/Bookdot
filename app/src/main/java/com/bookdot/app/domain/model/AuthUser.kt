package com.bookdot.app.domain.model

data class AuthUser(
    val accountId: String,
    val displayName: String,
    val isLoggedIn: Boolean = false,
    val createdAt: Long
)