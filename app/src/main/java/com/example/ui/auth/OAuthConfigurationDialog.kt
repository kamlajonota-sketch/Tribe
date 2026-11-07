package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardShape

@Composable
fun OAuthConfigurationDialog(
    reason: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Google Sign-In Configuration Required"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Setup Checklist for Real Google Sign-In:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                ConfigStepItem(
                    step = "1",
                    title = "Google Cloud Console",
                    details = "Create an OAuth 2.0 Web Client ID in your Google Cloud Project credentials."
                )

                ConfigStepItem(
                    step = "2",
                    title = "Android OAuth Client",
                    details = "Package: com.aistudio.tribe.travel\nSHA-1: FC:4F:00:47:C7:1C:25:DA:E2:BB:2B:D3:CD:07:0D:01:D5:CE:7D:18"
                )

                ConfigStepItem(
                    step = "3",
                    title = "Supabase Auth Google Provider",
                    details = "In Supabase Dashboard > Authentication > Providers > Google, enable Google and paste your Client ID and Client Secret."
                )

                ConfigStepItem(
                    step = "4",
                    title = "Authorized Redirect URI",
                    details = "Add 'https://cididsanututlefebehw.supabase.co/auth/v1/callback' to Authorized Redirect URIs in Google Cloud Console."
                )

                ConfigStepItem(
                    step = "5",
                    title = "App Secret Configuration",
                    details = "Set GOOGLE_WEB_CLIENT_ID=<your-web-client-id> in your .env file or the AI Studio Secrets panel."
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("oauth_dialog_got_it_button")
            ) {
                Text(
                    text = "Got It",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        modifier = modifier.testTag("oauth_configuration_dialog")
    )
}

@Composable
private fun ConfigStepItem(
    step: String,
    title: String,
    details: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Step $step: $title",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        )
    }
}
