package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread
import com.example.data.model.PostEntity
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedAccentIceDark
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    post: PostEntity,
    friends: List<ChatThread>,
    onDismiss: () -> Unit,
    onSendToFriend: (String) -> Unit
) {
    val sentStatus = remember { mutableStateMapOf<String, Boolean>() }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FrostedCardSolid,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.testTag("share_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider
            )

            // Friends quick send row
            Text(
                text = "Send via Direct",
                style = MaterialTheme.typography.labelMedium,
                color = FrostedTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(friends) { friend ->
                    val isSent = sentStatus[friend.partnerUsername] == true
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(68.dp)
                            .clickable {
                                sentStatus[friend.partnerUsername] = !isSent
                                onSendToFriend(friend.partnerUsername)
                            }
                    ) {
                        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.dp, FrostedCardBorder, CircleShape)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(FrostedSurfaceVariantSolid)
                            ) {
                                TeaGramImage(
                                    model = friend.partnerAvatarResName,
                                    contentDescription = friend.partnerUsername,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (isSent) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(FrostedAccentIce.copy(alpha = 0.85f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        tint = FrostedAccentIceDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = friend.partnerUsername,
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostedTextPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = if (isSent) "Sent" else "Send",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSent) FrostedAccentIce else FrostedTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider
            )

            // Action rows: Add to story, Copy link, Share via...
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShareActionButton(
                    icon = Icons.Default.Add,
                    label = "Add to story",
                    onClick = {
                        Toast.makeText(context, "Added to your story", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
                ShareActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy link",
                    onClick = {
                        val shareText = "Check this out on TeaGram: ${post.authorUsername} - ${post.caption.take(50)}"
                        val clip = ClipData.newPlainText("TeaGram", shareText)
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
                ShareActionButton(
                    icon = Icons.Default.Share,
                    label = "Share to...",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check this out on TeaGram: ${post.authorUsername} - ${post.caption.take(100)}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FrostedSurfaceVariantSolid)
                .border(1.dp, FrostedCardBorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = FrostedTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FrostedTextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

