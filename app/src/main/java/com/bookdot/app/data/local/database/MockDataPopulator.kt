package com.bookdot.app.data.local.database

import com.bookdot.app.data.local.dao.UserDao
import com.bookdot.app.data.local.dao.PostDao
import com.bookdot.app.data.local.dao.CommentDao
import com.bookdot.app.data.local.entities.UserEntity
import com.bookdot.app.data.local.entities.PostEntity
import com.bookdot.app.data.local.entities.CommentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockDataPopulator @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao
) {
    
    suspend fun populateDatabase() {
        // 이미 데이터가 있으면 추가하지 않음
        val existingUsers = userDao.getUserById("1")
        if (existingUsers != null) return
        
        // Mock Users
        val mockUsers = listOf(
            UserEntity(
                id = "1",
                username = "bootdot_user",
                displayName = "Boot Dot User",
                avatarUrl = null,
                bio = "Book dot에서 나만의 이야기를 써내려가고 있습니다 📚",
                followerCount = 120,
                followingCount = 45,
                postCount = 8,
                isFollowing = false,
                createdAt = System.currentTimeMillis() - 86400000 // 1일 전
            ),
            UserEntity(
                id = "2", 
                username = "mobile_dev",
                displayName = "모바일 개발자",
                avatarUrl = null,
                bio = "Android 개발을 사랑하는 개발자입니다 📱",
                followerCount = 89,
                followingCount = 67,
                postCount = 12,
                isFollowing = true,
                createdAt = System.currentTimeMillis() - 172800000 // 2일 전
            ),
            UserEntity(
                id = "3",
                username = "design_guru", 
                displayName = "디자인 구루",
                avatarUrl = null,
                bio = "UI/UX 디자인과 사용자 경험을 연구합니다 ✨",
                followerCount = 234,
                followingCount = 89,
                postCount = 15,
                isFollowing = false,
                createdAt = System.currentTimeMillis() - 259200000 // 3일 전
            )
        )
        
        // Mock Posts
        val mockPosts = listOf(
            PostEntity(
                id = "1",
                userId = "1",
                content = "안녕하세요! Boot dot 앱을 테스트 중입니다. 정말 멋진 앱이 될 것 같아요! 🚀\n\n모든 기능이 순조롭게 작동하고 있습니다.",
                imageUrls = emptyList(),
                videoUrl = null,
                likeCount = 42,
                commentCount = 2, // c1, c2 댓글
                createdAt = System.currentTimeMillis() - 3600000 // 1시간 전
            ),
            PostEntity(
                id = "2",
                userId = "2",
                content = "Android 개발 팁을 공유합니다! 📱\n\nKotlin과 Jetpack Compose를 사용하면 정말 효율적인 앱을 만들 수 있어요. Clean Architecture 패턴도 추천합니다!",
                imageUrls = emptyList(),
                videoUrl = null,
                likeCount = 67,
                commentCount = 1, // c3 댓글
                createdAt = System.currentTimeMillis() - 7200000 // 2시간 전
            ),
            PostEntity(
                id = "3",
                userId = "1",
                content = "오늘 날씨가 정말 좋네요! ☀️ 이런 날에는 밖에 나가서 산책하는 것이 최고입니다.",
                imageUrls = emptyList(),
                videoUrl = null,
                likeCount = 28,
                commentCount = 0,
                createdAt = System.currentTimeMillis() - 14400000 // 4시간 전
            ),
            PostEntity(
                id = "4",
                userId = "3",
                content = "UI 디자인 트렌드 2025! ✨\n\n1. 미니멀 디자인\n2. 다크 모드 지원\n3. 마이크로 인터랙션\n4. 접근성 향상\n\n이런 요소들이 중요해지고 있어요.",
                imageUrls = emptyList(),
                videoUrl = null,
                likeCount = 89,
                commentCount = 1, // c4 댓글
                createdAt = System.currentTimeMillis() - 21600000 // 6시간 전
            ),
            PostEntity(
                id = "5",
                userId = "2",
                content = "코드 리뷰의 중요성에 대해 이야기해볼까요? 🤔\n\n좋은 코드 리뷰는 팀의 코드 품질을 높이고, 지식 공유도 촉진합니다.",
                imageUrls = emptyList(),
                videoUrl = null,
                likeCount = 156,
                commentCount = 0,
                createdAt = System.currentTimeMillis() - 28800000 // 8시간 전
            )
        )
        
        // Mock Comments
        val mockComments = listOf(
            CommentEntity(
                id = "c1",
                postId = "1",
                userId = "2",
                content = "정말 멋진 포스트네요! 👍",
                likeCount = 3,
                createdAt = System.currentTimeMillis() - 1800000 // 30분 전
            ),
            CommentEntity(
                id = "c2", 
                postId = "1",
                userId = "3",
                content = "동감합니다! Boot dot 앱이 정말 잘 만들어진 것 같아요.",
                likeCount = 1,
                createdAt = System.currentTimeMillis() - 900000 // 15분 전
            ),
            CommentEntity(
                id = "c3",
                postId = "2",
                userId = "1",
                content = "유용한 팁 감사합니다! Clean Architecture는 정말 중요하죠.",
                likeCount = 5,
                createdAt = System.currentTimeMillis() - 5400000 // 1.5시간 전
            ),
            CommentEntity(
                id = "c4",
                postId = "4",
                userId = "2",
                content = "UI 트렌드 정보 정말 도움이 됩니다. 특히 다크모드 지원 부분이 인상적이에요!",
                likeCount = 2,
                createdAt = System.currentTimeMillis() - 18000000 // 5시간 전
            )
        )
        
        // 데이터베이스에 삽입
        userDao.insertUsers(mockUsers)
        postDao.insertPosts(mockPosts)
        commentDao.insertComments(mockComments)
    }
}