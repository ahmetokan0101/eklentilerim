// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class Dizilife : MainAPI() {
    override var mainUrl              = "https://dizi25.life"
    override var name                 = "Dizilife"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.TvSeries, TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/tur/aksiyon/"     to "Aksssssiyon",
        "${mainUrl}/tur/komedi/"      to "Komedi",
        "${mainUrl}/tur/dram/"        to "Dram",
        "${mainUrl}/tur/bilim-kurgu/" to "Bilim Kurgu",
        "${mainUrl}/tur/fantastik/"   to "Fantastik",
        "${mainUrl}/tur/korku/"       to "Korku",
        "${mainUrl}/tur/romantik/"    to "Romantik",
        "${mainUrl}/tur/gerilim/"     to "Gerilim",
        "${mainUrl}/tur/belgesel/"    to "Belgesel",
        "${mainUrl}/tur/anime/"       to "Anime",
        "${mainUrl}/tur/macera/"     to "Macera",
        "${mainUrl}/tur/suc/"         to "Suç",
        "${mainUrl}/tur/gizem/"       to "Gizem",
        "${mainUrl}/tur/tarih/"       to "Tarih",
        "${mainUrl}/tur/biyografi/"   to "Biyografi",
        "${mainUrl}/tur/aile/"        to "Aile",
        "${mainUrl}/tur/muzikal/"     to "Müzikal",
        "${mainUrl}/tur/savas/"       to "Savaş",
        "${mainUrl}/tur/western/"     to "Western",
        "${mainUrl}/tur/spor/"        to "Spor",
        "${mainUrl}/tur/gerceklik/"   to "Gerçeklik"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page > 1) "${request.data}?page=$page&sort=popularity" else request.data
        val document = app.get(url).document
        val home     = document.select("div.content-card").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val dataUrl   = this.attr("data-url") ?: return null
        val href      = fixUrlNull(dataUrl) ?: return null
        
        val title     = this.selectFirst("h3.card-title")?.text()?.trim() 
            ?: this.selectFirst("div.card-info h3")?.text()?.trim()
            ?: return null
        
        val posterUrl = fixUrlNull(
            this.selectFirst("div.card-image img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() } 
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            }
        )

        // Dizi/Film ayrımı: card-badge veya URL'den kontrol et
        val badge = this.selectFirst("div.card-badge")?.text()?.trim()?.lowercase()
        val isTvSeries = badge == "dizi" || dataUrl.contains("/dizi/", ignoreCase = true)
        val isMovie = badge == "film" || dataUrl.contains("/film/", ignoreCase = true)

        return when {
            isTvSeries -> newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
            isMovie -> newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
            else -> newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl } // Varsayılan: Film
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/?s=${query}").document

        // Arama sonuçları da content-card yapısını kullanıyor olabilir
        val results = document.select("div.content-card").mapNotNull { it.toMainPageResult() }
        if (results.isNotEmpty()) return results
        
        // Alternatif: Eğer farklı bir yapı varsa
        return document.select("div.result-item article, article").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h3, h2, .title, a")?.text()?.trim() ?: return null
        val href      = fixUrlNull(
            this.selectFirst("a")?.attr("href")
                ?: this.attr("data-url")
                ?: this.selectFirst("[data-url]")?.attr("data-url")
        ) ?: return null
        val posterUrl = fixUrlNull(
            this.selectFirst("img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            }
        )

        // Dizi/Film ayrımı: URL'den kontrol et
        val isTvSeries = href.contains("/dizi/", ignoreCase = true)
        val isMovie = href.contains("/film/", ignoreCase = true)

        return when {
            isTvSeries -> newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
            isMovie -> newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
            else -> newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl } // Varsayılan: Film
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("h1.content-title")?.text()?.trim()
            ?: document.selectFirst("h1")?.text()?.trim() 
            ?: document.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: return null
        
        // Banner/Cover image: div.dizi-cover-image img (üstteki büyük fotoğraf) - ÖNCELİKLİ
        // dizi-cover-overlay sadece CSS overlay, içinde resim yok, dizi-cover-image içindeki img kullanılıyor
        val banner          = fixUrlNull(
            document.selectFirst("div.dizi-cover-container div.dizi-cover-image img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            } ?: document.selectFirst("div.dizi-cover-image img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            }
        )
        
        // Poster: div.content-poster img (küçük poster) - Banner yoksa poster kullan
        val poster          = fixUrlNull(
            banner // Önce banner'ı poster olarak kullan
            ?: document.selectFirst("div.content-poster img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            } ?: document.selectFirst("div.poster img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            } ?: document.selectFirst("meta[property='og:image']")?.attr("content")
        )
        
        // content-description: p.content-description
        val description     = document.selectFirst("p.content-description")?.text()?.trim()
            ?: document.selectFirst("div.wp-content p")?.text()?.trim()
            ?: document.selectFirst("meta[property='og:description']")?.attr("content")?.trim()
            ?: document.selectFirst("div.description")?.text()?.trim()
        
        // year: span.year
        val year            = document.selectFirst("span.year")?.text()?.trim()?.toIntOrNull()
            ?: document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
            ?: Regex("""(\d{4})""").find(document.selectFirst("div.extra")?.text() ?: "")?.groupValues?.get(1)?.toIntOrNull()
        
        // age-rating: span.age-rating
        val ageRating       = document.selectFirst("span.age-rating")?.text()?.trim()
        
        // genre: span.genre (virgülle ayrılmış)
        val tags            = document.selectFirst("span.genre")?.text()?.trim()?.split(",")?.mapNotNull { it.trim().takeIf { it.isNotEmpty() } }
            ?: document.select("div.sgeneros a").mapNotNull { it.text().takeIf { it.isNotEmpty() } }
            .ifEmpty { document.select("a[href*='/tur/']").mapNotNull { it.text().trim().takeIf { it.isNotEmpty() } } }
        
        // rating-value: span.rating-value (Java'daki mantık: toDoubleOrNull kullan)
        val ratingText      = document.selectFirst("span.rating-value")?.text()?.trim()
            ?: document.selectFirst("span.dt_rating_vgs")?.text()?.trim()
            ?: document.selectFirst("span.rating")?.text()?.trim()
        val rating          = ratingText?.toDoubleOrNull()
        
        val duration        = document.selectFirst("span.runtime")?.text()?.split(" ")?.first()?.trim()?.toIntOrNull()
            ?: Regex("""(\d+)\s*min""").find(document.text())?.groupValues?.get(1)?.toIntOrNull()
        
        val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
            .ifEmpty { document.select("div.related article").mapNotNull { it.toRecommendationResult() } }
        
        val actors          = document.select("span.valor a").mapNotNull { 
            val actorName = it.text().trim()
            if (actorName.isNotEmpty()) Actor(actorName) else null
        }.ifEmpty { 
            document.select("div.cast a").mapNotNull {
                val actorName = it.text().trim()
                if (actorName.isNotEmpty()) Actor(actorName) else null
            }
        }
        
        val trailer         = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { "https://www.youtube.com/embed/$it" }
            ?: document.selectFirst("iframe[src*='youtube']")?.attr("src")
            ?: document.selectFirst("a[href*='youtube.com']")?.attr("href")?.let { 
                Regex("""(?:youtube\.com\/watch\?v=|youtu\.be\/|embed\/)([^&\s]+)""").find(it)?.groupValues?.get(1)?.let { 
                    "https://www.youtube.com/embed/$it" 
                }
            }

        // Dizi/Film ayrımı: URL'den kontrol et
        val isTvSeries = url.contains("/dizi/", ignoreCase = true)
        val isMovie = url.contains("/film/", ignoreCase = true)

        // Rating'i score'a çevir (Java'daki mantık: Score.from10 kullan)
        val score = rating?.let { Score.from10(it) }

        return if (isTvSeries) {
            // Dizi için bölüm listesi - HTML yapısına göre: div.episode-card
            val episodes = document.select("div.episode-card").mapNotNull {
                // Episode başlığı - h3.episode-title
                val epName = it.selectFirst("h3.episode-title")?.text()?.trim() 
                    ?: "Bölüm"
                
                // Season ve Episode numaralarını data attribute'larından al
                val epSeason = it.attr("data-season-number").toIntOrNull()
                val epEpisode = it.attr("data-episode-number").toIntOrNull()
                val epEpisodeId = it.attr("data-episode-id")
                
                // URL oluştur: episode-id ile veya base URL + season/episode
                val slug = url.substringAfter("/dizi/").substringBefore("?")
                val epHref = when {
                    epEpisodeId.isNotEmpty() && epSeason != null && epEpisode != null -> {
                        // Episode ID, season ve episode varsa: /dizi/{slug}/sezon-{season}/bolum-{episode}
                        fixUrlNull("${mainUrl}/dizi/${slug}/sezon-${epSeason}/bolum-${epEpisode}")
                    }
                    epSeason != null && epEpisode != null -> {
                        // Season ve episode varsa, URL oluştur
                        fixUrlNull("${mainUrl}/dizi/${slug}/sezon-${epSeason}/bolum-${epEpisode}")
                    }
                    epEpisodeId.isNotEmpty() -> {
                        // Sadece episode ID varsa
                        fixUrlNull("${mainUrl}/bolum/${epEpisodeId}")
                    }
                    else -> url // Fallback: ana URL
                } ?: url
                
                // Episode poster'ı - div.episode-thumbnail img
                val epPoster = fixUrlNull(
                    it.selectFirst("div.episode-thumbnail img")?.let { img ->
                        img.attr("data-src").takeIf { it.isNotEmpty() }
                            ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                            ?: img.attr("src")
                    }
                )

                newEpisode(epHref) {
                    this.name = epName
                    this.episode = epEpisode
                    this.season = epSeason
                    this.posterUrl = epPoster
                }
            }.sortedWith(compareBy<Episode> { it.season ?: 0 }.thenBy { it.episode ?: 0 })

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.score           = score
                this.duration        = duration
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        } else {
            // Film için
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.score           = score
                this.duration        = duration
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        }
    }

    private fun Element.toRecommendationResult(): SearchResponse? {
        // Java'daki mantığa göre: h3.card-title kullan ve onclick'ten URL çıkar
        val title = this.selectFirst("h3.card-title")?.text()?.trim() 
            ?: this.selectFirst("a img")?.attr("alt")?.trim() 
            ?: this.selectFirst("a")?.attr("title")?.trim()
            ?: return null
        
        // onclick attribute'undan URL çıkar (Java'daki mantık: window.location.href='...')
        val href = this.attr("onclick")?.let { onclick ->
            Regex("""window\.location\.href=['"]([^'"]+)['"]""").find(onclick)?.groupValues?.get(1)
        }?.let { fixUrlNull(it) }
            ?: fixUrlNull(this.selectFirst("a")?.attr("href"))
            ?: return null
        
        val posterUrl = fixUrlNull(
            this.selectFirst("img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            }
        )
        
        // Rating'i score'a çevir (Java'daki mantık: .card-rating)
        val ratingText = this.selectFirst(".card-rating")?.text()?.trim()
        val rating = ratingText?.toDoubleOrNull()
        val score = rating?.let { Score.from10(it) }

        // Dizi/Film ayrımı: URL'den kontrol et
        val isTvSeries = href.contains("/dizi/", ignoreCase = true)
        val isMovie = href.contains("/film/", ignoreCase = true)

        return when {
            isTvSeries -> newTvSeriesSearchResponse(title, href, TvType.TvSeries) { 
                this.posterUrl = posterUrl
                this.score = score
            }
            isMovie -> newMovieSearchResponse(title, href, TvType.Movie) { 
                this.posterUrl = posterUrl
                this.score = score
            }
            else -> newMovieSearchResponse(title, href, TvType.Movie) { 
                this.posterUrl = posterUrl
                this.score = score
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("STF", "data » ${data}")
        val document = app.get(data, referer = data).document

        val iframes = mutableListOf<String>()

        // 1. Önce tüm iframe'leri bul (data-src ve src attribute'larını kontrol et)
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("data-src").takeIf { it.isNotEmpty() } 
                ?: iframe.attr("src").takeIf { it.isNotEmpty() }
            src?.let { 
                fixUrlNull(it)?.let { iframes.add(it) }
            }
        }

        // 2. iframe-container içindeki iframe'leri bul
        if (iframes.isEmpty()) {
            document.select("div.iframe-container iframe").forEach { iframe ->
                val src = iframe.attr("data-src").takeIf { it.isNotEmpty() } 
                    ?: iframe.attr("src").takeIf { it.isNotEmpty() }
                src?.let { 
                    fixUrlNull(it)?.let { iframes.add(it) }
                }
            }
        }

        // 3. videoIframe ID'li iframe'i bul
        if (iframes.isEmpty()) {
            document.selectFirst("#videoIframe")?.let { iframe ->
                val src = iframe.attr("data-src").takeIf { it.isNotEmpty() } 
                    ?: iframe.attr("src").takeIf { it.isNotEmpty() }
                src?.let { 
                    fixUrlNull(it)?.let { iframes.add(it) }
                }
            }
        }

        // 4. Video player container'ları kontrol et
        if (iframes.isEmpty()) {
            document.selectFirst("div.video-player, div.player, div#player")?.let { container ->
                container.select("iframe").forEach { iframe ->
                    val src = iframe.attr("data-src").takeIf { it.isNotEmpty() } 
                        ?: iframe.attr("src").takeIf { it.isNotEmpty() }
                    src?.let { 
                        fixUrlNull(it)?.let { iframes.add(it) }
                    }
                }
            }
        }

        // 5. Script içinde iframe URL'leri ara (dinamik yükleme için)
        if (iframes.isEmpty()) {
            val htmlContent = document.html()
            // iframe src pattern'leri ara
            val iframePatterns = listOf(
                Regex("""iframe\s+src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                Regex("""iframe\s+data-src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                Regex("""videoIframe\.src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                Regex("""setAttribute\(['"]src['"],\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE)
            )
            
            iframePatterns.forEach { pattern ->
                pattern.findAll(htmlContent).forEach { match ->
                    match.groupValues.getOrNull(1)?.let { url ->
                        fixUrlNull(url)?.let { 
                            if (!iframes.contains(it)) {
                                iframes.add(it)
                            }
                        }
                    }
                }
            }
        }

        // 6. Tüm bulunan iframe'leri işle
        if (iframes.isNotEmpty()) {
            Log.d("STF", "${iframes.size} iframe bulundu")
            for (iframe in iframes) {
                if (iframe.isNotEmpty()) {
                    Log.d("STF", "iframe işleniyor » $iframe")
                    loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
                }
            }
            return true
        }

        Log.d("STF", "iframe bulunamadı")
        return false
    }
}