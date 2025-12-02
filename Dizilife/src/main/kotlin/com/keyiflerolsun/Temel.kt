// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class Temel : MainAPI() {
    override var mainUrl              = "https://dizi25.life"
    override var name                 = "Temel"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = true
    override val supportedTypes       = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/tur/aksiyon/"     to "Aksiyon",
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

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/arama?q=${query}").document

        // Arama sonuçları content-card yapısını kullanıyor
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

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("h1")?.text()?.trim() 
            ?: document.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: return null
        
        val poster          = fixUrlNull(
            document.selectFirst("div.poster img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            } ?: document.selectFirst("meta[property='og:image']")?.attr("content")
        )
        
        val description     = document.selectFirst("div.wp-content p")?.text()?.trim()
            ?: document.selectFirst("meta[property='og:description']")?.attr("content")?.trim()
            ?: document.selectFirst("div.description")?.text()?.trim()
        
        val year            = document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
            ?: Regex("""(\d{4})""").find(document.selectFirst("div.extra")?.text() ?: "")?.groupValues?.get(1)?.toIntOrNull()
        
        val tags            = document.select("div.sgeneros a").mapNotNull { it.text().takeIf { it.isNotEmpty() } }
            .ifEmpty { document.select("a[href*='/tur/']").mapNotNull { it.text().trim().takeIf { it.isNotEmpty() } } }
        
        val rating          = document.selectFirst("span.dt_rating_vgs")?.text()?.trim()?.toRatingInt()
            ?: document.selectFirst("span.rating")?.text()?.trim()?.toRatingInt()
        
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

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
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

    private fun Element.toRecommendationResult(): SearchResponse? {
        val title     = this.selectFirst("a img")?.attr("alt")?.trim() 
            ?: this.selectFirst("a")?.attr("title")?.trim()
            ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(
            this.selectFirst("a img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("data-lazy-src").takeIf { it.isNotEmpty() }
                    ?: img.attr("src")
            }
        )

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("STF", "data » ${data}")
        val document = app.get(data, referer = data).document

        // İframe'leri bul
        val iframes = document.select("iframe").mapNotNull { 
            fixUrlNull(it.attr("data-src").takeIf { it.isNotEmpty() } ?: it.attr("src"))
        }

        // Eğer iframe yoksa, video player container'ı kontrol et
        if (iframes.isEmpty()) {
            val videoContainer = document.selectFirst("div.video-player, div.player, div#player")
            val iframe = videoContainer?.selectFirst("iframe")?.let {
                fixUrlNull(it.attr("data-src").takeIf { it.isNotEmpty() } ?: it.attr("src"))
            }
            
            if (iframe != null) {
                Log.d("STF", "iframe bulundu » $iframe")
                loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
                return true
            }
        }

        // Tüm iframe'leri işle
        for (iframe in iframes) {
            if (iframe.isNotEmpty()) {
                Log.d("STF", "iframe işleniyor » $iframe")
                loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
            }
        }

        return iframes.isNotEmpty()
    }
}