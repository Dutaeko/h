package com.dutaeko.shinigamireader.data.source

import com.dutaeko.shinigamireader.core.model.Chapter
import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.ReaderPage

interface MangaSourceParser {
    val id: String
    val displayName: String

    fun homeSections(): List<HomeSection>
    fun search(query: String, filters: Map<String, String> = emptyMap()): List<Manga>
    fun mangaDetail(mangaId: String): Manga?
    fun chapterList(mangaId: String): List<Chapter>
    fun pageList(chapterId: String): List<ReaderPage>
    fun availableGenres(): List<String> = emptyList()
}
