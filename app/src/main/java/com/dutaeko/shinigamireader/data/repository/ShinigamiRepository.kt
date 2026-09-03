package com.dutaeko.shinigamireader.data.repository

import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.ReaderPage
import com.dutaeko.shinigamireader.core.model.ReaderPreferences
import com.dutaeko.shinigamireader.data.source.MangaSourceParser
import com.dutaeko.shinigamireader.data.source.ShinigamiParserAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShinigamiRepository(
    private val parser: MangaSourceParser = ShinigamiParserAdapter(),
    private val fallbackRepository: MangaRepository = InMemoryMangaRepository(),
) {

    fun fallbackHomeSections(): List<HomeSection> = fallbackRepository.homeSections()

    fun fallbackDiscoverManga(): List<Manga> = fallbackRepository.discoverManga()

    fun libraryEntries(): List<LibraryEntry> = fallbackRepository.libraryEntries()

    fun readerPreferences(): ReaderPreferences = fallbackRepository.readerPreferences()

    fun availableGenres(): List<String> = parser.availableGenres()

    suspend fun loadHomeSections(): List<HomeSection> = withContext(Dispatchers.IO) {
        parser.homeSections()
    }

    suspend fun search(
        query: String,
        preset: String,
    ): List<Manga> = withContext(Dispatchers.IO) {
        parser.search(
            query = query,
            filters = filtersForPreset(preset),
        )
    }

    suspend fun loadMangaDetail(mangaId: String): Manga? = withContext(Dispatchers.IO) {
        parser.mangaDetail(mangaId)
    }

    suspend fun loadPages(chapterId: String): List<ReaderPage> = withContext(Dispatchers.IO) {
        parser.pageList(chapterId)
    }

    private fun filtersForPreset(preset: String): Map<String, String> {
        return when (preset) {
            "Trending" -> mapOf("sort" to "popularity", "sort_order" to "desc")
            "Latest" -> mapOf("sort" to "latest", "sort_order" to "desc")
            "Top Rated" -> mapOf("sort" to "rating", "sort_order" to "desc")
            "Action" -> mapOf("sort" to "popularity", "genre" to "action")
            "Romance" -> mapOf("sort" to "popularity", "genre" to "romance")
            "Completed" -> mapOf("sort" to "latest", "status" to "completed")
            else -> mapOf("sort" to "latest", "sort_order" to "desc")
        }
    }
}
