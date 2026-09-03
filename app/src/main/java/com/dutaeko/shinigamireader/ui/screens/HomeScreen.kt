package com.dutaeko.shinigamireader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.ui.components.ContinueReadingCard
import com.dutaeko.shinigamireader.ui.components.HeroMangaCard
import com.dutaeko.shinigamireader.ui.components.MangaHorizontalList
import com.dutaeko.shinigamireader.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sections: List<HomeSection>,
    continueReading: LibraryEntry?,
    onOpenManga: (Manga) -> Unit,
    onResumeReading: (LibraryEntry) -> Unit,
    onDiscoverTap: () -> Unit,
    sourceStatusMessage: String,
    isRefreshing: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Shinigami Reader", fontWeight = FontWeight.Bold)
                    Text(
                        "Read smarter, faster, and more immersive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            actions = {
                IconButton(onClick = onDiscoverTap) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isRefreshing) "Syncing Source" else "Source Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = sourceStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            continueReading?.let { entry ->
                item {
                    ContinueReadingCard(
                        entry = entry,
                        onResume = { onResumeReading(entry) },
                    )
                }
            }

            sections.firstOrNull()?.items?.firstOrNull()?.let { heroManga ->
                item {
                    HeroMangaCard(
                        manga = heroManga,
                        onClick = { onOpenManga(heroManga) },
                    )
                }
            }

            items(sections) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(title = section.title, subtitle = section.subtitle)
                    MangaHorizontalList(items = section.items, onClick = onOpenManga)
                }
            }

            if (sections.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = "Belum ada data source yang berhasil dimuat. UI tetap siap dipakai dan akan otomatis memakai data saat parser tersambung.",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "UX highlight: feed dibuat ringkas, cover dominan, dan progress baca selalu terlihat agar user cepat kembali membaca.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                )
            }
        }
    }
}
