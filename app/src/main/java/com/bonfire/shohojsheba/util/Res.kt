package com.bonfire.shohojsheba.util

import android.content.Context

fun Context.drawableId(name: String): Int =
    resources.getIdentifier(name, "drawable", packageName)


//Use context.drawableId(service.iconName) in your adapter instead of service.iconRes.
//
//For detail images:
//
//val ids = detail.imageNames.split(',').mapNotNull { n ->
//    val id = context.drawableId(n.trim())
//    if (id != 0) id else null
//}