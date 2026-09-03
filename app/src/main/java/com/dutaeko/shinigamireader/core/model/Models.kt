package com.dutaeko.shinigamireader.core.model

data class Manga(
    val id: String,
    val title: String,
    val coverUrl: String,
    val author: String = "",
    val description: String = "",
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val rating: Double = 0.0,
    val followers: String = "",
    val tags: List<String> = emptyList(),
    val updateLabel: String = "",
    val chapters: List<Chapter> = emptyList(),
    val typeLabel: String = "",
    val sourceId: String = "shinigami",
)

data class Chapter(
    val id: String,
    val title: String,
    val number: Double,
    val uploadLabel: String = "",
    val downloaded: Boolean = false,
    val readProgress: Int = 0,
    val pages: List<ReaderPage> = emptyList(),
    val sourceMangaId: String = "",
    val remoteChapterId: String = "",
)

data class ReaderPage(
    val index: Int,
    val imageUrl: String,
)

data class HomeSection(
    val title: String,
    val subtitle: String,
    val items: List<Manga>,
)

data class LibraryEntry(
    val manga: Manga,
    val state: LibraryState,
    val unreadCount: Int,
    val downloadedCount: Int,
    val progressPercent: Int,
    val lastReadChapterLabel: String,
)

data class ReaderPreferences(
    val readingMode: ReadingMode,
    val brightnessPercent: Int,
    val pageSpacingPercent: Int,
    val autoScrollEnabled: Boolean,
    val showComments: Boolean,
    val cropBorders: Boolean,
    val keepScreenOn: Boolean,
)

enum class MangaStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    UNKNOWN,
}

enum class LibraryState {
    READING,
    PLANNED,
    COMPLETED,
    ON_HOLD,
}

enum class ReadingMode {
    VERTICAL,
    WEBTOON,
    RTL_PAGED,
    LTR_PAGED,
    DOUBLE_PAGE,
}
