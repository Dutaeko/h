package com.dutaeko.shinigamireader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dutaeko.shinigamireader.core.model.Chapter
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.ReaderPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    manga: Manga,
    chapter: Chapter,
    preferences: ReaderPreferences,
    onBack: () -> Unit,
    onNextChapter: () -> Unit,
    isLoading: Boolean,
    sourceStatusMessage: String,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text(chapter.title, fontWeight = FontWeight.Bold)
                    Text(manga.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Reader Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sourceStatusMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AssistChip(onClick = { }, label = { Text(preferences.readingMode.name) })
                            AssistChip(onClick = { }, label = { Text("Brightness ${preferences.brightnessPercent}%") })
                            AssistChip(onClick = { }, label = { Text("Spacing ${preferences.pageSpacingPercent}%") })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AssistChip(onClick = { }, label = { Text(if (preferences.showComments) "Comments On" else "Comments Off") })
                            AssistChip(onClick = { }, label = { Text(if (preferences.cropBorders) "Crop Borders" else "Original Margins") })
                            AssistChip(onClick = { }, label = { Text(if (preferences.keepScreenOn) "Keep Awake" else "Auto Sleep") })
                        }
                    }
                }
            }

            if (isLoading && chapter.pages.isEmpty()) {
                item {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            text = "Halaman chapter sedang dimuat dari parser Shinigami...",
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(chapter.pages) { page ->
                AsyncImage(
                    model = page.imageUrl,
                    contentDescription = "Page ${page.index}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.FillWidth,
                )
            }

            if (!isLoading && chapter.pages.isEmpty()) {
                item {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            text = "Belum ada halaman yang bisa ditampilkan untuk chapter ini.",
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Button(onClick = onNextChapter, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Chapter")
                    Icon(Icons.Outlined.NavigateNext, contentDescription = null)
                }
            }
        }
    }
}
