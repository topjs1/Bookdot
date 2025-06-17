package com.bookdot.app.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BootDotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkColors else MonochromeLightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BootDotTypography,
        content = content
    )
}