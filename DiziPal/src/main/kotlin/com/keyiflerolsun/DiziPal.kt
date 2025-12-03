package com.keyiflerolsun

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.network.CloudflareKiller
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import java.net.URL
import org.json.JSONObject
import kotlin.text.Charsets

class Dizipal : MainAPI() {
    override var mainUrl              = "https://dizipal1515.com/"
    override var name                 = "Dizipal"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    // ! CloudFlare bypass
    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L  // 0.15 saniye
    override var sequentialMainPageScrollDelay = 150L  // 0.15 saniye

    // ! CloudFlare v2
    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    // Kategori ID'leri - API'den içerik çekmek için
    private val kategoriIdMap = mapOf(
        "aile" to "20",
        "aksiyon" to "13",
        "aksiyon-macera" to "27",
        "animasyon" to "19",
        "belgesel" to "12",
        "bilim-kurgu" to "17",
        "bilim-kurgu-fantazi" to "28",
        "biyografi" to "2",
        "cocuklar" to "31",
        "comedy" to "33",
        "dram" to "4",
        "drama" to "34",
        "fantastik" to "21",
        "game-show" to "9",
        "gerceklik" to "26",
        "gerilim" to "15",
        "gizem" to "7",
        "haberler" to "24",
        "kisa" to "14",
        "komedi" to "3",
        "korku" to "22",
        "macera" to "18",
        "muzik" to "10",
        "muzikal" to "23",
        "reality-tv" to "11",
        "romance" to "37",
        "romantik" to "8",
        "savas" to "16",
        "savas-politik" to "30",
        "spor" to "25",
        "suc" to "1",
        "talk" to "32",
        "talk-show" to "5",
        "tarih" to "6",
        "thriller" to "36",
        "western" to "29"
    )

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            
            // Response body'nin ilk 1MB'ını parse et (stream bozulmaz)
            // peekBody() kullanılmalı, normal body() stream'i bozar
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())
            
            // Cloudflare challenge sayfasını kontrol et
            // En yaygın kullanılan yöntem: HTML içinde "Just a moment" kontrolü
            if (doc.html().contains("Just a moment")) {
                // Cloudflare tespit edildi, CloudflareKiller ile challenge'ı çöz
                return cloudflareKiller.intercept(chain)
            }
            
            return response
        }
    }

    override val mainPage = mainPageOf(
        "${mainUrl}"                to "Öne Çıkanlar",
        "kategori:bilim-kurgu"      to "Bilim Kurgu",
        "kategori:aile"             to "Aile",
        "kategori:aksiyon"          to "Aksiyon",
        "kategori:animasyon"        to "Animasyon",
        "kategori:belgesel"         to "Belgesel",
        "kategori:komedi"           to "Komedi",
        "kategori:korku"            to "Korku",
        "kategori:dram"             to "Dram",
        "kategori:gerilim"          to "Gerilim",
        "kategori:macera"           to "Macera",
        "kategori:romantik"         to "Romantik",
        "kategori:fantastik"        to "Fantastik",
        "kategori:gizem"            to "Gizem",
        "kategori:suc"              to "Suç",
        "kategori:biyografi"        to "Biyografi"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data.trimEnd('/')
        
        // Ana sayfa kontrolü - request.name ile kontrol et (daha güvenilir)
        val isHomePage = request.name == "Öne Çıkanlar"
        
        // Kategori sayfası kontrolü (kategori:xxx formatı)
        val isKategoriPage = url.startsWith("kategori:")
        
        // Öne Çıkanlar için sayfalama yok, sadece ilk sayfayı göster
        if (isHomePage && page > 1) {
            return newHomePageResponse(request.name, emptyList())
        }
        
        // Ana sayfa ise direkt HTML'den Trend Diziler'i çek
        if (isHomePage) {
            val document = app.get(mainUrl).document
        
            // Trend Diziler bölümünü çek - HTML'den direkt al, filtre yok
            val trends = document.select("ul.trends li").mapNotNull { it.toTrendResult() }
            val finalTrends = if (trends.isNotEmpty()) {
                trends.distinctBy { it.url }
            } else {
                // Alternatif selector'lar dene
                val altTrends = document.select("ul.trends > li").mapNotNull { it.toTrendResult() }
                if (altTrends.isNotEmpty()) {
                    altTrends.distinctBy { it.url }
                } else {
                    // Daha fazla alternatif dene
                    document.select("article.movie-type-genres ul.trends li").mapNotNull { it.toTrendResult() }
                    .distinctBy { it.url }
            }
            }
            
            return newHomePageResponse(request.name, finalTrends)
        }
        
        // Kategori sayfasıysa API'yi kullan
        if (isKategoriPage) {
            val kategoriKey = url.substringAfter("kategori:")
            val kategoriId = kategoriIdMap[kategoriKey]
            
            if (kategoriId != null) {
                val results = getCategoryContent(kategoriId, page)
                return newHomePageResponse(request.name, results, results.isNotEmpty())
            }
        }
        
        // Diğer durumlar için normal sayfa çek
        val document = app.get(url).document
        val home = document.select("div.items article").mapNotNull { it.toMainPageResult() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, home)
    }

    /**
     * Kategori API'sinden içerik çeker
     */
    private suspend fun getCategoryContent(kategoriId: String, sayfa: Int = 1, sayfaBasina: Int = 30): List<SearchResponse> {
        val apiUrl = "${mainUrl}/bg/findseries"
        
        val formData = mapOf(
            "cKey" to "c61f91c5141d178450934fe81c0a2029",
            "cValue" to "MTc2NDcwMDgwMGFiNmI2ZDEzNDg1ZmE4MjQyZmU2YzRhNzc0OTE2NTM3NjQyMTU5Mjk2YTI4YTU0NjUyMjI2ZmVjMzFkYzBkMWQyMWY4YzdiNA==",
            "currentPage" to sayfa.toString(),
            "currentPageCount" to sayfaBasina.toString(),
            "categoryIdsComma" to kategoriId,
            "imdbPointMin" to "0",
            "imdbPointMax" to "10",
            "releaseYearStart" to "1923",
            "releaseYearEnd" to "2025",
            "countryIdsComma" to "",
            "orderType" to "date_desc",
            "yerliCountry" to "9"
        )
        
        val headers = mapOf(
            "Content-Type" to "application/x-www-form-urlencoded",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Referer" to mainUrl,
            "Origin" to mainUrl.replace(Regex("/$"), ""),
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7"
        )
        
        try {
            val response = app.post(apiUrl, data = formData, headers = headers)
            
            if (response.code == 200) {
                val jsonResponse = response.parsedSafe<CategoryApiResponse>()
                
                // Eğer parsedSafe null dönerse, raw response'u kontrol et
                if (jsonResponse == null) {
                    val responseText = response.text
                    
                    // Manuel JSON parsing dene
                    try {
                        val jsonText = responseText
                        if (jsonText.contains("\"html\"")) {
                            val htmlStart = jsonText.indexOf("\"html\":\"") + 8
                            val htmlEnd = jsonText.indexOf("\"", htmlStart + 1)
                            if (htmlEnd > htmlStart) {
                                val htmlContent = jsonText.substring(htmlStart, htmlEnd)
                                    .replace("\\\"", "\"")
                                    .replace("\\n", "\n")
                                    .replace("\\/", "/")
                                
                                if (htmlContent.isNotEmpty()) {
                                    return parseCategoryHtml(htmlContent)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Silent fail
                    }
                    return emptyList()
                }
                
                if (jsonResponse.state == true || jsonResponse.data != null) {
                    val htmlContent = jsonResponse.data?.html ?: return emptyList()
                    
                    if (htmlContent.isNotEmpty()) {
                        return parseCategoryHtml(htmlContent)
                    }
                }
            }
        } catch (e: Exception) {
            // Silent fail
        }
        
        return emptyList()
    }

    /**
     * Kategori API'den gelen HTML'i parse eder
     */
    private fun parseCategoryHtml(htmlContent: String): List<SearchResponse> {
        val document = Jsoup.parse(htmlContent)
        
        // Tüm div'leri bul ve class attribute'unu kontrol et
        // Python kodundaki gibi bg-[#22232a] class'ına sahip div'leri bul
        val allDivs = document.select("div")
        val kartlar = allDivs.filter { div ->
            val classAttr = div.attr("class")
            // bg- ile başlayan class'ları bul (bg-[#22232a] gibi)
            classAttr.contains("bg-") && div.selectFirst("a[href]") != null
        }
        
        // İlk 2 içeriği atla (genelde kategoriye ait olmayan öne çıkan içerikler)
        // Ama eğer toplam 2 veya daha az içerik varsa hiçbirini atlama
        val cardsToProcess = if (kartlar.size > 2) {
            kartlar.drop(2)
        } else {
            kartlar
        }
        
        return cardsToProcess.mapNotNull { kart ->
            try {
                val linkElem = kart.selectFirst("a[href]") ?: return@mapNotNull null
                val href = linkElem.attr("href") ?: return@mapNotNull null
                
                // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
                val fixedHref = if (href.contains("/bolum/")) {
                    val fixed = fixUrlNull(href)?.replace("/bolum/", "/series/")
                    fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return@mapNotNull null
                } else {
                    fixUrlNull(href) ?: return@mapNotNull null
                }
                
                val titleAttr = linkElem.attr("title")
                
                // Başlık - önce h2, sonra title attribute
                val baslikElem = kart.selectFirst("h2")
                var title = baslikElem?.text()?.trim() ?: ""
                if (title.isEmpty() && titleAttr.isNotEmpty()) {
                    title = titleAttr.replace(" izle", "").trim()
                }
                
                if (title.isEmpty()) return@mapNotNull null
                
                // Poster
                val imgElem = kart.selectFirst("img")
                val posterUrl = imgElem?.let {
                    fixUrlNull(it.attr("data-src")) ?: fixUrlNull(it.attr("src"))
                }
                
                // Detaylı tip belirleme
                val isTvSeries = determineTvType(fixedHref, title)
                
                if (isTvSeries) {
                    newTvSeriesSearchResponse(title, fixedHref, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                } else {
                    newMovieSearchResponse(title, fixedHref, TvType.Movie) {
                        this.posterUrl = posterUrl
                    }
                }
            } catch (e: Exception) {
                null
            }
        }.distinctBy { it.url }
    }

    /**
     * Kategori API yanıt modeli
     */
    private data class CategoryApiResponse(
        @JsonProperty("state") val state: Boolean? = null,
        @JsonProperty("data") val data: CategoryApiData? = null
    )

    private data class CategoryApiData(
        @JsonProperty("html") val html: String? = null
    )

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

    // Sidebar'dan değer okuma yardımcı fonksiyonu
    private fun getSidebarValue(document: org.jsoup.nodes.Document, key: String, exactMatch: Boolean = true): String? {
        return document.select(".rigth-content li, ul.rigth-content li").firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            if (exactMatch) {
                keyText == key
            } else {
                keyText.contains(key, ignoreCase = true)
            }
        }?.selectFirst(".value a, .value")?.text()?.trim()
    }
    
    // Rating string'ini double'a çevirme (Score.from10 için)
    private fun String.toRatingDouble(): Double? {
        return this.replace(",", ".").toDoubleOrNull()
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("h1")?.text()?.trim() ?: return null
        
        // Poster: Yeni yapıda .page-top img veya eski yapıda div.poster img
        val poster = fixUrlNull(
            document.selectFirst(".page-top img")?.attr("src") ?:
            document.selectFirst(".page-top img.lazyload")?.attr("data-src") ?:
            document.selectFirst("div.poster img")?.attr("src")
        )
        
        // Açıklama: Yeni yapıda div.w-full p.text-white.text-base
        val description = document.selectFirst("div.w-full p.text-white.text-base")?.text()?.trim()
            ?: document.selectFirst("div.w-full p.mt-4")?.text()?.trim()
            ?: document.selectFirst("div.wp-content p")?.text()?.trim()
        
        // Yıl: Sidebar'dan "Yıl" key'i ile (içerir kontrolü)
        val year = document.select(".rigth-content li, ul.rigth-content li").firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            keyText.contains("Yıl", ignoreCase = true) && !keyText.contains("Gösterim")
        }?.selectFirst(".value a, .value")?.text()?.trim()?.toIntOrNull()
            ?: document.select(".rigth-content li, ul.rigth-content li").firstOrNull {
                val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
                keyText.contains("Gösterim", ignoreCase = true)
            }?.selectFirst(".value")?.text()?.trim()?.toIntOrNull()
            ?: document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
        
        // Kategoriler: Sidebar'dan "Kategoriler" key'i ile
        val tags = (document.select(".rigth-content li, ul.rigth-content li").firstOrNull {
            it.selectFirst(".key")?.text()?.trim() == "Kategoriler"
        }?.select(".value a")?.map { it.text().trim() } ?: emptyList<String>())
            .ifEmpty { document.select("div.sgeneros a").map { it.text().trim() } }
        
        // Rating: Sidebar'dan "IMDB Puanı" key'i ile (içerir kontrolü) - Score.from10 için string/double
        val ratingString = document.select(".rigth-content li, ul.rigth-content li").firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            keyText.contains("IMDB", ignoreCase = true) || keyText.contains("Puan", ignoreCase = true)
        }?.selectFirst(".value")?.text()?.trim()
            ?: document.selectFirst("span.dt_rating_vgs")?.text()?.trim()
        
        val rating = ratingString?.toRatingDouble()?.let { Score.from10(it) }
        
        // Süre: Sidebar'dan "Süre" key'i ile
        val durationText = getSidebarValue(document, "Süre") ?: ""
        val duration = Regex("""(\d+)""").find(durationText)?.groupValues?.get(1)?.toIntOrNull()
            ?: document.selectFirst("span.runtime")?.text()?.split(" ")?.first()?.trim()?.toIntOrNull()
        
        // Öneriler: Eski selector'ları koruyoruz (varsa)
        val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
        
        // Oyuncular: Yeni yapıda .movie-actors .actor-item .name veya a[title] - resim URL'i ile birlikte
        val actors = document.select(".movie-actors a[href*='/oyuncu/']").mapNotNull {
            val name = it.selectFirst(".actor-item .name")?.text()?.trim()
                ?: it.selectFirst(".name")?.text()?.trim()
                ?: it.attr("title").takeIf { it.isNotBlank() }
            
            if (name == null || name.isBlank()) return@mapNotNull null
            
            // Oyuncu resmi: a tag'i içindeki img'den al
            val imageUrl = it.selectFirst("img")?.let { img ->
                fixUrlNull(img.attr("data-src").takeIf { it.isNotBlank() }
                    ?: img.attr("src"))
            }
            
            Actor(name, imageUrl)
        }.ifEmpty {
            document.select(".movie-actors .actor-item .name").mapNotNull {
                val name = it.text().trim()
                if (name.isBlank()) return@mapNotNull null
                
                // Oyuncu resmi: parent'a bak (a tag'i veya li)
                val parent = it.parent()?.parent()
                val imageUrl = parent?.selectFirst("img")?.let { img ->
                    fixUrlNull(img.attr("data-src").takeIf { it.isNotBlank() }
                        ?: img.attr("src"))
                }
                
                Actor(name, imageUrl)
            }
        }.ifEmpty {
            document.select("span.valor a").mapNotNull { 
                val name = it.text().trim()
                if (name.isBlank()) return@mapNotNull null
                
                // Eski yapıda resim yoksa null
                Actor(name)
            }
        }
        
        // Trailer: Eski regex'i koruyoruz
        val trailer = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { 
            "https://www.youtube.com/embed/$it" 
        }

        // Bölümler: Yeni yapıda .season-lists veya szn1, szn2 gibi sezonlar
        val episodes = mutableListOf<Episode>()
        
        // Tüm sezonları al (hidden olanlar dahil - Cloudstream3 kendisi gösterir)
        val allSeasons = document.select(".season-lists")
        
        if (allSeasons.isNotEmpty()) {
            // Yeni yapı: Her sezon için bölümleri al
            allSeasons.forEach { seasonDiv ->
                // Sezon numarasını szn1, szn2 gibi class'lardan çıkar
                val seasonFromClass = Regex("""szn(\d+)""").find(seasonDiv.classNames().joinToString(" "))?.groupValues?.get(1)?.toIntOrNull()
                
                seasonDiv.select(".grid > div").forEach { episodeDiv ->
                    val epLink = episodeDiv.selectFirst("a[data-dizipal-pageloader]") ?: return@forEach
                    val epHref = fixUrlNull(epLink.attr("href")) ?: return@forEach
                    
                    // Bölüm başlığı
                    val epTitle = epLink.selectFirst("h2")?.text()?.trim() ?: title
                    
                    // Sezon ve bölüm bilgisi - önce div'den, sonra URL'den
                    val epInfo = epLink.selectFirst("div[style*='font-size:13px']")?.text()?.trim() ?: ""
                    val epSeasonFromInfo = Regex("""(\d+)\.?\s*[Ss]ezon""").find(epInfo)?.groupValues?.get(1)?.toIntOrNull()
                    val epEpisodeFromInfo = Regex("""(\d+)\.?\s*[Bb]ölüm""").find(epInfo)?.groupValues?.get(1)?.toIntOrNull()
                    
                    // URL'den sezon ve bölüm çıkar (örn: wednesday-1x1 -> 1, 1)
                    val urlMatch = Regex("""-(\d+)x(\d+)(?:\.|$)""").find(epHref)
                    val epSeasonFromUrl = urlMatch?.groupValues?.get(1)?.toIntOrNull()
                    val epEpisodeFromUrl = urlMatch?.groupValues?.get(2)?.toIntOrNull()
                    
                    // Episode numarası için span'a da bak
                    val epNumber = episodeDiv.selectFirst("span.text-white.opacity-60")?.text()?.trim()?.toIntOrNull()
                    
                    // Öncelik sırası: class > info > URL
                    val epSeason = seasonFromClass ?: epSeasonFromInfo ?: epSeasonFromUrl ?: 1
                    val epEpisode = epEpisodeFromInfo ?: epEpisodeFromUrl ?: epNumber
                    
                    episodes.add(newEpisode(epHref) {
                        this.name = epTitle
                        this.season = epSeason
                        this.episode = epEpisode
                        // Bölüm için dizinin ana poster/backdrop resmini kullan
                        this.posterUrl = poster
                    })
                }
            }
        } else {
            // Eski yapı: Eski selector'ları dene
            episodes.addAll(document.select("div.episodes article, div.bolumler article, div.seasons article").mapNotNull {
                val epName = it.selectFirst("a")?.text()?.trim() ?: return@mapNotNull null
                val epHref = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val epEpisode = Regex("""(\d+)\.?\s*[Bb]ölüm""").find(epName)?.groupValues?.get(1)?.toIntOrNull()
                val epSeason = Regex("""(\d+)\.?\s*[Ss]ezon""").find(epName)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            newEpisode(epHref) {
                    this.name = epName
                    this.season = epSeason
                this.episode = epEpisode
                    // Bölüm için dizinin ana poster/backdrop resmini kullan
                    this.posterUrl = poster
                }
            })
        }
        
        // Detaylı tip belirleme - bölümler varsa kesinlikle dizi
        val isTvSeries = episodes.isNotEmpty() || determineTvType(url, title)

        return if (isTvSeries && episodes.isNotEmpty()) {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.score           = rating
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
                this.score           = rating
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

    // Stream URL çıkarma için constants
    private val PASSPHRASE = "3hPn4uCjTVtfYWcjIcoJQ4cL1WWk1qxXI39egLYOmNv6IblA7eKJz68uU3eLzux1biZLCms0quEjTYniGv5z1JcKbNIsDQFSeIZOBZJz4is6pD7UyWDggWWzTLBQbHcQFpBQdClnuQaMNUHtLHTpzCvZy33p6I7wFBvL4fnXBYH84aUIyWGTRvM2G5cfoNf4705tO2kv"
    private val SOURCE2_PATH = "/source2.php?v="

    // Şifreli veri modeli
    private data class EncryptedData(
        val ciphertext: String,
        val iv: String,
        val salt: String
    )

    // ADIM -1: HTML'den şifreli veriyi çıkar
    private suspend fun extractEncryptedDataFromUrl(url: String): EncryptedData? {
        try {
            val document = app.get(url).document
            val html = document.html()
            
            // data-rm-k="true" attribute'una sahip elementten veriyi çıkar - Python'daki pattern'ler
            var match = Regex("""data-rm-k=["']?true["']?[^>]*>([^<]+)""", RegexOption.IGNORE_CASE).find(html)
            if (match == null) {
                match = Regex("""data-rm-k=["']?true["']?[^>]*>\s*(\{[^}]+\})""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).find(html)
            }
            
            if (match != null) {
                var dataText = match.groupValues[1].trim()
                // Python'daki gibi HTML entity'leri decode et
                dataText = dataText.replace("&quot;", "\"").replace("&amp;", "&")
                
                try {
                    // JSON parse et - Python'da json.loads() direkt kullanıyor
                    // Önce JSON objesini bulmaya çalış
                    val jsonStart = dataText.indexOf('{')
                    val jsonEnd = dataText.lastIndexOf('}') + 1
                    if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        val jsonText = dataText.substring(jsonStart, jsonEnd)
                        val json = JSONObject(jsonText)
                        
                        // Python'daki gibi .get() ile optional al, yoksa empty string
                        val ciphertext = if (json.has("ciphertext")) json.getString("ciphertext") else ""
                        val iv = if (json.has("iv")) json.getString("iv") else ""
                        val salt = if (json.has("salt")) json.getString("salt") else ""
                        
                        if (ciphertext.isNotEmpty() && iv.isNotEmpty() && salt.isNotEmpty()) {
                            return EncryptedData(ciphertext, iv, salt)
                        }
                    } else {
                        // Direkt JSON string olabilir
                        val json = JSONObject(dataText)
                        val ciphertext = if (json.has("ciphertext")) json.getString("ciphertext") else ""
                        val iv = if (json.has("iv")) json.getString("iv") else ""
                        val salt = if (json.has("salt")) json.getString("salt") else ""
                        
                        if (ciphertext.isNotEmpty() && iv.isNotEmpty() && salt.isNotEmpty()) {
                            return EncryptedData(ciphertext, iv, salt)
                        }
                    }
                } catch (e: Exception) {
                    // JSON parse hatası - Python'da da try-except kullanıyor
                }
            }
        } catch (e: Exception) {
            // Hata durumunda null dön
        }
        return null
    }

    // ADIM 0: Şifreli veriyi decrypt et (PBKDF2-SHA512 + AES-CBC)
    private fun decryptIframeUrl(encryptedData: EncryptedData): String? {
        try {
            // Hex string'leri byte array'e çevir
            val saltBytes = hexStringToByteArray(encryptedData.salt)
            val ivBytes = hexStringToByteArray(encryptedData.iv)
            val ciphertextBytes = Base64.decode(encryptedData.ciphertext, Base64.DEFAULT)
            
            // PBKDF2-SHA512 ile key derivation (999 iterations)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val spec = PBEKeySpec(PASSPHRASE.toCharArray(), saltBytes, 999, 256)
            val tmpKey = factory.generateSecret(spec)
            val key = SecretKeySpec(tmpKey.encoded, "AES")
            
            // AES-CBC decrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivBytes))
            val decrypted = cipher.doFinal(ciphertextBytes)
            val iframeUrl = String(decrypted, Charsets.UTF_8).trim()
            
            // URL'i düzelt
            return when {
                iframeUrl.startsWith("//") -> "https:$iframeUrl"
                iframeUrl.startsWith("http") -> iframeUrl
                else -> "https://$iframeUrl"
            }
        } catch (e: Exception) {
            return null
        }
    }

    // Hex string'i byte array'e çevir
    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    // ADIM 1: iframe URL'inden player URL çıkar
    private suspend fun getPlayerUrl(iframeUrl: String): String? {
        try {
            val headers = mapOf(
                "Referer" to "${mainUrl}/",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
                "Origin" to mainUrl.replace(Regex("/$"), ""),
                "Connection" to "keep-alive",
                "Upgrade-Insecure-Requests" to "1",
                "Sec-Fetch-Dest" to "iframe",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Site" to "cross-site"
            )
            
            val response = app.get(iframeUrl, headers = headers, interceptor = interceptor)
            val html = response.text
            
            // window.openPlayer() içinden player URL çıkar - Python'daki pattern'ler
            var match = Regex("""window\.openPlayer\(['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE).find(html)
            if (match != null) {
                return match.groupValues[1]
            }
            
            // Alternatif pattern'ler (Python kodundan)
            val altPatterns = listOf(
                Regex("""openPlayer\(['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                Regex("""player.*?['"]([^'"]+source2[^'"]+)['"]""", RegexOption.IGNORE_CASE),
                Regex("""source2\.php\?v=([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
            )
            
            for (pattern in altPatterns) {
                match = pattern.find(html)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
            
            return null
        } catch (e: Exception) {
            return null
        }
    }

    // ADIM 2: source2.php'den M3U8 URL çıkar
    private suspend fun getM3u8Url(playerUrl: String, iframeUrl: String): String? {
        try {
            val url = URL(iframeUrl)
            val mainDomain = "${url.protocol}://${url.host}"
            val source2Url = "$mainDomain$SOURCE2_PATH$playerUrl"
            
            // Origin'i Python'daki gibi parse et: iframe_url.split('/')[0] + '//' + iframe_url.split('/')[2]
            val originUrl = URL(iframeUrl)
            val origin = "${originUrl.protocol}://${originUrl.host}"
            
            val headers = mapOf(
                "Referer" to iframeUrl,
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
                "Origin" to origin,
                "Connection" to "keep-alive",
                "Upgrade-Insecure-Requests" to "1",
                "Sec-Fetch-Dest" to "document",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Site" to "cross-site"
            )
            
            val response = app.get(source2Url, headers = headers, interceptor = interceptor)
            val html = response.text
            
            // "file":"..." pattern'inden M3U8 URL çıkar - Python'daki tüm pattern'ler
            val patterns = listOf(
                Regex("""\"file\":\"([^\"]+)\""""),  // İlk pattern
                Regex("""\"file\":\"((?:\\\\\"|[^\"]+))\""""),  // Escaped quotes için
                Regex("""\"file\"\s*:\s*\"([^\"]+)\""""),  // Whitespace ile
                Regex("""file["']?\s*:\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)  // Alternatif quotes
            )
            
            for (pattern in patterns) {
                val match = pattern.find(html)
                if (match != null) {
                    var m3u8Url = match.groupValues[1]
                    // Python'daki gibi escape karakterlerini temizle
                    m3u8Url = m3u8Url.replace("\\/", "/")
                        .replace("\\u0026", "&")
                        .replace("\\\\\"", "\"")
                        .replace("\\\\", "")
                        .replace("\\", "")
                    
                    // M3U8 kontrolü
                    if (m3u8Url.contains(".m3u8", ignoreCase = true)) {
                        return m3u8Url
                    }
                }
            }
            
            // Direkt M3U8 URL regex (Python'daki son pattern)
            val m3u8Match = Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""", RegexOption.IGNORE_CASE).find(html)
            return m3u8Match?.groupValues?.get(0)
        } catch (e: Exception) {
            // Hata durumunda null dön
        }
        return null
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        // ADIM -1: Şifreli veriyi çıkar
        val encryptedData = extractEncryptedDataFromUrl(data)
        if (encryptedData == null) {
            // Fallback: Eski yöntemle iframe ara
            return loadLinksFallback(data, subtitleCallback, callback)
        }

        // ADIM 0: iframe URL'ini decrypt et
        val iframeUrl = decryptIframeUrl(encryptedData)
        if (iframeUrl == null) {
            return loadLinksFallback(data, subtitleCallback, callback)
        }

        // ÖNEMLİ: iframe URL'ini önce direkt extractor'a gönder (hemen çalışsın)
        // Extractors daha iyi sonuç verebilir veya alternatif formatlar bulabilir
        loadExtractor(iframeUrl, "${mainUrl}/", subtitleCallback, callback)

        // ADIM 1: Player URL'i çıkar
        val playerUrl = getPlayerUrl(iframeUrl)
        
        // ADIM 2: M3U8 URL'i çıkar (direkt stream URL)
        if (playerUrl != null) {
            val m3u8Url = getM3u8Url(playerUrl, iframeUrl)
            if (m3u8Url != null && m3u8Url.isNotEmpty()) {
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = m3u8Url,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.headers = mapOf("Referer" to iframeUrl)
                        this.quality = Qualities.Unknown.value
                    }
                )
            }
        }
        
        // Her durumda true dön - extractor'lar en azından bir şey bulmalı
        return true
    }

    // Fallback: Eski iframe bulma yöntemi
    private suspend fun loadLinksFallback(data: String, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        try {
            val document = app.get(data, referer = data, interceptor = interceptor).document
            val iframes = mutableListOf<String>()

            // Tüm iframe'leri bul - önce data-src, sonra src
            document.select("iframe").forEach { iframe ->
                val src = iframe.attr("data-src").takeIf { it.isNotEmpty() } 
                    ?: iframe.attr("src").takeIf { it.isNotEmpty() }
                src?.let { 
                    fixUrlNull(it)?.let { 
                        if (it.isNotEmpty() && !iframes.contains(it)) {
                            iframes.add(it)
                        }
                    }
                }
            }

            // Script içinde iframe URL'leri ara
            if (iframes.isEmpty()) {
                val htmlContent = document.html()
                val iframePatterns = listOf(
                    Regex("""iframe\s+src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                    Regex("""iframe\s+data-src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                    Regex("""videoIframe\.src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE),
                    Regex("""src\s*[:=]\s*['"]([^'"]*player[^'"]*)['"]""", RegexOption.IGNORE_CASE),
                    Regex("""window\.location\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE)
                )
                
                iframePatterns.forEach { pattern ->
                    pattern.findAll(htmlContent).forEach { match ->
                        match.groupValues.getOrNull(1)?.let { url ->
                            fixUrlNull(url)?.let { 
                                if (it.isNotEmpty() && !iframes.contains(it) && 
                                    !it.contains("ads") && !it.contains("google") && 
                                    !it.contains("facebook") && !it.contains("doubleclick")) {
                                    iframes.add(it)
                                }
                            }
                        }
                    }
                }
            }

            // Bulunan iframe'leri loadExtractor'a gönder
            if (iframes.isNotEmpty()) {
                for (iframe in iframes) {
                    if (iframe.isNotEmpty()) {
                        loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
                    }
                }
                return true
            }

            // Eğer hiç iframe bulunamazsa, sayfanın kendisini extractor'a gönder (son çare)
            loadExtractor(data, "${mainUrl}/", subtitleCallback, callback)
            return true
        } catch (e: Exception) {
            // Hata durumunda da sayfayı extractor'a gönder
            loadExtractor(data, "${mainUrl}/", subtitleCallback, callback)
            return true
        }
    }
}