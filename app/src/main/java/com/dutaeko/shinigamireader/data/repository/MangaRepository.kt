package com.dutaeko.shinigamireader.data.repository

import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.ReaderPreferences

interface MangaRepository {
    fun homeSections(): List<HomeSection>
    fun discoverManga(): List<Manga>
    fun libraryEntries(): List<LibraryEntry>
    fun readerPreferences(): ReaderPreferences
}
