package com.dutaeko.shinigamireader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dutaeko.shinigamireader.core.model.Chapter
import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.ReaderPreferences
import com.dutaeko.shinigamireader.data.repository.ShinigamiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ShinigamiReaderUiState(
    val homeSections: List<HomeSection> = emptyList(),
    val discoverItems: List<Manga> = emptyList(),
    val libraryEntries: List<LibraryEntry> = emptyList(),
    val readerPreferences: ReaderPreferences,
    val selectedManga: Manga? = null,
    val selectedChapter: Chapter? = null,
    val isRefreshingHome: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isLoadingPages: Boolean = false,
    val usingFallbackData: Boolean = true,
    val sourceStatusMessage: String = "Mode demo aktif. Data live akan dicoba dari parser Shinigami.",
    val availableGenres: List<String> = emptyList(),
)

class ShinigamiReaderViewModel : ViewModel() {

    private val repository = ShinigamiRepository()
    private var searchJob: Job? = null

    var uiState by mutableStateOf(
        ShinigamiReaderUiState(
            homeSections = repository.fallbackHomeSections(),
            discoverItems = repository.fallbackDiscoverManga(),
            libraryEntries = repository.libraryEntries(),
            readerPreferences = repository.readerPreferences(),
            selectedManga = repository.fallbackDiscoverManga().firstOrNull(),
            availableGenres = repository.availableGenres(),
        ),
    )
        private set

    init {
        refreshHome()
        search(query = "", preset = "Trending", debounceMillis = 0L)
    }

    fun refreshHome() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isRefreshingHome = true,
                sourceStatusMessage = "Mengambil katalog terbaru dari parser Shinigami...",
            )
            runCatching {
                repository.loadHomeSections()
            }.onSuccess { sections ->
                if (sections.isNotEmpty()) {
                    uiState = uiState.copy(
                        homeSections = sections,
                        isRefreshingHome = false,
                        usingFallbackData = false,
                        sourceStatusMessage = "Source aktif: API Shinigami live + cache 12 menit.",
                    )
                } else {
                    uiState = uiState.copy(
                        isRefreshingHome = false,
                        usingFallbackData = true,
                        sourceStatusMessage = "API Shinigami belum mengembalikan data. UI memakai fallback demo.",
                    )
                }
            }.onFailure {
                uiState = uiState.copy(
                    isRefreshingHome = false,
                    usingFallbackData = true,
                    sourceStatusMessage = "Gagal menghubungi source Shinigami. Fallback demo tetap aktif.",
                )
            }
        }
    }

    fun search(
        query: String,
        preset: String,
        debounceMillis: Long = 350L,
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            uiState = uiState.copy(isSearching = true)
            if (debounceMillis > 0) delay(debounceMillis)
            runCatching {
                repository.search(query = query, preset = preset)
            }.onSuccess { items ->
                uiState = uiState.copy(
                    discoverItems = if (items.isNotEmpty()) items else uiState.discoverItems,
                    isSearching = false,
                    usingFallbackData = if (items.isNotEmpty()) false else uiState.usingFallbackData,
                    sourceStatusMessage = if (items.isNotEmpty()) {
                        "Discover memakai hasil parser Shinigami untuk preset $preset."
                    } else {
                        "Hasil live kosong, menampilkan data yang terakhir tersedia."
                    },
                )
            }.onFailure {
                uiState = uiState.copy(
                    isSearching = false,
                    sourceStatusMessage = "Pencarian live gagal, menampilkan katalog lokal/fallback.",
                )
            }
        }
    }

    fun openManga(manga: Manga) {
        uiState = uiState.copy(
            selectedManga = manga,
            selectedChapter = null,
            isLoadingDetail = true,
            sourceStatusMessage = "Membuka detail ${manga.title} dari source Shinigami...",
        )

        viewModelScope.launch {
            runCatching {
                repository.loadMangaDetail(manga.id)
            }.onSuccess { detail ->
                val merged = detail?.mergeWith(manga) ?: manga
                uiState = uiState.copy(
                    selectedManga = merged,
                    isLoadingDetail = false,
                    sourceStatusMessage = if (detail != null) {
                        "Detail, genre, author, dan chapter diambil dari parser Shinigami."
                    } else {
                        "Detail live tidak tersedia, memakai data yang sudah ada."
                    },
                )
            }.onFailure {
                uiState = uiState.copy(
                    isLoadingDetail = false,
                    sourceStatusMessage = "Gagal memuat detail live, tapi layar detail tetap bisa dibuka.",
                )
            }
        }
    }

    fun resumeLibraryEntry(entry: LibraryEntry) {
        val chapter = entry.manga.chapters.firstOrNull { it.readProgress in 1..99 }
            ?: entry.manga.chapters.firstOrNull()
        uiState = uiState.copy(
            selectedManga = entry.manga,
            selectedChapter = chapter,
            isLoadingPages = false,
            sourceStatusMessage = "Lanjut membaca dari library lokal.",
        )
    }

    fun openChapter(chapter: Chapter) {
        val currentManga = uiState.selectedManga ?: return
        uiState = uiState.copy(
            selectedChapter = chapter.copy(pages = emptyList()),
            isLoadingPages = true,
            sourceStatusMessage = "Mengambil halaman chapter ${chapter.number} dari source Shinigami...",
        )

        viewModelScope.launch {
            runCatching {
                repository.loadPages(chapter.id)
            }.onSuccess { pages ->
                val resolvedPages = if (pages.isNotEmpty()) pages else chapter.pages
                val enrichedChapter = chapter.copy(pages = resolvedPages)
                val updatedManga = currentManga.copy(
                    chapters = currentManga.chapters.map {
                        if (it.id == chapter.id) enrichedChapter else it
                    },
                )
                uiState = uiState.copy(
                    selectedManga = updatedManga,
                    selectedChapter = enrichedChapter,
                    isLoadingPages = false,
                    sourceStatusMessage = if (resolvedPages.isNotEmpty()) {
                        "Reader aktif dengan halaman live dari parser Shinigami."
                    } else {
                        "Halaman live kosong, reader menampilkan data yang tersedia."
                    },
                )
            }.onFailure {
                uiState = uiState.copy(
                    selectedChapter = chapter,
                    isLoadingPages = false,
                    sourceStatusMessage = "Gagal memuat halaman live chapter. Coba lagi nanti.",
                )
            }
        }
    }

    fun openNextChapter() {
        val manga = uiState.selectedManga ?: return
        val chapter = uiState.selectedChapter ?: return
        val currentIndex = manga.chapters.indexOfFirst { it.id == chapter.id }
        if (currentIndex in 0 until manga.chapters.lastIndex) {
            openChapter(manga.chapters[currentIndex + 1])
        }
    }

    private fun Manga.mergeWith(fallback: Manga): Manga {
        return copy(
            title = title.ifBlank { fallback.title },
            coverUrl = coverUrl.ifBlank { fallback.coverUrl },
            author = author.ifBlank { fallback.author },
            description = description.ifBlank { fallback.description },
            rating = if (rating > 0.0) rating else fallback.rating,
            followers = followers.ifBlank { fallback.followers },
            tags = if (tags.isNotEmpty()) tags else fallback.tags,
            updateLabel = updateLabel.ifBlank { fallback.updateLabel },
            chapters = if (chapters.isNotEmpty()) chapters else fallback.chapters,
            typeLabel = typeLabel.ifBlank { fallback.typeLabel },
        )
    }
}
