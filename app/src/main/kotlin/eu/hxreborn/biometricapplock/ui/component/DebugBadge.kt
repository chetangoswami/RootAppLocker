package eu.hxreborn.biometricapplock.ui.component

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import eu.hxreborn.biometricapplock.BuildConfig

@Composable
fun DebugBadge(modifier: Modifier = Modifier) {
    if (!BuildConfig.DEBUG) return
    val context = LocalContext.current
    AssistChip(
        modifier = modifier,
        onClick = {
            Toast
                .makeText(
                    context,
                    "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                        "${BuildConfig.GIT_SHA} · ${BuildConfig.BUILD_TIME}",
                    Toast.LENGTH_LONG,
                ).show()
        },
        label = {
            Text(
                text = "${BuildConfig.BUILD_TYPE.uppercase()} · ${BuildConfig.GIT_SHA}",
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
