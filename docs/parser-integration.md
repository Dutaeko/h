# Parser Integration Plan

## Status terbaru
Parser `Shinigami` yang Anda paste sudah saya konversi ke arsitektur aplikasi ini melalui:

- `data/source/MangaSourceParser.kt`
- `data/source/ShinigamiParserAdapter.kt`
- `data/source/TimedMemoryCache.kt`
- `data/repository/ShinigamiRepository.kt`
- `ui/ShinigamiReaderViewModel.kt`

## Fitur parser yang sudah dimapping

### 1. Katalog / search
Sudah dimapping ke endpoint:
- `/v1/manga/list`

Dukungan filter awal:
- sort latest
- sort popularity
- sort rating
- genre action
- genre romance
- status completed
- query keyword

### 2. Detail manga
Sudah dimapping ke endpoint:
- `/v1/manga/detail/{mangaId}`

Field yang diambil:
- title
- cover
- description
- status
- author/artist
- genres
- format/type

### 3. Chapter list
Sudah dimapping ke endpoint:
- `/v1/chapter/{mangaId}/list?page_size=3000`

Field yang diambil:
- chapter number
- chapter title
- release date
- chapter id

### 4. Reader page list
Sudah dimapping ke endpoint:
- `/v1/chapter/detail/{chapterId}`

Field yang diambil:
- base cdn
- path/directory
- image filename/url
- fallback CDN replacement

## Arsitektur integrasi
### Data flow
`ShinigamiParserAdapter` → `ShinigamiRepository` → `ShinigamiReaderViewModel` → Compose UI

### Fallback strategy
Karena source live bisa gagal / timeout / dibatasi jaringan:
- aplikasi tetap punya demo dataset lokal
- UI tetap jalan walau API live gagal
- status source ditampilkan di Home / Discover / Profile / Detail / Reader

### Cache strategy
Saya mengikuti pola parser Java Anda dengan cache TTL 12 menit untuk:
- list
- detail
- chapter list
- page list
- chapter id lookup

## Keterbatasan saat ini
Karena sandbox sesi ini tidak punya Android SDK/JDK yang lengkap dan API source tidak bisa saya hit langsung dari environment, saya belum bisa memverifikasi runtime request end-to-end di sini.

Tetapi secara struktur kode, integrasi parser sudah saya siapkan agar tinggal Anda buka di Android Studio lalu sync/build.

## Langkah lanjutan yang saya sarankan
1. Tambahkan Room untuk history, bookmark, dan offline progress
2. Tambahkan DataStore untuk reader settings persisten
3. Tambahkan WorkManager untuk smart prefetch & background download
4. Tambahkan Hilt untuk dependency injection
5. Tambahkan paging dan error-state UI yang lebih granular
