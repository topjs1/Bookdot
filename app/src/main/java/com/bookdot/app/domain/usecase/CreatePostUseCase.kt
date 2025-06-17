package com.bookdot.app.domain.usecase

import android.net.Uri
import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(content: String, images: List<Uri>): Result<Post> {
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Post content cannot be empty"))
        }
        return postRepository.createPost(content, images)
    }
}