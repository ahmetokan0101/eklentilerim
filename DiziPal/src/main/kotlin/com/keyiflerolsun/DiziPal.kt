// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class Dizipal : MainAPI() {
    override var mainUrl              = "https://dizipal1515.com/"
    override var name                 = "Dizipal"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}"                to "Öne Çıkanlar",
        "${mainUrl}/tur/aile/"      to "Aile",
        "${mainUrl}/tur/aksiyon/"   to "Aksiyon",
        "${mainUrl}/tur/animasyon/" to "Animasyon",
        "${mainUrl}/tur/belgesel/"  to "Belgesel"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data.trimEnd('/')
        
        // Ana sayfa için Trend Diziler bölümünü çek
        // request.name kontrolü daha güvenilir
        val isHomePage = request.name == "Öne Çıkanlar" || 
                        url == mainUrl.trimEnd('/') || 
                        url == "${mainUrl.trimEnd('/')}/"
        
        // Öne Çıkanlar için sayfalama yok, sadece ilk sayfayı göster
        if (isHomePage && page > 1) {
            return newHomePageResponse(request.name, emptyList())
        }
        
        val document = app.get(url).document
        
        val home = if (isHomePage) {
            // Trend Diziler bölümünü çek ve duplicate'leri engelle
            val trends = document.select("ul.trends li").mapNotNull { it.toTrendResult() }
            val uniqueTrends = if (trends.isNotEmpty()) {
                trends.distinctBy { it.url } // Aynı URL'li öğeleri filtrele
            } else {
                // Alternatif selector'lar dene
                document.select("ul.trends > li").mapNotNull { it.toTrendResult() }
                    .distinctBy { it.url }
            }
            uniqueTrends
        } else {
            document.select("div.items article").mapNotNull { it.toMainPageResult() }
                .distinctBy { it.url } // Diğer sayfalarda da duplicate kontrolü
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toTrendResult(): SearchResponse? {
        val aTag = this.selectFirst("a") ?: return null
        var rawHref = aTag.attr("href")
        
        // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        // Poster opsiyonel - olmasa da devam et
        val imgTag = this.selectFirst("img")
        val posterUrl = imgTag?.let { 
            fixUrlNull(it.attr("data-src")) ?: fixUrlNull(it.attr("src"))
        }
        
        // URL'den başlık çıkar (örn: /series/stranger-things -> Stranger Things)
        val slug = href.substringAfterLast("/").trim()
        if (slug.isEmpty()) return null
        
        val title = slug.split("-")
            .joinToString(" ") { word -> 
                word.replaceFirstChar { char -> char.uppercaseChar() }
            }
        
        // Detaylı tip belirleme (dizi/film)
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { 
                this.posterUrl = posterUrl
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { 
                this.posterUrl = posterUrl
            }
        }
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title     = this.selectFirst("div.flbaslik")?.text() ?: return null
        val rawHref   = this.selectFirst("a")?.attr("href") ?: return null
        
        // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))
        
        // Detaylı tip belirleme (dizi/film)
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/?s=${query}").document

        return document.select("div.result-item article").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.title a")?.text() ?: return null
        val rawHref   = this.selectFirst("div.title a")?.attr("href") ?: return null
        
        // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        
        // Detaylı tip belirleme (dizi/film)
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("h1")?.text()?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("div.poster img")?.attr("src"))
        val description     = document.selectFirst("div.wp-content p")?.text()?.trim()
        val year            = document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
        val tags            = document.select("div.sgeneros a").map { it.text() }
        val rating          = document.selectFirst("span.dt_rating_vgs")?.text()?.trim()?.toRatingInt()
        val duration        = document.selectFirst("span.runtime")?.text()?.split(" ")?.first()?.trim()?.toIntOrNull()
        val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
        val actors          = document.select("span.valor a").map { Actor(it.text()) }
        val trailer         = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { "https://www.youtube.com/embed/$it" }

        // Önce bölümleri kontrol et - eğer bölümler varsa kesinlikle dizi
        val episodes = document.select("div.episodes article, div.bolumler article, div.seasons article").mapNotNull {
            val epName    = it.selectFirst("a")?.text()?.trim() ?: return@mapNotNull null
            val epHref    = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val epEpisode = Regex("""(\d+)\.?\s*[Bb]ölüm""").find(epName)?.groupValues?.get(1)?.toIntOrNull()
            val epSeason  = Regex("""(\d+)\.?\s*[Ss]ezon""").find(epName)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            newEpisode(epHref) {
                this.name    = epName
                this.season  = epSeason
                this.episode = epEpisode
            }
        }
        
        // Detaylı tip belirleme - bölümler varsa kesinlikle dizi
        val isTvSeries = episodes.isNotEmpty() || determineTvType(url, title)

        return if (isTvSeries && episodes.isNotEmpty()) {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.rating          = rating
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.rating          = rating
                this.duration        = duration
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        }
    }

    private fun Element.toRecommendationResult(): SearchResponse? {
        val title     = this.selectFirst("a img")?.attr("alt") ?: return null
        val rawHref   = this.selectFirst("a")?.attr("href") ?: return null
        
        // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("a img")?.attr("data-src"))
        
        // Detaylı tip belirleme (dizi/film)
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }
    
    /**
     * URL ve başlıktan detaylı şekilde film/dizi tipini belirler
     * Öncelik sırası:
     * 1. /movies/ varsa -> Film
     * 2. /series/ varsa -> Dizi
     * 3. /dizi/ varsa -> Dizi
     * 4. /bolum/ varsa -> Dizi
     * 5. Başlıkta "Sezon" veya "Bölüm" varsa -> Dizi
     * 6. Diğer durumlarda -> Film
     */
    private fun determineTvType(url: String, title: String = ""): Boolean {
        // /movies/ varsa kesinlikle film
        if (url.contains("/movies/") || url.contains("/movie/")) {
            return false
        }
        
        // /series/ varsa kesinlikle dizi
        if (url.contains("/series/")) {
            return true
        }
        
        // /dizi/ varsa kesinlikle dizi
        if (url.contains("/dizi/")) {
            return true
        }
        
        // /bolum/ varsa kesinlikle dizi
        if (url.contains("/bolum/")) {
            return true
        }
        
        // Başlıkta "Sezon" veya "Bölüm" varsa dizi
        if (title.contains("Sezon", ignoreCase = true) || 
            title.contains("Bölüm", ignoreCase = true) ||
            title.contains("Season", ignoreCase = true) ||
            title.contains("Episode", ignoreCase = true)) {
            return true
        }
        
        // Diğer durumlarda film varsay
        return false
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("STF", "data » ${data}")
        val document = app.get(data).document

        // TODO:
        // loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)

        return true
    }
}