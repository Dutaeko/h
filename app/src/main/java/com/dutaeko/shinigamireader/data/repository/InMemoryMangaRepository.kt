package com.dutaeko.shinigamireader.data.repository

import com.dutaeko.shinigamireader.core.model.Chapter
import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.LibraryEntry
import com.dutaeko.shinigamireader.core.model.LibraryState
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.MangaStatus
import com.dutaeko.shinigamireader.core.model.ReaderPage
import com.dutaeko.shinigamireader.core.model.ReaderPreferences
import com.dutaeko.shinigamireader.core.model.ReadingMode

class InMemoryMangaRepository : MangaRepository {

    private val catalogue = listOf(
        sampleManga(
            id = "shadow-blade",
            title = "Shadow Blade Requiem",
            author = "A. Morikawa",
            status = MangaStatus.ONGOING,
            rating = 4.9,
            followers = "412K",
            updateLabel = "Updated 14 min ago",
            tags = listOf("Action", "Dark Fantasy", "Seinen"),
            coverUrl = "https://placehold.co/600x900/111111/F5F5F5?text=Shadow+Blade",
            description = "Seorang pemburu iblis bangkit kembali dengan kontrak kutukan. Ia harus menyeimbangkan balas dendam, politik dunia roh, dan rahasia masa lalunya.",
        ),
        sampleManga(
            id = "neon-realm",
            title = "Neon Realm Archivist",
            author = "Luna Ito",
            status = MangaStatus.ONGOING,
            rating = 4.7,
            followers = "285K",
            updateLabel = "Updated yesterday",
            tags = listOf("Sci-Fi", "Mystery", "School"),
            coverUrl = "https://placehold.co/600x900/18142B/F4F1FF?text=Neon+Realm",
            description = "Arsip digital yang berisi ingatan manusia bocor ke dunia nyata. Seorang siswi hacker memburu file terlarang yang bisa mengubah sejarah.",
        ),
        sampleManga(
            id = "jade-kitchen",
            title = "Jade Kitchen Chronicles",
            author = "Mira Shen",
            status = MangaStatus.COMPLETED,
            rating = 4.6,
            followers = "96K",
            updateLabel = "Completed",
            tags = listOf("Slice of Life", "Comedy", "Cooking"),
            coverUrl = "https://placehold.co/600x900/133B2C/EAFBF0?text=Jade+Kitchen",
            description = "Chef muda yang bereinkarnasi di era kerajaan menggunakan resep modern untuk menaklukkan istana, pasar gelap, dan hati para bangsawan.",
        ),
        sampleManga(
            id = "blue-orbit",
            title = "Blue Orbit: Last Colony",
            author = "Ken Aozora",
            status = MangaStatus.HIATUS,
            rating = 4.8,
            followers = "144K",
            updateLabel = "Hiatus • 3 months",
            tags = listOf("Adventure", "Space", "Drama"),
            coverUrl = "https://placehold.co/600x900/0D2340/EAF4FF?text=Blue+Orbit",
            description = "Koloni terakhir di sabuk asteroid kehilangan kontak dengan bumi. Tim penyelamat menemukan bahwa ancaman terbesar justru berasal dari awak mereka sendiri.",
        ),
        sampleManga(
            id = "moonlit-doctor",
            title = "Moonlit Doctor",
            author = "Sora Hayami",
            status = MangaStatus.ONGOING,
            rating = 4.5,
            followers = "72K",
            updateLabel = "Updated 3 days ago",
            tags = listOf("Romance", "Fantasy", "Medical"),
            coverUrl = "https://placehold.co/600x900/2A1A32/FDEFFF?text=Moonlit+Doctor",
            description = "Dokter darurat terseret ke dunia fantasi dan menjadi tabib istana yang harus memecahkan wabah magis sambil menyembunyikan asal-usulnya.",
        ),
    )

    override fun homeSections(): List<HomeSection> = listOf(
        HomeSection(
            title = "Spotlight of the Day",
            subtitle = "Pilihan cerita dengan retention tinggi dan world-building kuat",
            items = catalogue.take(3),
        ),
        HomeSection(
            title = "Latest Updates",
            subtitle = "Update chapter baru dari manga favoritmu",
            items = catalogue.shuffled().take(4),
        ),
        HomeSection(
            title = "Because You Read Action",
            subtitle = "Rekomendasi personal berbasis histori baca",
            items = catalogue.filter { "Action" in it.tags || "Adventure" in it.tags },
        ),
    )

    override fun discoverManga(): List<Manga> = catalogue

    override fun libraryEntries(): List<LibraryEntry> = listOf(
        LibraryEntry(
            manga = catalogue[0],
            state = LibraryState.READING,
            unreadCount = 8,
            downloadedCount = 14,
            progressPercent = 64,
            lastReadChapterLabel = "Chapter 54",
        ),
        LibraryEntry(
            manga = catalogue[1],
            state = LibraryState.READING,
            unreadCount = 3,
            downloadedCount = 6,
            progressPercent = 29,
            lastReadChapterLabel = "Chapter 18",
        ),
        LibraryEntry(
            manga = catalogue[2],
            state = LibraryState.COMPLETED,
            unreadCount = 0,
            downloadedCount = 22,
            progressPercent = 100,
            lastReadChapterLabel = "Finished",
        ),
        LibraryEntry(
            manga = catalogue[4],
            state = LibraryState.PLANNED,
            unreadCount = 12,
            downloadedCount = 0,
            progressPercent = 0,
            lastReadChapterLabel = "Belum dibaca",
        ),
    )

    override fun readerPreferences(): ReaderPreferences = ReaderPreferences(
        readingMode = ReadingMode.VERTICAL,
        brightnessPercent = 82,
        pageSpacingPercent = 10,
        autoScrollEnabled = false,
        showComments = true,
        cropBorders = true,
        keepScreenOn = true,
    )

    private fun sampleManga(
        id: String,
        title: String,
        author: String,
        status: MangaStatus,
        rating: Double,
        followers: String,
        updateLabel: String,
        tags: List<String>,
        coverUrl: String,
        description: String,
    ): Manga {
        val chapters = List(12) { chapterIndex ->
            val chapterNo = 60 - chapterIndex
            Chapter(
                id = "$id-chapter-$chapterNo",
                title = "Chapter $chapterNo • Turning Point",
                number = chapterNo.toDouble(),
                uploadLabel = if (chapterIndex == 0) "20 minutes ago" else "${chapterIndex + 1} days ago",
                downloaded = chapterIndex < 5,
                readProgress = if (chapterIndex == 6) 44 else if (chapterIndex > 6) 100 else 0,
                pages = List(8) { pageIndex ->
                    ReaderPage(
                        index = pageIndex + 1,
                        imageUrl = "https://placehold.co/1200x1800/0F0F10/F4F4F5?text=${title.replace(" ", "+")}+P${pageIndex + 1}",
                    )
                },
            )
        }

        return Manga(
            id = id,
            title = title,
            coverUrl = coverUrl,
            author = author,
            description = description,
            status = status,
            rating = rating,
            followers = followers,
            tags = tags,
            updateLabel = updateLabel,
            chapters = chapters,
        )
    }
}
