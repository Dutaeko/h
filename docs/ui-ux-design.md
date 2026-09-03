# UI / UX Design — Shinigami Reader

## 1. Tujuan Produk
Aplikasi ini dirancang untuk pengguna yang:
- membaca manga harian dengan sesi lama,
- ingin cepat kembali ke chapter terakhir,
- suka mengeksplor judul baru lewat cover dan genre,
- butuh pengalaman reader yang nyaman, cepat, dan minim gangguan.

## 2. Prinsip Desain
1. **Reading-first** — semua alur harus mengarahkan user kembali ke proses membaca secepat mungkin.
2. **Visual hierarchy kuat** — cover dominan, judul jelas, metadata sekunder.
3. **Low-friction navigation** — home, discover, library, profile cukup 1 tap.
4. **Context preserved** — progress baca, chapter terakhir, dan status offline selalu terlihat.
5. **Dark-friendly** — cocok untuk penggunaan malam hari.

## 3. Arsitektur Navigasi
- **Home**: continue reading, spotlight, latest update, rekomendasi personal
- **Discover**: search, filter genre/status, katalog grid
- **Library**: koleksi tersimpan, status, offline, unread
- **Detail**: info manga, genre, CTA, chapter list
- **Reader**: halaman manga, quick settings, next chapter
- **Profile**: statistik dan pengaturan otomatisasi

## 4. Informasi yang Diprioritaskan
### Home
- Continue reading card
- Hero manga pilihan
- Latest updates
- Recommendation lanes

### Detail Manga
- Cover
- Title + author
- Rating + followers
- Status
- Genre chips
- Tombol baca terbaru / simpan
- Chapter list dengan badge offline/progress

### Reader
- Nama chapter
- Nama manga
- Reader mode aktif
- Brightness / spacing / comments / border crop
- Tombol next chapter

## 5. Desain Komponen
### Card System
- Radius 18–20dp
- Cover portrait 2:3
- Tinggi kontras antara background dan surface
- Metadata dibuat singkat agar tidak overload

### Chips
Dipakai untuk:
- genre
- status
- filter discover
- quick settings di reader
- unread/offline badges

### Bottom Navigation
- 4 item utama
- Label tetap terlihat
- Reader dan Detail disembunyikan dari bottom nav agar fokus

## 6. UX Flow Utama
### Flow A — Kembali membaca
1. User buka app
2. Continue Reading tampil di atas
3. Tap card
4. Masuk reader ke chapter progres terakhir
5. Selesai chapter → next chapter

### Flow B — Cari judul baru
1. User buka Discover
2. Ketik keyword atau pilih filter
3. Lihat grid hasil
4. Buka detail
5. Save ke library / langsung baca

### Flow C — Kelola library
1. User buka Library
2. Filter Reading / Completed / Offline
3. Cek unread chapter
4. Tap judul untuk buka detail

## 7. Design Tokens
### Color Mood
- Primary: ungu gelap premium
- Secondary: pink accent untuk elemen aktif
- Background: hampir hitam, cocok untuk sesi baca panjang
- Surface: abu sangat gelap agar card terangkat tapi tetap hemat mata

### Typography
- Headline: judul manga dan section besar
- Title: sub-section dan CTA penting
- Body: metadata dan deskripsi
- Label: chip, badge, info ringkas

## 8. Accessibility
- Kontras tinggi pada dark mode
- Target sentuh minimum 48dp
- Hierarki teks jelas
- Layout bebas clutter
- Cocok untuk one-hand navigation di layar besar

## 9. Fitur Lanjutan yang Disarankan
- Download manager dengan queue, retry, dan prioritas chapter
- Histori baca persisten via Room
- Sinkronisasi cloud user profile
- Notifikasi chapter terbaru
- Auto prefetch chapter berikutnya
- Komentar / reaksi komunitas per chapter
- Smart recommendation engine berdasarkan histori tag & completion
- Multiple source support bila parser bertambah

## 10. Catatan Integrasi Parser
Parser `Shinigami` sekarang sudah dimapping ke layer `MangaSourceParser` dan dipakai lewat `ShinigamiRepository` + `ShinigamiReaderViewModel`.

## 11. UX Ketahanan Jaringan
Karena source manga live bisa timeout, berubah domain, atau gagal merespons:
- app menampilkan status source secara jelas,
- UI tetap usable dengan fallback demo dataset,
- user tidak dipaksa melihat layar kosong,
- screen detail dan reader menampilkan state loading yang kontekstual.
