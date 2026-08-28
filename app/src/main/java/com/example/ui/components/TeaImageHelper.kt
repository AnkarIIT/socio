package com.example.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

object ImageHelper {
    fun getDrawableResId(name: String): Int? {
        val cleanName = name.removePrefix("drawable:").removeSuffix(".jpg").removeSuffix(".png")
        return when (cleanName) {
            "img_feed_travel" -> R.drawable.img_feed_travel
            "img_feed_coffee" -> R.drawable.img_feed_coffee
            "img_feed_arch" -> R.drawable.img_feed_arch
            "img_feed_portrait" -> R.drawable.img_feed_portrait
            "img_app_icon" -> R.drawable.img_app_icon
            else -> null
        }
    }
}

@Composable
fun TeaGramImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    colorFilter: ColorFilter? = null
) {
    val drawableId = ImageHelper.getDrawableResId(model)
    if (drawableId != null) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        )
    } else {
        // Support Coil for web urls or file uris, with fallback
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter,
            error = painterResource(id = R.drawable.img_feed_travel),
            placeholder = painterResource(id = R.drawable.img_feed_travel)
        )
    }
}
