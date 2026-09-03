package com.dutaeko.shinigamireader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.ui.components.CompactMangaCard
import com.dutaeko.shinigamireader.ui.components.SectionTitle

@Composable
fun DiscoverScreen(
    items: List<Manga>,
    onOpenManga: (Manga) -> Unit,
    onSearch: (String, String) -> Unit,
    isLoading: Boolean,
    statusMessage: String,
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Trending") }
    val filters = listOf("Trending", "Latest", "Top Rated", "Action", "Romance", "Completed")

    LaunchedEffect(query, selectedFilter) {
        onSearch(query, selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            title = "Discover",
            subtitle = "Cari manga langsung dari parser Shinigami dengan filter cepat dan katalog grid",
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = if (isLoading) "Searching live source..." else statusMessage,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search title, author, or genre") },
            singleLine = true,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Smart Filters", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items) { manga ->
                CompactMangaCard(
                    manga = manga,
                    onClick = { onOpenManga(manga) },
                )
            }
        }
    }
}
