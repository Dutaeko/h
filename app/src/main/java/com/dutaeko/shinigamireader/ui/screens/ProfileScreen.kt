package com.dutaeko.shinigamireader.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dutaeko.shinigamireader.core.model.ReaderPreferences
import com.dutaeko.shinigamireader.ui.components.SectionTitle

@Composable
fun ProfileScreen(
    preferences: ReaderPreferences,
    sourceStatusMessage: String,
    availableGenresCount: Int,
) {
    val settings = listOf(
        "Smart download over Wi-Fi" to true,
        "Parental lock for mature sources" to false,
        "Daily reminder to continue reading" to true,
        "Sync history to cloud profile" to false,
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle(
                    title = "Profile & Settings",
                    subtitle = "Reader habits, smart automation, dan preferensi kenyamanan membaca",
                )
            }

            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Source Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sourceStatusMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Genre parser tersedia: $availableGenresCount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(title = "78h", subtitle = "Reading Time", modifier = Modifier.weight(1f))
                    StatCard(title = "146", subtitle = "Chapters This Month", modifier = Modifier.weight(1f))
                }
            }

            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Reader Preset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Mode: ${preferences.readingMode.name}")
                        Text("Brightness: ${preferences.brightnessPercent}%")
                        Text("Spacing: ${preferences.pageSpacingPercent}%")
                        Text("Auto scroll: ${if (preferences.autoScrollEnabled) "On" else "Off"}")
                    }
                }
            }

            items(settings) { setting ->
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(setting.first, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Setting kompleks ini nantinya bisa dihubungkan ke DataStore + WorkManager.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = setting.second, onCheckedChange = { })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
