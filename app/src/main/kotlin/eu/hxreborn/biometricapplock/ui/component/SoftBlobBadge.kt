package com.example.rootapplocker.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SoftBlobBadge(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier.size(size),
        shape = shape,
        color = containerColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
