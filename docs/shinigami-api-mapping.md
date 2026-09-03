# Shinigami API Mapping

Dokumen ini merangkum mapping parser Java `Shinigami` yang Anda paste ke implementasi Kotlin di aplikasi Android ini.

## Endpoint utama

### 1. List manga
- `GET https://api.shngm.io/v1/manga/list?page=1&page_size=30`
- Query opsional:
  - `q`
  - `sort=latest|popularity|rating`
  - `sort_order=asc|desc`
  - `status`
  - `format`
  - `type`
  - `genre_include`
  - `genre_exclude`

### 2. Detail manga
- `GET https://api.shngm.io/v1/manga/detail/{slugOrMangaId}`

### 3. List chapter
- `GET https://api.shngm.io/v1/chapter/{slugOrMangaId}/list?page_size=3000`

### 4. Detail chapter / page list
- `GET https://api.shngm.io/v1/chapter/detail/{chapterId}`

## Header request yang dipakai
- `Referer: https://11.shinigami.asia/`
- `Origin: https://11.shinigami.asia`
- `Accept: application/json`
- `Accept-Language: id,en-US;q=0.9`
- `User-Agent: Mozilla/5.0`
- `DNT: 1`
- `Sec-GPC: 1`

## Mapping field list manga
### Input JSON yang dicari
- `manga_id`
- `mangaId`
- `id`
- `slug`
- `title`
- `cover_image_url`
- `coverImageUrl`
- `thumbnail`
- `cover`
- `format`
- `type`
- `comic_type`
- taxonomy `Genre`
- taxonomy `Format`

### Output model app
- `Manga.id`
- `Manga.title`
- `Manga.coverUrl`
- `Manga.tags`
- `Manga.typeLabel`
- `Manga.updateLabel`

## Mapping detail manga
### Input JSON yang dicari
- `title`
- `description`
- `status`
- `cover_image_url`
- `coverImageUrl`
- `thumbnail`
- taxonomy `Author`
- taxonomy `Artist`
- taxonomy `Genre`
- taxonomy `Format`

### Output model app
- `Manga.author`
- `Manga.description`
- `Manga.status`
- `Manga.tags`
- `Manga.typeLabel`

## Mapping chapter
### Input JSON yang dicari
- `chapter_number`
- `chapter_title`
- `release_date`
- `chapter_id`
- `chapterId`
- `id`
- `chapter_uuid`
- `uuid`

### Output model app
- `Chapter.number`
- `Chapter.title`
- `Chapter.uploadLabel`
- `Chapter.remoteChapterId`
- `Chapter.id` dalam format komposit: `source|mangaId|chapterNumber|remoteChapterId`

## Mapping page list
### Input JSON yang dicari
- `data.chapter.path`
- `data.chapter.image_path`
- `data.chapter.imagePath`
- `data.chapter.directory`
- `data.base_url`
- `root.base_url`
- `data.chapter.data`
- `data.chapter.pages`
- `data.chapter.images`

### Output model app
- `ReaderPage.imageUrl`

## Cache yang diterapkan di implementasi Kotlin
- detail cache
- chapter cache
- page cache
- chapter id cache
- list cache
- TTL: 12 menit

## Catatan teknis
Implementasi Kotlin saat ini berusaha mengikuti parser Java semaksimal mungkin, namun dengan penyesuaian ke arsitektur Compose + ViewModel + repository agar aplikasi Android ini siap dikembangkan lebih lanjut.
