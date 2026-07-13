package com.bonfire.shohojsheba.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun DecorativeBackground(
    content: @Composable () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = Modifier.fillMaxSize()) {
        // Decorative Canvas with subtle circles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            val width = size.width
            val height = size.height
            val radius = 60.dp.toPx()

            // Top right circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.03f),
                radius = radius,
                center = Offset(width * 0.8f, height * 0.1f)
            )
            
            // Bottom left circle
            drawCircle(
                color = secondaryColor.copy(alpha = 0.03f),
                radius = radius * 0.8f,
                center = Offset(width * 0.2f, height * 0.9f)
            )
            
            // Middle right circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.02f),
                radius = radius * 1.2f,
                center = Offset(width * 0.9f, height * 0.5f)
            )
            
            // Top left circle
            drawCircle(
                color = secondaryColor.copy(alpha = 0.025f),
                radius = radius * 0.6f,
                center = Offset(width * 0.1f, height * 0.2f)
            )
        }

        // Content on top
        content()
    }
}
