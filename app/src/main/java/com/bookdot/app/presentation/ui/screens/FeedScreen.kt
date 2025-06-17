package com.bookdot.app.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bookdot.app.presentation.ui.components.PostCard
import com.bookdot.app.presentation.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    authViewModel: com.bookdot.app.presentation.viewmodel.AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Handle error display
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = "Book",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Book dot",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate("signup") {
                                popUpTo("feed") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "로그아웃",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_post") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Create post",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.posts.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { uiState.posts.size })
                
                // 책 배경 스타일
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = Color.White, // 흰색 배경
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp
                ) {
                    Column {
                        // 페이지 인디케이터
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${uiState.posts.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8B4513), // 갈색 텍스트
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Divider(
                            color = Color(0xFFD2B48C),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        // 페이지 콘텐츠
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) { page ->
                            BookPageContent(
                                post = uiState.posts[page],
                                onLike = { viewModel.likePost(uiState.posts[page].id) },
                                onComment = { navController.navigate("post/${uiState.posts[page].id}/comments") },
                                onUserClick = { navController.navigate("profile/${uiState.posts[page].user.id}") }
                            )
                        }
                    }
                }
            }
            
            // Loading Indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BookPageContent(
    post: com.bookdot.app.domain.model.Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 작성자 정보 (책의 제목처럼)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF8B4513).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = post.user.displayName.firstOrNull()?.toString() ?: "?",
                        color = Color(0xFF8B4513),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = post.user.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F4F4F) // 다크 슬레이트 그레이
                )
                Text(
                    text = formatBookTime(post.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF696969) // 딤 그레이
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 구분선 (책의 장 구분처럼)
        Divider(
            color = Color(0xFFD2B48C),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 포스트 내용 (책의 본문처럼)
        Text(
            text = post.content,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color(0xFF2F4F4F),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.5.sp
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 구분선
        Divider(
            color = Color(0xFFD2B48C),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 액션 버튼들 (책 하단의 각주처럼)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BookActionButton(
                icon = if (post.isLiked) "♥" else "♡",
                text = "${post.likeCount}",
                onClick = onLike,
                isActive = post.isLiked
            )
            BookActionButton(
                icon = "💬",
                text = "${post.commentCount}",
                onClick = onComment
            )
        }
    }
}

@Composable
private fun BookActionButton(
    icon: String,
    text: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) Color(0xFF8B4513).copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isActive) Color(0xFF8B4513) else Color(0xFF696969),
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

private fun formatBookTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        diff < 604800_000 -> "${diff / 86400_000}일 전"
        else -> {
            val sdf = java.text.SimpleDateFormat("MM월 dd일", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}