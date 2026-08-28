package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentEntity
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.util.TimeAgo
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.InstaHeartRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    comments: List<CommentEntity>,
    userAvatar: String,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onToggleLike: (CommentEntity) -> Unit,
    sheetState: SheetState
) {
    var inputText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FrostedCardSolid,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.testTag("comments_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider
            )

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No comments yet.",
                            style = MaterialTheme.typography.titleSmall,
                            color = FrostedTextPrimary
                        )
                        Text(
                            text = "Start the conversation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FrostedTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            onToggleLike = { onToggleLike(comment) }
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider
            )

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, FrostedCardBorderSubtle, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(FrostedSurfaceVariantSolid)
                ) {
                    TeaGramImage(
                        model = userAvatar,
                        contentDescription = "My avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Add a comment...", fontSize = 13.sp, color = FrostedTextSecondary)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder,
                        focusedContainerColor = FrostedCardSolid,
                        unfocusedContainerColor = FrostedCardSolid,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary
                    ),
                    singleLine = true
                )

                if (inputText.isNotBlank()) {
                    TextButton(
                        onClick = {
                            onAddComment(inputText)
                            inputText = ""
                        },
                        modifier = Modifier.testTag("post_comment_button")
                    ) {
                        Text(
                            text = "Post",
                            color = FrostedAccentIce,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: CommentEntity,
    onToggleLike: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, FrostedCardBorderSubtle, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(FrostedSurfaceVariantSolid)
            ) {
                TeaGramImage(
                    model = comment.authorAvatarUrl,
                    contentDescription = comment.authorUsername,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorUsername,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = TimeAgo.format(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = FrostedTextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FrostedTextPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostedTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (comment.likesCount > 0) {
                        Text(
                            text = "${comment.likesCount} likes",
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostedTextMuted
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like comment",
                tint = if (comment.isLiked) InstaHeartRed else FrostedTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

