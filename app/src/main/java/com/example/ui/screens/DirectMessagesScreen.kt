package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread
import com.example.ui.components.TeaGramImage
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.InstaHeartRed
import com.example.ui.theme.InstaLinkBlue

@Composable
fun DirectMessagesScreen(
    currentUsername: String,
    threads: List<ChatThread>,
    onBackClick: () -> Unit,
    onThreadClick: (ChatThread) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredThreads = remember(threads, searchQuery) {
        if (searchQuery.isBlank()) threads
        else threads.filter {
            it.partnerFullName.contains(searchQuery, ignoreCase = true) ||
                    it.partnerUsername.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvas)
            .statusBarsPadding()
            .testTag("direct_messages_screen")
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("dm_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FrostedTextPrimary
                    )
                }

                Text(
                    text = currentUsername,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New message",
                    tint = FrostedTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2. Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search", fontSize = 14.sp, color = FrostedTextSecondary)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FrostedTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dm_search_input"),
                shape = RoundedCornerShape(14.dp),
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
        }

        // 3. Notes / Active Status Tray
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Your note
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(68.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, FrostedCardBorder, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(FrostedSurfaceVariantSolid),
                        contentAlignment = Alignment.Center
                    ) {
                        TeaGramImage(
                            model = "img_feed_portrait",
                            contentDescription = "My Note",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "Your note",
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostedTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            items(threads) { thread ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clickable { onThreadClick(thread) }
                ) {
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .border(1.dp, FrostedCardBorder, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(FrostedSurfaceVariantSolid)
                        ) {
                            TeaGramImage(
                                model = thread.partnerAvatarResName,
                                contentDescription = thread.partnerUsername,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (thread.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .border(2.dp, FrostedCanvas, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                    Text(
                        text = thread.partnerUsername,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = FrostedTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = FrostedDivider
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary
            )
            Text(
                text = "Requests",
                style = MaterialTheme.typography.titleSmall,
                color = FrostedAccentIce,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 4. Threads List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredThreads, key = { it.conversationId }) { thread ->
                ChatThreadRow(
                    thread = thread,
                    onClick = { onThreadClick(thread) }
                )
            }
        }
    }
}

@Composable
private fun ChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("dm_thread_${thread.partnerUsername}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .border(1.dp, FrostedCardBorderSubtle, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(FrostedSurfaceVariantSolid)
                ) {
                    TeaGramImage(
                        model = thread.partnerAvatarResName,
                        contentDescription = thread.partnerUsername,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (thread.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .border(2.dp, FrostedCanvas, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = thread.partnerFullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    color = FrostedTextPrimary
                )
                Text(
                    text = "${thread.lastMessage} • ${thread.timestampFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (thread.unreadCount > 0) FrostedTextPrimary else FrostedTextSecondary,
                    fontWeight = if (thread.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (thread.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FrostedAccentIce)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Send camera shot",
                tint = FrostedTextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

