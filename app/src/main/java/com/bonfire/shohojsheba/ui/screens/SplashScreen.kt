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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.bonfire.shohojsheba.ui.theme.SplashGradientEnd
import com.bonfire.shohojsheba.ui.theme.SplashGradientStart
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

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(SplashGradientStart, SplashGradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // Main content column (Logo, Name, Tagline)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box to stack the Canvas logo and the Text on top
            Box(contentAlignment = Alignment.Center) {
                AppLogoCanvas(modifier = Modifier.size(150.dp))
                Text(
                    text = "সেবা", // This is part of the logo, so it doesn't need to be translated
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SplashTextTeal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.tagline),
                fontSize = 16.sp,
                color = SplashTextTeal,
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
            color = SplashTextTeal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}


@Composable
fun AppLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.06f

        // Outer green ring
        drawCircle(
            color = SplashLogoGreen,
            style = Stroke(width = strokeWidth)
        )

        // Inner white circle background
        drawCircle(
            color = Color.White,
            radius = (size.width / 2) - (strokeWidth / 2) // Overlap slightly for seamlessness
        )

        // Green pentagon path
        val pentagonSize = size.width * 0.6f
        val pentagonPath = Path().apply {
            val angle = 2 * Math.PI / 5
            val radius = pentagonSize / 2
            val cx = size.center.x
            val cy = size.center.y
            
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
        drawPath(path = pentagonPath, color = SplashLogoGreen)
    }
}
