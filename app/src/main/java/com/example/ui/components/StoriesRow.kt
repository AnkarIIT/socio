package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoryEntity
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedStoryRingBrush
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.InstaLinkBlue

@Composable
fun StoriesRow(
    stories: List<StoryEntity>,
    onStoryClick: (StoryEntity) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("stories_row"),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(stories, key = { it.id }) { story ->
            StoryItem(
                story = story,
                onClick = {
                    if (story.isUserStory && !story.hasUnseen) {
                        onAddStoryClick()
                    } else {
                        onStoryClick(story)
                    }
                }
            )
        }
    }
}

@Composable
fun StoryItem(
    story: StoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(70.dp)
            .clickable(onClick = onClick)
            .testTag("story_item_${story.username}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(66.dp),
            contentAlignment = Alignment.Center
        ) {
            val ringModifier = if (story.hasUnseen) {
                Modifier
                    .size(66.dp)
                    .border(
                        width = 2.dp,
                        brush = FrostedStoryRingBrush,
                        shape = CircleShape
                    )
            } else {
                Modifier
                    .size(66.dp)
                    .border(
                        width = 1.dp,
                        color = FrostedCardBorder,
                        shape = CircleShape
                    )
            }

            Box(
                modifier = ringModifier
                    .padding(2.5.dp)
                    .border(2.dp, FrostedCanvas, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Story Avatar Image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(FrostedSurfaceVariantSolid)
                ) {
                    TeaGramImage(
                        model = story.avatarUrl,
                        contentDescription = "${story.username} story",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // If user story and no unseen, show blue "+" badge
            if (story.isUserStory && !story.hasUnseen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 2.dp, end = 2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(FrostedCanvas)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(InstaLinkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to story",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Text(
            text = story.username,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = FrostedTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
        )
    }
}
