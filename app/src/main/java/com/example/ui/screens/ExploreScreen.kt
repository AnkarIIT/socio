package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.ui.components.TeaGramImage
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedAccentIceDark
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary

data class ExploreTile(
    val id: Long,
    val imageResName: String,
    val isReel: Boolean = false,
    val hasMultiple: Boolean = false,
    val category: String = "For you",
    val title: String = "",
    val correspondingPost: PostEntity? = null
)

@Composable
fun ExploreScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    posts: List<PostEntity>,
    onPostClick: (PostEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("For you", "Architecture", "Travel", "Coffee", "Portraits", "Design", "Nature")

    // Build rich explore grid tiles
    val exploreTiles = remember(posts) {
        val baseList = mutableListOf<ExploreTile>()
        // Map real posts into explore tiles
        posts.forEachIndexed { index, post ->
            val cat = when (post.imageResName) {
                "img_feed_travel" -> "Travel"
                "img_feed_arch" -> "Architecture"
                "img_feed_coffee" -> "Coffee"
                "img_feed_portrait" -> "Portraits"
                else -> "For you"
            }
            baseList.add(
                ExploreTile(
                    id = post.id,
                    imageResName = post.imageResName,
                    isReel = index % 2 == 1,
                    hasMultiple = index % 3 == 0,
                    category = cat,
                    title = post.caption,
                    correspondingPost = post
                )
            )
        }
        // Additional aesthetic tiles for full grid
        val placeholders = listOf(
            ExploreTile(101, "img_feed_travel", isReel = true, category = "Travel", title = "Cycladic sunset"),
            ExploreTile(102, "img_feed_coffee", isReel = false, category = "Coffee", title = "Roast profile"),
            ExploreTile(103, "img_feed_arch", isReel = true, category = "Architecture", title = "Brutalist facade"),
            ExploreTile(104, "img_feed_portrait", isReel = false, category = "Portraits", title = "Street candid"),
            ExploreTile(105, "img_feed_travel", isReel = false, hasMultiple = true, category = "Travel", title = "Island hopping"),
            ExploreTile(106, "img_feed_arch", isReel = false, category = "Architecture", title = "Shadow & light"),
            ExploreTile(107, "img_feed_coffee", isReel = true, category = "Coffee", title = "Morning brew"),
            ExploreTile(108, "img_feed_portrait", isReel = false, hasMultiple = true, category = "Portraits", title = "Film grain")
        )
        baseList + placeholders
    }

    val filteredTiles = remember(exploreTiles, searchQuery, selectedCategory) {
        exploreTiles.filter { tile ->
            val matchesCategory = selectedCategory == "For you" || tile.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    tile.title.contains(searchQuery, ignoreCase = true) ||
                    tile.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvas)
            .statusBarsPadding()
    ) {
        // 1. Frosted Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text("Search", fontSize = 14.sp, color = FrostedTextSecondary)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = FrostedTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = FrostedTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("explore_search_bar"),
                shape = RoundedCornerShape(16.dp),
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

        // 2. Category Chips with Frosted Theme
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FrostedAccentIce,
                        selectedLabelColor = FrostedAccentIceDark,
                        containerColor = FrostedCardSolid,
                        labelColor = FrostedTextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = FrostedCardBorderSubtle,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("explore_chip_$category")
                )
            }
        }

        // 3. Grid of photos with frosted aesthetic
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp)
                .testTag("explore_grid"),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(filteredTiles, key = { "${it.id}_${it.imageResName}" }) { tile ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val targetPost = tile.correspondingPost ?: posts.firstOrNull { it.imageResName == tile.imageResName } ?: posts.firstOrNull()
                            if (targetPost != null) {
                                onPostClick(targetPost)
                            }
                        }
                        .testTag("explore_tile_${tile.id}")
                ) {
                    TeaGramImage(
                        model = tile.imageResName,
                        contentDescription = tile.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Badge top right
                    if (tile.isReel) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "Reel",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else if (tile.hasMultiple) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Collections,
                                contentDescription = "Carousel",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
