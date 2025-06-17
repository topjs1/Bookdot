package com.bookdot.app.data.remote.api

import com.bookdot.app.data.remote.dto.UserDto
import retrofit2.http.*

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): UserDto
    
    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): UserDto
    
    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): UserDto
    
    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): List<UserDto>
    
    @POST("users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String)
    
    @DELETE("users/{userId}/follow")
    suspend fun unfollowUser(@Path("userId") userId: String)
    
    @PUT("users/me")
    suspend fun updateProfile(@Body user: UserDto): UserDto
}