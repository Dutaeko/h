package com.dutaeko.shinigamireader

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.ui.ShinigamiReaderViewModel
import com.dutaeko.shinigamireader.ui.navigation.AppDestination
import com.dutaeko.shinigamireader.ui.screens.DiscoverScreen
import com.dutaeko.shinigamireader.ui.screens.HomeScreen
import com.dutaeko.shinigamireader.ui.screens.LibraryScreen
import com.dutaeko.shinigamireader.ui.screens.MangaDetailScreen
import com.dutaeko.shinigamireader.ui.screens.ProfileScreen
import com.dutaeko.shinigamireader.ui.screens.ReaderScreen

@Composable
fun ShinigamiReaderApp(
    readerViewModel: ShinigamiReaderViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val uiState = readerViewModel.uiState

    val mainDestinations = listOf(
        AppDestination.Home,
        AppDestination.Discover,
        AppDestination.Library,
        AppDestination.Profile,
    )

    val currentEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in mainDestinations.map { it.route }) {
                NavigationBar {
                    mainDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigate(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    sections = uiState.homeSections,
                    continueReading = uiState.libraryEntries.firstOrNull { it.progressPercent in 1..99 },
                    onOpenManga = { manga ->
                        readerViewModel.openManga(manga)
                        navController.navigate(AppDestination.Detail.route)
                    },
                    onResumeReading = { entry: LibraryEntry ->
                        readerViewModel.resumeLibraryEntry(entry)
                        navController.navigate(AppDestination.Reader.route)
                    },
                    onDiscoverTap = { navController.navigate(AppDestination.Discover.route) },
                    sourceStatusMessage = uiState.sourceStatusMessage,
                    isRefreshing = uiState.isRefreshingHome,
                )
            }

            composable(AppDestination.Discover.route) {
                DiscoverScreen(
                    items = uiState.discoverItems,
                    onOpenManga = { manga ->
                        readerViewModel.openManga(manga)
                        navController.navigate(AppDestination.Detail.route)
                    },
                    onSearch = { query, preset -> readerViewModel.search(query, preset) },
                    isLoading = uiState.isSearching,
                    statusMessage = uiState.sourceStatusMessage,
                )
            }

            composable(AppDestination.Library.route) {
                LibraryScreen(
                    items = uiState.libraryEntries,
                    onOpenManga = { entry ->
                        readerViewModel.openManga(entry.manga)
                        navController.navigate(AppDestination.Detail.route)
                    },
                )
            }

            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    preferences = uiState.readerPreferences,
                    sourceStatusMessage = uiState.sourceStatusMessage,
                    availableGenresCount = uiState.availableGenres.size,
                )
            }

            composable(AppDestination.Detail.route) {
                uiState.selectedManga?.let { manga ->
                    MangaDetailScreen(
                        manga = manga,
                        onBack = { navController.popBackStack() },
                        onReadChapter = { chapter ->
                            readerViewModel.openChapter(chapter)
                            navController.navigate(AppDestination.Reader.route)
                        },
                        isLoading = uiState.isLoadingDetail,
                        sourceStatusMessage = uiState.sourceStatusMessage,
                    )
                }
            }

            composable(AppDestination.Reader.route) {
                val manga = uiState.selectedManga
                val chapter = uiState.selectedChapter
                if (manga != null && chapter != null) {
                    ReaderScreen(
                        manga = manga,
                        chapter = chapter,
                        preferences = uiState.readerPreferences,
                        onBack = { navController.popBackStack() },
                        onNextChapter = { readerViewModel.openNextChapter() },
                        isLoading = uiState.isLoadingPages,
                        sourceStatusMessage = uiState.sourceStatusMessage,
                    )
                }
            }
        }
    }
}
