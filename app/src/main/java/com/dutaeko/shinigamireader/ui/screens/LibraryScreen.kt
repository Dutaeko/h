package com.dutaeko.shinigamireader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.ui.components.LibraryRow
import com.dutaeko.shinigamireader.ui.components.SectionTitle

@Composable
fun LibraryScreen(
    items: List<LibraryEntry>,
    onOpenManga: (LibraryEntry) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Reading", "Completed", "Offline")

    val filteredItems = items.filter { entry ->
        when (selectedFilter) {
            "Reading" -> entry.state.name == "READING"
            "Completed" -> entry.state.name == "COMPLETED"
            "Offline" -> entry.downloadedCount > 0
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(
            title = "Your Library",
            subtitle = "Kelola status baca, unread chapter, dan chapter offline dalam satu dashboard",
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(filters) { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(filteredItems) { entry ->
                LibraryRow(entry = entry, onClick = { onOpenManga(entry) })
            }
            if (filteredItems.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada item untuk filter ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
