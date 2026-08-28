package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TeaGramImage
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedAccentIceDark
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.InstaLinkBlue

enum class TeaGramFilter(val displayName: String) {
    NORMAL("Normal"),
    CLARENDON("Clarendon"),
    VINTAGE("Vintage"),
    MONO("Mono"),
    WARM("Warm"),
    COOL("Cool")
}

fun TeaGramFilter.colorFilter(): ColorFilter? {
    val matrix = when (this) {
        TeaGramFilter.NORMAL -> ColorMatrix()
        TeaGramFilter.CLARENDON -> ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.15f, 0f, 0f, 8f,
                0f, 0f, 1.1f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        TeaGramFilter.VINTAGE -> ColorMatrix(
            floatArrayOf(
                0.6f, 0.3f, 0.1f, 0f, 10f,
                0.2f, 0.7f, 0.1f, 0f, 10f,
                0.1f, 0.2f, 0.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        TeaGramFilter.MONO -> ColorMatrix(
            floatArrayOf(
                0.2126f, 0.7152f, 0.0722f, 0f, 0f,
                0.2126f, 0.7152f, 0.0722f, 0f, 0f,
                0.2126f, 0.7152f, 0.0722f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        TeaGramFilter.WARM -> ColorMatrix(
            floatArrayOf(
                1.1f, 0f, 0f, 0f, 15f,
                0f, 1f, 0f, 0f, 5f,
                0f, 0f, 0.9f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        TeaGramFilter.COOL -> ColorMatrix(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, -5f,
                0f, 1f, 0f, 0f, 5f,
                0f, 0f, 1.1f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
    return ColorFilter.colorMatrix(matrix)
}

@Composable
fun CreatePostScreen(
    onDismiss: () -> Unit,
    onSharePost: (imageRes: String, caption: String, location: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetImages = listOf(
        "img_feed_travel" to "Santorini Coast",
        "img_feed_coffee" to "Cozy Morning Coffee",
        "img_feed_arch" to "Modern Geometric Pavillion",
        "img_feed_portrait" to "Golden Hour Portrait"
    )

    var selectedImageRes by remember { mutableStateOf(presetImages[0].first) }
    var selectedFilter by remember { mutableStateOf(TeaGramFilter.NORMAL) }
    var captionText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageRes = uri.toString()
        }
    }

    val locationSuggestions = listOf(
        "San Francisco, California",
        "Oia, Santorini, Greece",
        "Copenhagen, Denmark",
        "SoHo, New York City",
        "Tokyo, Japan"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("create_post_close")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = FrostedTextPrimary
                )
            }

            Text(
                text = "New post",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary
            )

            TextButton(
                onClick = {
                    val finalCaption = if (captionText.isBlank()) "Captured a new perspective ✨" else captionText.trim()
                    val finalLocation = locationText.ifBlank { null }
                    onSharePost(selectedImageRes, finalCaption, finalLocation)
                },
                modifier = Modifier.testTag("create_post_share_button")
            ) {
                Text(
                    text = "Share",
                    color = FrostedAccentIce,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = FrostedDivider
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 2. Selected Photo Preview with Filter effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(FrostedCardSolid)
            ) {
                TeaGramImage(
                    model = selectedImageRes,
                    contentDescription = "Selected post photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = selectedFilter.colorFilter()
                )

                // Pick from device gallery overlay button
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FrostedCardSolid.copy(alpha = 0.85f))
                        .border(1.dp, FrostedCardBorderSubtle, CircleShape)
                        .testTag("pick_custom_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Choose from gallery",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Preset Photo Selection Strip
            Text(
                text = "Choose Photo",
                style = MaterialTheme.typography.titleSmall,
                color = FrostedTextPrimary,
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presetImages) { (resName, title) ->
                    val isSelected = selectedImageRes == resName
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedImageRes = resName }
                            .testTag("preset_image_$resName")
                    ) {
                        val borderMod = if (isSelected) {
                            Modifier.border(2.dp, FrostedAccentIce, RoundedCornerShape(10.dp))
                        } else {
                            Modifier.border(1.dp, FrostedCardBorderSubtle, RoundedCornerShape(10.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .then(borderMod)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FrostedSurfaceVariantSolid)
                        ) {
                            TeaGramImage(
                                model = resName,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = title.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isSelected) FrostedAccentIce else FrostedTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 4. Filters Row
            Text(
                text = "Filter: ${selectedFilter.displayName}",
                style = MaterialTheme.typography.titleSmall,
                color = FrostedTextPrimary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TeaGramFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) FrostedAccentIce else FrostedCardSolid
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else FrostedCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_${filter.name}")
                    ) {
                        Text(
                            text = filter.displayName,
                            color = if (isSelected) FrostedAccentIceDark else FrostedTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 5. Caption Input
            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                placeholder = {
                    Text("Write a caption...", color = FrostedTextSecondary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("create_post_caption_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FrostedAccentIce,
                    unfocusedBorderColor = FrostedCardBorder,
                    focusedContainerColor = FrostedCardSolid,
                    unfocusedContainerColor = FrostedCardSolid,
                    focusedTextColor = FrostedTextPrimary,
                    unfocusedTextColor = FrostedTextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Quick hashtags
            val hashtags = listOf("#aesthetic", "#photography", "#travelgram", "#minimalism", "#vsco")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(hashtags) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(FrostedCardSolid)
                            .border(1.dp, FrostedCardBorderSubtle, RoundedCornerShape(16.dp))
                            .clickable {
                                captionText = if (captionText.isBlank()) tag else "$captionText $tag"
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tag,
                            color = FrostedAccentIce,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Location Tagging
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = FrostedAccentIce,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    placeholder = { Text("Add location", fontSize = 13.sp, color = FrostedTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_post_location_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder,
                        focusedContainerColor = FrostedCardSolid,
                        unfocusedContainerColor = FrostedCardSolid,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary
                    )
                )
            }

            // Quick location suggestions
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(locationSuggestions) { loc ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrostedCardSolid)
                            .border(1.dp, FrostedCardBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { locationText = loc }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = loc,
                            color = FrostedTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
