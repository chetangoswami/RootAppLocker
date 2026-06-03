package eu.hxreborn.biometricapplock.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.hxreborn.biometricapplock.BuildConfig

@Composable
fun DebugBadge(modifier: Modifier = Modifier) {
    if (!BuildConfig.DEBUG) return
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = {
            Text(
                text = BuildConfig.BUILD_TYPE.uppercase(),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
        border = null,
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
    )
}
