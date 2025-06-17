package com.bookdot.app.domain.usecase

import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(): Flow<List<Post>> {
        return postRepository.getFeed()
    }
}