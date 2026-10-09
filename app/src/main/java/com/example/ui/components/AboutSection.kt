package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.config.DeveloperProfile
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Clean, professional "About Regional Voice AI" component.
 *
 * Displays:
 * - App Name & Tagline
 * - Developed by SHAMEEM KM
 * - Instagram @ime3m (clickable, launches native app or web profile)
 * - Current Application Version (automatically read from BuildConfig)
 * - Privacy Policy, Terms of Service, Send Feedback links
 *
 * Fully supports Light mode, Dark mode, and System theme using the existing theme system.
 */
@Composable
fun AboutRegionalVoiceAIContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("about_regional_voice_ai_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FuturisticTheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Crest / Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.accent.copy(alpha = 0.14f))
                    .border(1.5.dp, FuturisticTheme.accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Name
            Text(
                text = DeveloperProfile.appName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FuturisticTheme.primaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("about_app_name")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = DeveloperProfile.appTagline,
                style = MaterialTheme.typography.bodyMedium,
                color = FuturisticTheme.secondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("about_app_tagline")
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.8.dp, color = FuturisticTheme.border.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // DEVELOPED BY SECTION
            // =================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Developed by",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = FuturisticTheme.secondaryText,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = DeveloperProfile.developerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText,
                    fontSize = 17.sp,
                    modifier = Modifier.testTag("about_developer_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Instagram",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = FuturisticTheme.secondaryText,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Clickable Instagram Badge: 📷 @ime3m
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            DeveloperProfile.openInstagram(context)
                        }
                        .testTag("about_instagram_link"),
                    color = FuturisticTheme.surfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Camera Icon / 📷 representation
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Instagram Camera",
                                    tint = FuturisticTheme.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "📷 ${DeveloperProfile.instagramUsername}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FuturisticTheme.primaryText,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Tap to open official profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FuturisticTheme.secondaryText,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Instagram Profile",
                            tint = FuturisticTheme.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(thickness = 0.8.dp, color = FuturisticTheme.border.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // VERSION SECTION (Automatically read from BuildConfig)
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_version_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = FuturisticTheme.secondaryText,
                    fontSize = 14.sp
                )

                // Current app version dynamically read from Android build configuration
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FuturisticTheme.accent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FuturisticTheme.accent,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(thickness = 0.8.dp, color = FuturisticTheme.border.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // PRIVACY POLICY, TERMS OF SERVICE, SEND FEEDBACK
            // =================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Privacy Policy Link
                AboutActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Read our user privacy & data protection policy",
                    testTag = "about_privacy_policy_button",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DeveloperProfile.privacyPolicyUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Privacy Policy: ${DeveloperProfile.privacyPolicyUrl}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Terms of Service Link
                AboutActionRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Usage terms, guidelines & service conditions",
                    testTag = "about_terms_button",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DeveloperProfile.termsUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Terms: ${DeveloperProfile.termsUrl}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Send Feedback Link
                AboutActionRow(
                    icon = Icons.Default.Feedback,
                    title = "Send Feedback",
                    subtitle = "Contact developer & share your suggestions",
                    testTag = "about_feedback_button",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${DeveloperProfile.supportEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Regional Voice AI Feedback")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Feedback: ${DeveloperProfile.supportEmail}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FuturisticTheme.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FuturisticTheme.secondaryText,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = FuturisticTheme.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
