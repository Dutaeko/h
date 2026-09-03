package com.dutaeko.shinigamireader.data.source

import com.dutaeko.shinigamireader.core.model.Chapter
import com.dutaeko.shinigamireader.core.model.HomeSection
import com.dutaeko.shinigamireader.core.model.Manga
import com.dutaeko.shinigamireader.core.model.MangaStatus
import com.dutaeko.shinigamireader.core.model.ReaderPage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class ShinigamiParserAdapter(
    private val baseUrl: String = DEFAULT_BASE,
    private val apiBaseUrl: String = API,
    private val fallbackCdn: String = FALLBACK_CDN,
) : MangaSourceParser {

    override val id: String = "shinigami"
    override val displayName: String = "Shinigami Source"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val detailCache = TimedMemoryCache<String, Manga>(CACHE_TTL)
    private val chapterCache = TimedMemoryCache<String, List<Chapter>>(CACHE_TTL)
    private val pageCache = TimedMemoryCache<String, List<ReaderPage>>(CACHE_TTL)
    private val chapterIdCache = TimedMemoryCache<String, String>(CACHE_TTL)
    private val listCache = TimedMemoryCache<String, List<Manga>>(CACHE_TTL)

    override fun homeSections(): List<HomeSection> {
        val latest = search(query = "", filters = mapOf("sort" to "latest"))
        val popularity = search(query = "", filters = mapOf("sort" to "popularity"))
        val rating = search(query = "", filters = mapOf("sort" to "rating"))

        return listOf(
            HomeSection(
                title = "Latest from Shinigami",
                subtitle = "Update terbaru langsung dari parser source Shinigami API",
                items = latest.take(10),
            ),
            HomeSection(
                title = "Trending Now",
                subtitle = "Judul dengan minat baca tertinggi dari source yang sama",
                items = popularity.take(10),
            ),
            HomeSection(
                title = "Top Rated Picks",
                subtitle = "Kurasi judul dengan skor paling menarik untuk dibaca panjang",
                items = rating.take(10),
            ),
        ).filter { it.items.isNotEmpty() }
    }

    override fun search(
        query: String,
        filters: Map<String, String>,
    ): List<Manga> {
        val safeQuery = query.trim()
        val safeSort = filters["sort"]
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it in setOf("popularity", "rating", "latest") }
            ?: "latest"
        val safeSortOrder = filters["sort_order"]
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it in setOf("asc", "desc") }
            ?: "desc"

        val url = buildString {
            append(apiBaseUrl)
            append("/v1/manga/list?page=1&page_size=30")
            if (safeQuery.isNotEmpty()) {
                append("&q=")
                append(encode(safeQuery))
            }
            append("&sort=")
            append(encode(safeSort))
            append("&sort_order=")
            append(encode(safeSortOrder))
            appendOptionalQuery(this, "status", filters["status"])
            appendOptionalQuery(this, "format", filters["format"])
            appendOptionalQuery(this, "type", filters["type"])
            appendOptionalQuery(this, "genre_include", filters["genre_include"] ?: filters["genre"])
            appendOptionalQuery(this, "genre_exclude", filters["genre_exclude"])
            if (!filters["genre_include"].isNullOrBlank() || !filters["genre"].isNullOrBlank()) {
                append("&genre_include_mode=and")
            }
            if (!filters["genre_exclude"].isNullOrBlank()) {
                append("&genre_exclude_mode=and")
            }
        }

        listCache.get(url)?.let { return it }

        return runCatching {
            val root = getJson(url)
            val data = getArray(root, "data")
            val seen = LinkedHashSet<String>()
            val result = buildList {
                for (element in data) {
                    if (element?.isJsonObject == true) {
                        val parsed = parsePost(element.asJsonObject)
                        val dedupeKey = parsed.id.ifBlank { parsed.title }
                        if (dedupeKey.isNotBlank() && seen.add(dedupeKey)) {
                            add(parsed)
                        }
                    }
                }
            }
            listCache.put(url, result)
            result
        }.getOrElse { emptyList() }
    }

    override fun mangaDetail(mangaId: String): Manga? {
        detailCache.get(mangaId)?.let { return it }

        return runCatching<Manga?> {
            val root = getJson("$apiBaseUrl/v1/manga/detail/$mangaId")
            val data = getObject(root, "data") ?: return@runCatching null
            val detail = parseDetail(mangaId, data)
            val chapters = chapterList(mangaId)
            val merged = detail.copy(chapters = chapters)
            detailCache.put(mangaId, merged)
            merged
        }.getOrNull()
    }

    override fun chapterList(mangaId: String): List<Chapter> {
        chapterCache.get(mangaId)?.let { return it }

        return runCatching {
            val root = getJson("$apiBaseUrl/v1/chapter/$mangaId/list?page_size=3000")
            val data = getArray(root, "data")
            val seen = LinkedHashSet<String>()
            val result = buildList {
                for (element in data) {
                    if (element?.isJsonObject != true) continue
                    val item = element.asJsonObject
                    val number = getDouble(item, "chapter_number", -1.0)
                    if (number < 0) continue
                    val numberLabel = formatChapterNumber(number)
                    if (!seen.add(numberLabel)) continue

                    val remoteChapterId = firstNonEmpty(
                        getString(item, "chapter_id"),
                        getString(item, "chapterId"),
                        getString(item, "id"),
                        getString(item, "chapter_uuid"),
                        getString(item, "uuid"),
                    )
                    if (remoteChapterId.isNotBlank()) {
                        chapterIdCache.put(cacheChapterNumberKey(mangaId, numberLabel), remoteChapterId)
                        chapterIdCache.put(cacheChapterNumberKey(mangaId, number.toString()), remoteChapterId)
                    }

                    add(
                        Chapter(
                            id = composeChapterKey(
                                mangaId = mangaId,
                                chapterNumber = numberLabel,
                                remoteChapterId = remoteChapterId,
                            ),
                            title = getString(item, "chapter_title")
                                .ifBlank { "Chapter $numberLabel" },
                            number = number,
                            uploadLabel = prettyDate(getString(item, "release_date")).ifBlank { "Unknown update" },
                            sourceMangaId = mangaId,
                            remoteChapterId = remoteChapterId,
                        ),
                    )
                }
            }.sortedByDescending { it.number }

            chapterCache.put(mangaId, result)
            result
        }.getOrElse { emptyList() }
    }

    override fun pageList(chapterId: String): List<ReaderPage> {
        pageCache.get(chapterId)?.let { return it }

        val parts = chapterId.split("|")
        val mangaId = parts.getOrNull(1).orEmpty()
        val chapterNumber = parts.getOrNull(2).orEmpty()
        var remoteChapterId = parts.getOrNull(3).orEmpty()

        if (mangaId.isBlank()) return emptyList()

        if (remoteChapterId.isBlank()) {
            remoteChapterId = chapterIdCache.get(cacheChapterNumberKey(mangaId, chapterNumber)).orEmpty()
        }
        if (remoteChapterId.isBlank()) {
            val chapters = chapterList(mangaId)
            remoteChapterId = chapters.firstOrNull {
                formatChapterNumber(it.number) == chapterNumber
            }?.remoteChapterId.orEmpty()
        }
        if (remoteChapterId.isBlank()) return emptyList()

        return runCatching {
            val root = getJson("$apiBaseUrl/v1/chapter/detail/$remoteChapterId")
            val data = getObject(root, "data")
            val chapter = getObject(data, "chapter") ?: data ?: JsonObject()
            val cdnBase = firstNonEmpty(
                getString(data, "base_url"),
                getString(data, "baseUrl"),
                getString(root, "base_url"),
                fallbackCdn,
            )
            val path = firstNonEmpty(
                getString(chapter, "path"),
                getString(chapter, "image_path"),
                getString(chapter, "imagePath"),
                getString(chapter, "directory"),
            )
            var pages = getArray(chapter, "data")
            if (pages.size() == 0) pages = getArray(chapter, "pages")
            if (pages.size() == 0) pages = getArray(chapter, "images")

            val seen = LinkedHashSet<String>()
            val result = buildList {
                pages.forEachIndexed { index, imageElement ->
                    if (imageElement == null || imageElement.isJsonNull) return@forEachIndexed
                    val fileName = if (imageElement.isJsonObject) {
                        firstNonEmpty(
                            getString(imageElement.asJsonObject, "url"),
                            getString(imageElement.asJsonObject, "src"),
                            getString(imageElement.asJsonObject, "image"),
                            getString(imageElement.asJsonObject, "filename"),
                            getString(imageElement.asJsonObject, "name"),
                        )
                    } else {
                        imageElement.asString.orEmpty()
                    }
                    val imageUrl = buildImageUrl(
                        cdnBase = cdnBase,
                        path = path,
                        name = fileName,
                    )
                    if (imageUrl.startsWith("http") && seen.add(imageUrl)) {
                        add(ReaderPage(index = index + 1, imageUrl = imageUrl))
                    }
                }
            }
            pageCache.put(chapterId, result)
            result
        }.getOrElse { emptyList() }
    }

    override fun availableGenres(): List<String> = listOf(
        "Action",
        "Adaptation",
        "Adult",
        "Adventure",
        "Comedy",
        "Cooking",
        "Crime",
        "Demon",
        "Demons",
        "Dra",
        "Drama",
        "Ecchi",
        "Fantasy",
        "Fight",
        "Game",
        "Gender Bender",
        "Harem",
        "Historical",
        "Horror",
        "Isekai",
        "Josei",
        "Love",
        "Magic",
        "Martial Arts",
        "Mature",
        "Mecha",
        "Medical",
        "Murim",
        "Mystery",
        "Philosophical",
        "Psychological",
        "Regression",
        "Revenge",
        "Romance",
        "School Life",
        "Sci-fi",
        "Seinen",
        "Shoujo",
        "Shounen",
        "Slice of Life",
        "Smut",
        "Sports",
        "Supernatural",
        "Supranatural",
        "Thriller",
        "Tragedy",
        "Violence",
        "Wuxia",
    )

    private fun getJson(url: String): JsonObject {
        val request = Request.Builder()
            .url(url)
            .header("Referer", "$baseUrl/")
            .header("Origin", baseUrl)
            .header("Accept", "application/json")
            .header("DNT", "1")
            .header("Sec-GPC", "1")
            .header("Accept-Language", "id,en-US;q=0.9")
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("HTTP ${response.code}")
            return JsonParser.parseString(body).asJsonObject
        }
    }

    private fun parsePost(item: JsonObject): Manga {
        val mangaId = firstNonEmpty(
            getString(item, "manga_id"),
            getString(item, "mangaId"),
            getString(item, "id"),
            getString(item, "slug"),
        )
        val title = getString(item, "title")
        val cover = firstNonEmpty(
            getString(item, "cover_image_url"),
            getString(item, "coverImageUrl"),
            getString(item, "thumbnail"),
            getString(item, "cover"),
        )
        val typeLabel = firstNonEmpty(
            getString(item, "format"),
            getString(item, "type"),
            getString(item, "comic_type"),
            taxonomyText(item, "Format"),
            inferTypeFromText("$title ${taxonomyText(item, "Genre")}"),
        )
        val genres = taxonomyText(item, "Genre")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val rating = firstPositiveDouble(
            getDouble(item, "rating", 0.0),
            getDouble(item, "score", 0.0),
            getDouble(item, "avg_rating", 0.0),
        )
        val followers = compactNumber(
            firstPositiveDouble(
                getDouble(item, "follower_count", 0.0),
                getDouble(item, "followers", 0.0),
                getDouble(item, "view_count", 0.0),
                getDouble(item, "views", 0.0),
            ),
        )
        val latestChapter = firstNonEmpty(
            getString(item, "latest_chapter"),
            getString(item, "latestChapter"),
        )
        val updatedAt = firstNonEmpty(
            prettyDate(getString(item, "updated_at")),
            prettyDate(getString(item, "update_at")),
            prettyDate(getString(item, "release_date")),
        )
        val updateLabel = listOf(latestChapter, updatedAt)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
            .ifBlank { typeLabel.replaceFirstChar { it.uppercase() } }

        return Manga(
            id = mangaId,
            title = title,
            coverUrl = cover,
            author = "",
            description = "",
            status = MangaStatus.UNKNOWN,
            rating = rating,
            followers = followers,
            tags = genres,
            updateLabel = updateLabel,
            chapters = emptyList(),
            typeLabel = typeLabel,
            sourceId = id,
        )
    }

    private fun parseDetail(
        mangaId: String,
        data: JsonObject,
    ): Manga {
        val taxonomy = getObject(data, "taxonomy")
        val author = joinTaxonomy(taxonomy, "Author")
        val artist = joinTaxonomy(taxonomy, "Artist")
        val genres = joinTaxonomy(taxonomy, "Genre")
        val format = joinTaxonomy(taxonomy, "Format")
        val rating = firstPositiveDouble(
            getDouble(data, "rating", 0.0),
            getDouble(data, "score", 0.0),
            getDouble(data, "avg_rating", 0.0),
        )
        val followers = compactNumber(
            firstPositiveDouble(
                getDouble(data, "follower_count", 0.0),
                getDouble(data, "followers", 0.0),
                getDouble(data, "view_count", 0.0),
                getDouble(data, "views", 0.0),
            ),
        )
        val tagList = genres.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return Manga(
            id = mangaId,
            title = getString(data, "title"),
            coverUrl = firstNonEmpty(
                getString(data, "cover_image_url"),
                getString(data, "coverImageUrl"),
                getString(data, "thumbnail"),
            ),
            author = firstNonEmpty(author, artist),
            description = getString(data, "description"),
            status = statusLabel(getInt(data, "status", 0)),
            rating = rating,
            followers = followers,
            tags = tagList,
            updateLabel = firstNonEmpty(format, inferTypeFromText(genres)).replaceFirstChar { it.uppercase() },
            chapters = emptyList(),
            typeLabel = firstNonEmpty(format, inferTypeFromText(genres)),
            sourceId = id,
        )
    }

    private fun taxonomyText(
        item: JsonObject,
        key: String,
    ): String {
        val taxonomy = getObject(item, "taxonomy")
        val joined = joinTaxonomy(taxonomy, key)
        if (joined.isNotBlank()) return joined

        val array = getArray(item, key.lowercase(Locale.ROOT))
        return buildList {
            for (element in array) {
                if (element == null || element.isJsonNull) continue
                if (element.isJsonObject) {
                    val name = getString(element.asJsonObject, "name")
                    if (name.isNotBlank()) add(name)
                } else {
                    val name = element.asString.orEmpty().trim()
                    if (name.isNotBlank()) add(name)
                }
            }
        }.joinToString(", ")
    }

    private fun joinTaxonomy(
        taxonomy: JsonObject?,
        key: String,
    ): String {
        return getArray(taxonomy, key)
            .mapNotNull { element ->
                element
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.let { getString(it, "name").takeIf(String::isNotBlank) }
            }
            .joinToString(", ")
    }

    private fun inferTypeFromText(text: String): String {
        val normalized = text.lowercase(Locale.ROOT)
        return when {
            normalized.contains("manhwa") -> "manhwa"
            normalized.contains("manhua") -> "manhua"
            normalized.contains("webtoon") -> "webtoon"
            else -> "manga"
        }
    }

    private fun statusLabel(code: Int): MangaStatus = when (code) {
        1 -> MangaStatus.ONGOING
        2 -> MangaStatus.COMPLETED
        else -> MangaStatus.UNKNOWN
    }

    private fun buildImageUrl(
        cdnBase: String,
        path: String,
        name: String,
    ): String {
        val safeBase = normalizeBaseUrl(cdnBase.ifBlank { fallbackCdn })
        var safeName = name.trim()
        if (safeName.startsWith("http://") || safeName.startsWith("https://")) {
            return replaceLegacyCdn(safeName, safeBase)
        }

        var safePath = path.trim()
        if (safePath.startsWith("http://") || safePath.startsWith("https://")) {
            if (!safePath.endsWith('/')) safePath += "/"
            while (safeName.startsWith('/')) safeName = safeName.drop(1)
            return replaceLegacyCdn(safePath, safeBase) + safeName
        }

        while (safePath.startsWith('/')) safePath = safePath.drop(1)
        while (safePath.endsWith('/')) safePath = safePath.dropLast(1)
        while (safeName.startsWith('/')) safeName = safeName.drop(1)

        return if (safePath.isBlank()) {
            "$safeBase/$safeName"
        } else {
            "$safeBase/$safePath/$safeName"
        }
    }

    private fun normalizeBaseUrl(value: String): String {
        var base = value.trim().ifBlank { fallbackCdn }
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "https://$base"
        }
        while (base.endsWith('/')) base = base.dropLast(1)
        return base
    }

    private fun replaceLegacyCdn(
        url: String,
        dynamicBase: String,
    ): String {
        val legacyBases = listOf(
            "https://storage.shngm.id",
            "http://storage.shngm.id",
            "https://storage.shngm.io",
            "http://storage.shngm.io",
            "https://storage.shinigami.id",
            "http://storage.shinigami.id",
        )
        legacyBases.forEach { legacy ->
            if (url.startsWith(legacy)) {
                return dynamicBase + url.removePrefix(legacy)
            }
        }
        return url
    }

    private fun formatChapterNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString().trimEnd('0').trimEnd('.')
        }
    }

    private fun composeChapterKey(
        mangaId: String,
        chapterNumber: String,
        remoteChapterId: String,
    ): String = listOf(id, mangaId, chapterNumber, remoteChapterId).joinToString("|")

    private fun cacheChapterNumberKey(
        mangaId: String,
        chapterNumber: String,
    ): String = "$mangaId:$chapterNumber"

    private fun prettyDate(raw: String): String {
        val value = raw.trim()
        return if (value.isBlank()) "" else value.replace('T', ' ')
    }

    private fun compactNumber(value: Double): String {
        if (value <= 0.0) return ""
        return when {
            value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000).replace(".0", "")
            value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000).replace(".0", "")
            else -> value.toInt().toString()
        }
    }

    private fun appendOptionalQuery(
        builder: StringBuilder,
        key: String,
        value: String?,
    ) {
        if (!value.isNullOrBlank()) {
            builder.append("&")
            builder.append(key)
            builder.append("=")
            builder.append(encode(value))
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun getObject(
        jsonObject: JsonObject?,
        key: String,
    ): JsonObject? = runCatching {
        if (jsonObject != null && jsonObject.has(key) && jsonObject.get(key).isJsonObject) {
            jsonObject.getAsJsonObject(key)
        } else {
            null
        }
    }.getOrNull()

    private fun getArray(
        jsonObject: JsonObject?,
        key: String,
    ): JsonArray = runCatching {
        if (jsonObject != null && jsonObject.has(key) && jsonObject.get(key).isJsonArray) {
            jsonObject.getAsJsonArray(key)
        } else {
            JsonArray()
        }
    }.getOrDefault(JsonArray())

    private fun getString(
        jsonObject: JsonObject?,
        key: String,
    ): String = runCatching {
        if (jsonObject != null && jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
            jsonObject.get(key).asString.orEmpty()
        } else {
            ""
        }
    }.getOrDefault("")

    private fun getInt(
        jsonObject: JsonObject?,
        key: String,
        defaultValue: Int,
    ): Int = runCatching {
        if (jsonObject != null && jsonObject.has(key)) jsonObject.get(key).asInt else defaultValue
    }.getOrDefault(defaultValue)

    private fun getDouble(
        jsonObject: JsonObject?,
        key: String,
        defaultValue: Double,
    ): Double = runCatching {
        if (jsonObject != null && jsonObject.has(key)) jsonObject.get(key).asDouble else defaultValue
    }.getOrDefault(defaultValue)

    private fun firstNonEmpty(vararg values: String): String =
        values.firstOrNull { it.isNotBlank() }?.trim().orEmpty()

    private fun firstPositiveDouble(vararg values: Double): Double =
        values.firstOrNull { it > 0.0 } ?: 0.0

    companion object {
        private const val DEFAULT_BASE = "https://11.shinigami.asia"
        private const val API = "https://api.shngm.io"
        private const val FALLBACK_CDN = "https://storage.shngm.id"
        private const val CACHE_TTL = 12L * 60L * 1000L
    }
}
