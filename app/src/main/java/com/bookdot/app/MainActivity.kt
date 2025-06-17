package com.bookdot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.bookdot.app.presentation.ui.screens.*
import com.bookdot.app.presentation.ui.theme.BootDotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BootDotTheme {
                BootDotApp()
            }
        }
    }
}

@Composable
fun BootDotApp() {
    val navController = rememberNavController()
    val authViewModel: com.bookdot.app.presentation.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    // 항상 signup 화면부터 시작 (자동 로그인 비활성화)
    val startDestination = "signup"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("profile_setup") {
            ProfileSetupScreen(navController = navController)
        }
        
        composable("feed") {
            FeedScreen(navController = navController)
        }
        
        composable("create_post") {
            CreatePostScreen(navController = navController)
        }
        
        composable("post/{postId}/comments") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentsScreen(
                postId = postId,
                navController = navController
            )
        }
        
        composable("profile/{userId}") {
            // TODO: Implement ProfileScreen
        }
    }
}