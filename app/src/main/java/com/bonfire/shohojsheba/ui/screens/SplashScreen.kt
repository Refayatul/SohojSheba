package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.theme.SplashBackground
import com.bonfire.shohojsheba.ui.theme.SplashLogoGreen
import com.bonfire.shohojsheba.ui.theme.SplashTextTeal
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(true) {
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground), // Solid light background like the image
        contentAlignment = Alignment.Center
    ) {
        // Main content column (Logo, Name, Tagline)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box to stack the Canvas logo and the Text on top
            Box(contentAlignment = Alignment.Center) {
                // 1. Draw the Shape (Circle + Pentagon)
                AppLogoCanvas(modifier = Modifier.size(160.dp))

                // 2. The Text inside the Pentagon
                Text(
                    text = "সেবা",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(top = 8.dp) // Slight adjustment to center visually in pentagon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SplashTextTeal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = stringResource(id = R.string.tagline),
                fontSize = 16.sp,
                color = SplashTextTeal.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }

        // Version number aligned to the bottom
        val versionNumber = stringResource(id = R.string.version_number)
        val versionName = stringResource(id = R.string.version_name)
        val annotatedString = buildAnnotatedString {
            append("${stringResource(id = R.string.version_label)} $versionNumber")
            withStyle(style = SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 10.sp)) {
                append(versionName)
            }
        }
        Text(
            text = annotatedString,
            fontSize = 14.sp,
            color = SplashTextTeal.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun AppLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.05f
        val radiusOuter = (size.width / 2) - (strokeWidth / 2)

        // 1. Outer green ring
        drawCircle(
            color = SplashLogoGreen,
            radius = radiusOuter,
            style = Stroke(width = strokeWidth)
        )

        // 2. Inner white circle background
        drawCircle(
            color = Color.White,
            radius = radiusOuter - (strokeWidth / 2)
        )

        // 3. Green pentagon path (Filled)
        val pentagonSize = size.width * 0.55f // Adjust size of pentagon relative to circle
        val pentagonPath = Path().apply {
            val angle = 2 * Math.PI / 5
            val radius = pentagonSize / 2
            val cx = size.center.x
            val cy = size.center.y + 10f // Push slightly down to center visually

            // Start at top point
            moveTo(
                x = cx + radius * cos(-Math.PI / 2).toFloat(),
                y = cy + radius * sin(-Math.PI / 2).toFloat()
            )
            for (i in 1..4) {
                lineTo(
                    x = cx + radius * cos(-Math.PI / 2 + angle * i).toFloat(),
                    y = cy + radius * sin(-Math.PI / 2 + angle * i).toFloat()
                )
            }
            close()
        }

        // Draw filled pentagon
        drawPath(path = pentagonPath, color = SplashLogoGreen, style = Fill)
    }
}