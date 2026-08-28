package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.FrostedBrandGradient
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.InstaHeartRed

@Composable
fun TeaGramTopBar(
    onNewPostClick: () -> Unit,
    onDirectMessagesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedCanvas.copy(alpha = 0.90f))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Frosted Gradient Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_title_button")
                ) {
                    Text(
                        text = "TeaGram",
                        style = TextStyle(
                            brush = FrostedBrandGradient,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Feed selector",
                        modifier = Modifier.size(18.dp),
                        tint = FrostedTextPrimary.copy(alpha = 0.7f)
                    )
                }

                // Right action icons: Create, Notifications, Direct Messages
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onNewPostClick,
                        modifier = Modifier.testTag("top_bar_create_post")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddBox,
                            contentDescription = "Create post",
                            modifier = Modifier.size(24.dp),
                            tint = FrostedTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("top_bar_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(InstaHeartRed)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(24.dp),
                                tint = FrostedTextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDirectMessagesClick,
                        modifier = Modifier.testTag("top_bar_messages")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = InstaHeartRed,
                                    contentColor = Color.White
                                ) {
                                    Text("2", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = "Direct Messages",
                                modifier = Modifier.size(22.dp),
                                tint = FrostedTextPrimary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedCardBorderSubtle
            )
        }
    }
}
