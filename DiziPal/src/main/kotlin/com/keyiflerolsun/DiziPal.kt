// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.Jsoup

class Dizipal : MainAPI() {
    override var mainUrl              = "https://dizipal1515.com/"
    override var name                 = "Dizipal"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

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

    override val mainPage = mainPageOf(
        "${mainUrl}"                to "Öne Çıkanlar",
        "kategori:aile"             to "Aile",
        "kategori:aksiyon"          to "Aksiyon",
        "kategori:animasyon"        to "Animasyon",
        "kategori:belgesel"         to "Belgesel",
        "kategori:komedi"           to "Komedi",
        "kategori:korku"            to "Korku",
        "kategori:dram"             to "Dram",
        "kategori:gerilim"          to "Gerilim",
        "kategori:bilim-kurgu"      to "Bilim Kurgu",
        "kategori:macera"           to "Macera"
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
                val responseText = response.text
                
                // Önce parsedSafe ile dene
                val jsonResponse = response.parsedSafe<CategoryApiResponse>()
                
                if (jsonResponse != null && (jsonResponse.state == true || jsonResponse.data != null)) {
                    val htmlContent = jsonResponse.data?.html
                    if (!htmlContent.isNullOrEmpty()) {
                        val decodedHtml = htmlContent
                            .replace("\\\"", "\"")
                            .replace("\\n", "\n")
                            .replace("\\/", "/")
                            .replace("\\r", "\r")
                            .replace("\\t", "\t")
                        
                        return parseCategoryHtml(decodedHtml)
                    }
                }
                
                // Eğer parsedSafe başarısız olduysa, manuel JSON parsing dene
                try {
                    // JSON içinde html field'ını ara
                    if (responseText.contains("\"html\"")) {
                        // Daha güvenilir bir şekilde HTML içeriğini çıkar
                        val htmlPattern = Regex("\"html\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"", RegexOption.DOT_MATCHES_ALL)
                        val match = htmlPattern.find(responseText)
                        
                        if (match != null) {
                            var htmlContent = match.groupValues[1]
                            
                            // Escape karakterlerini temizle
                            htmlContent = htmlContent
                                .replace("\\\"", "\"")
                                .replace("\\n", "\n")
                                .replace("\\/", "/")
                                .replace("\\r", "\r")
                                .replace("\\t", "\t")
                                .replace("\\\\", "\\")
                            
                            if (htmlContent.isNotEmpty()) {
                                return parseCategoryHtml(htmlContent)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silent fail
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
        
        // Python kodundaki gibi bg-[#22232a] class'ına sahip div'leri bul
        // Önce attribute selector ile dene
        var kartlar = document.select("div[class*='bg-']").toList()
        
        // Eğer bulunamazsa, tüm div'leri filtrele
        if (kartlar.isEmpty()) {
            val allDivs = document.select("div")
            kartlar = allDivs.filter { div ->
                val classAttr = div.attr("class")
                classAttr.contains("bg-") && div.selectFirst("a[href]") != null
            }
        } else {
            // Sadece a[href] içerenleri filtrele
            kartlar = kartlar.filter { it.selectFirst("a[href]") != null }
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
                
                // URL'yi düzelt
                val fixedHref = fixUrlNull(href) ?: return@mapNotNull null
                
                // /bolum/ URL'lerini /series/ olarak düzelt ve bölüm numarasını temizle
                val finalHref = if (fixedHref.contains("/bolum/")) {
                    val fixed = fixedHref.replace("/bolum/", "/series/")
                    fixed.replace(Regex("-[0-9]+x.*$"), "")
                } else {
                    fixedHref
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
                val isTvSeries = determineTvType(finalHref, title)
                
                if (isTvSeries) {
                    newTvSeriesSearchResponse(title, finalHref, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                } else {
                    newMovieSearchResponse(title, finalHref, TvType.Movie) {
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
        val document = app.get(data).document

        // TODO:
        // loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)

        return true
    }
}