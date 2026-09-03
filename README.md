# Shinigami Reader

Aplikasi Android baca manga modern berbasis **Kotlin + Jetpack Compose** dengan fokus pada **fitur kompleks**, **UX nyaman untuk membaca lama**, dan sekarang sudah **diarahkan berdasarkan parser Shinigami** yang Anda paste.

## Status
Repository awal ini sudah saya ubah menjadi fondasi aplikasi Android yang cukup lengkap, meliputi:

- Arsitektur aplikasi Compose modern
- Navigasi multi-screen
- Desain UI/UX untuk home, discover, library, detail, reader, dan profile/settings
- Domain model manga yang siap dikembangkan
- **Implementasi parser `ShinigamiParserAdapter`** hasil konversi dari parser Java yang Anda kirim
- Repository + ViewModel untuk memuat source live dan fallback demo lokal
- Mockup UI concept untuk presentasi desain

## Fitur kompleks yang sudah dibuat

### 1. Home personalisasi
- Continue reading card
- Hero spotlight manga
- Latest updates, trending, top rated
- Banner status source live/fallback
- Progress membaca per chapter

### 2. Discover & live search
- Search bar besar
- Filter chip preset: Trending, Latest, Top Rated, Action, Romance, Completed
- Grid katalog manga
- Trigger pencarian live ke parser source

### 3. Library management
- Status koleksi: Reading, Planned, Completed, On Hold
- Badge unread chapter
- Jumlah chapter offline
- Ring progress per judul

### 4. Detail manga lengkap
- Metadata detail (author, rating, followers, status)
- Tag/genre chip
- CTA baca chapter terbaru / simpan ke library
- Daftar chapter dari parser
- Banner status loading source

### 5. Reader engine UX
- Mode baca: Vertical, Webtoon, RTL Paged, LTR Paged, Double Page
- Reader settings chip cepat
- Load halaman chapter dari endpoint parser
- Tampilan status source/loading
- Siap dikembangkan ke prefetch, cache, dan offline mode

### 6. Profile & settings
- Reading target / statistik mingguan
- Reader preset info
- Source health panel
- Pengaturan smart download, data saver, parental lock, notification

## Struktur proyek

```text
app/
  src/main/java/com/dutaeko/shinigamireader/
    core/model/              # model domain
    data/repository/         # repository demo + remote source
    data/source/             # interface parser + adapter Shinigami + cache
    ui/                      # viewmodel state app
    ui/components/           # reusable components Compose
    ui/navigation/           # route aplikasi
    ui/screens/              # screen Compose
    ui/theme/                # design system theme
  src/main/res/values/       # string/theme dasar

docs/
  ui-ux-design.md            # dokumen UX lengkap
  parser-integration.md      # status integrasi parser
  shinigami-api-mapping.md   # mapping parser Java ke Kotlin
  shinigami-reader-ui-concept.png
```

## Parser yang diintegrasikan
Parser Java yang Anda kirim memakai endpoint utama berikut:
- `https://api.shngm.io/v1/manga/list`
- `https://api.shngm.io/v1/manga/detail/{id}`
- `https://api.shngm.io/v1/chapter/{id}/list?page_size=3000`
- `https://api.shngm.io/v1/chapter/detail/{chapterId}`

Implementasi Kotlin sekarang sudah memuat:
- request header yang meniru parser asli
- cache TTL 12 menit
- mapping taxonomy genre/format/author
- mapping chapter id
- mapping halaman reader + fallback CDN replacement

## Stack yang dipakai
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coil Compose
- ViewModel Compose
- Kotlin Coroutines
- OkHttp
- Gson

## Dokumen desain
- `docs/ui-ux-design.md`
- `docs/parser-integration.md`
- `docs/shinigami-api-mapping.md`
- `docs/shinigami-reader-ui-concept.png`

## Catatan build
Di sandbox sesi ini saya tidak bisa menjalankan build APK end-to-end karena environment tidak menyediakan JDK/Android SDK lengkap. Tapi struktur project dan integrasi parser sudah saya siapkan agar bisa langsung dilanjutkan di Android Studio.

## Langkah berikutnya yang paling bagus
1. Tambahkan Room untuk history/progress/bookmark
2. Tambahkan DataStore untuk settings reader
3. Tambahkan WorkManager untuk download manager & prefetch
4. Tambahkan Hilt + modularisasi MVVM lebih lanjut
5. Tambahkan notifikasi update chapter dan sinkronisasi cloud
