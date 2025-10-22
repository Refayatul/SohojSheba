package com.bonfire.shohojsheba.models

import androidx.compose.ui.graphics.vector.ImageVector

data class Service(
    val id: String,
    val icon: ImageVector,
    val titleRes: Int,
    val subtitleRes: Int
)
