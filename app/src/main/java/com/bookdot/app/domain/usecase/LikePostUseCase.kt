package com.bookdot.app.domain.usecase

import com.bookdot.app.domain.repository.PostRepository
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, isLiked: Boolean): Result<Unit> {
        return if (isLiked) {
            postRepository.unlikePost(postId)
        } else {
            postRepository.likePost(postId)
        }
    }
}