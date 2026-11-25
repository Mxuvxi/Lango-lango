package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun LangoAvatar(
    photoPath: String,
    displayName: String,
    modifier: Modifier = Modifier,
    previewUri: Uri? = null,
    size: Dp = 48.dp
) {
    val model: Any? = when {
        previewUri != null -> previewUri
        photoPath.isNotBlank() && File(photoPath).exists() -> File(photoPath)
        else -> null
    }
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface)
    ) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Photo de profil de $displayName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (initial.isNotEmpty()) {
                    Text(
                        text = initial,
                        style = if (size >= 72.dp) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
