package com.bookdot.app.data.remote.api

import com.bookdot.app.data.remote.dto.CreatePostRequest
import com.bookdot.app.data.remote.dto.PostDto
import com.bookdot.app.data.remote.dto.LikeResponse
import retrofit2.http.*

interface PostApi {
    @GET("posts/feed")
    suspend fun getFeed(): List<PostDto>
    
    @GET("posts/user/{userId}")
    suspend fun getPostsByUser(@Path("userId") userId: String): List<PostDto>
    
    @GET("posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: String): PostDto
    
    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): PostDto
    
    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): LikeResponse
    
    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: String): LikeResponse
    
    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String)
}