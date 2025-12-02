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
        "${mainUrl}/tur/aile/"      to "Aisle",
        "${mainUrl}/tur/aksiyon/"   to "Aksiyon",
        "${mainUrl}/tur/animasyon/" to "Animasyon",
        "${mainUrl}/tur/belgesel/"  to "Belgesel"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}").document
        
        // Ana sayfa için Trend Diziler bölümünü çek
        val home = if (request.data == mainUrl || request.data == "${mainUrl}/") {
            document.select("ul.trends li").mapNotNull { it.toTrendResult() }
        } else {
            document.select("div.items article").mapNotNull { it.toMainPageResult() }
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toTrendResult(): SearchResponse? {
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val imgTag    = this.selectFirst("img")
        val posterUrl = fixUrlNull(imgTag?.attr("data-src")) ?: fixUrlNull(imgTag?.attr("src"))
        
        // URL'den başlık çıkar (örn: /series/stranger-things -> Stranger Things)
        val slug = href.substringAfterLast("/").trim()
        val title = slug.split("-")
            .joinToString(" ") { word -> 
                word.replaceFirstChar { char -> char.uppercaseChar() }
            }
        
        // URL'den tip belirleme (dizi/film)
        val isTvSeries = href.contains("/series/")
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title     = this.selectFirst("div.flbaslik")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))
        
        // URL veya başlıktan tip belirleme (dizi/film)
        val isTvSeries = href.contains("/dizi/") || href.contains("/bolum/") || href.contains("/series/") || title.contains("Sezon") || title.contains("Bölüm")
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
        val href      = fixUrlNull(this.selectFirst("div.title a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        
        // URL veya başlıktan tip belirleme (dizi/film)
        val isTvSeries = href.contains("/dizi/") || href.contains("/bolum/") || title.contains("Sezon") || title.contains("Bölüm")
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

        // URL veya içerikten tip belirleme (dizi/film)
        val isTvSeries = url.contains("/dizi/") || url.contains("/bolum/") || title.contains("Sezon") || title.contains("Bölüm")
        
        // Bölümleri kontrol et
        val episodes = if (isTvSeries) {
            document.select("div.episodes article, div.bolumler article, div.seasons article").mapNotNull {
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
        } else {
            emptyList()
        }

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
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("a img")?.attr("data-src"))
        
        // URL veya başlıktan tip belirleme (dizi/film)
        val isTvSeries = href.contains("/dizi/") || href.contains("/bolum/") || title.contains("Sezon") || title.contains("Bölüm")
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("STF", "data » ${data}")
        val document = app.get(data).document

        // TODO:
        // loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)

        return true
    }
}