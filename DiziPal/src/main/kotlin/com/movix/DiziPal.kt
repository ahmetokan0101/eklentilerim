package com.movix

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
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
    override var mainUrl              = "https://dizipal1521.com/"
    override var name                 = "Dizipal"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries, TvType.Live)

    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L
    override var sequentialMainPageScrollDelay = 150L
    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

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
            
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())
            
            if (doc.html().contains("Just a moment")) {
                return cloudflareKiller.intercept(chain)
            }
            
            return response
        }
    }

    // M3U Playlist URL (NeonSpor'dan)
    private val m3uPlaylistUrl = "https://raw.githubusercontent.com/primatzeka/kurbaga/main/NeonSpor/NeonSpor.m3u"
    
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
        "kategori:biyografi"        to "Biyografi",
        // === CANLI TV KATEGORİLERİ (M3U) ===
        "m3u:all"                   to "📺 Tüm Canlı Kanallar"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data.trimEnd('/')
        
        val isHomePage = request.name == "Öne Çıkanlar"
        val isKategoriPage = url.startsWith("kategori:")
        val isM3uPage = url.startsWith("m3u:")
        
        // === M3U CANLI TV KATEGORİLERİ ===
        if (isM3uPage) {
            return getM3uMainPage(url, request.name, page)
        }
        
        if (isHomePage && page > 1) {
            return newHomePageResponse(request.name, emptyList())
        }
        
        if (isHomePage) {
            val document = app.get(mainUrl).document
        
            val trends = document.select("ul.trends li").mapNotNull { it.toTrendResult() }
            val finalTrends = if (trends.isNotEmpty()) {
                trends.distinctBy { it.url }
            } else {
                val altTrends = document.select("ul.trends > li").mapNotNull { it.toTrendResult() }
                if (altTrends.isNotEmpty()) {
                    altTrends.distinctBy { it.url }
                } else {
                    document.select("article.movie-type-genres ul.trends li").mapNotNull { it.toTrendResult() }
                    .distinctBy { it.url }
            }
            }
            
            return newHomePageResponse(request.name, finalTrends)
        }
        
        if (isKategoriPage) {
            val kategoriKey = url.substringAfter("kategori:")
            val kategoriId = kategoriIdMap[kategoriKey]
            
            if (kategoriId != null) {
                val results = getCategoryContent(kategoriId, page)
                return newHomePageResponse(request.name, results, results.isNotEmpty())
            }
        }
        
        val document = app.get(url).document
        val home = document.select("div.items article").mapNotNull { it.toMainPageResult() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, home)
    }

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
                
                if (jsonResponse == null) {
                    val responseText = response.text
                    
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
        }
        
        return emptyList()
    }

    private fun parseCategoryHtml(htmlContent: String): List<SearchResponse> {
        val document = Jsoup.parse(htmlContent)
        
        val allDivs = document.select("div")
        val kartlar = allDivs.filter { div ->
            val classAttr = div.attr("class")
            classAttr.contains("bg-") && div.selectFirst("a[href]") != null
        }
        
        val cardsToProcess = if (kartlar.size > 2) {
            kartlar.drop(2)
        } else {
            kartlar
        }
        
        return cardsToProcess.mapNotNull { kart ->
            try {
                val linkElem = kart.selectFirst("a[href]") ?: return@mapNotNull null
                val href = linkElem.attr("href") ?: return@mapNotNull null
                
                val fixedHref = if (href.contains("/bolum/")) {
                    val fixed = fixUrlNull(href)?.replace("/bolum/", "/series/")
                    fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return@mapNotNull null
                } else {
                    fixUrlNull(href) ?: return@mapNotNull null
                }
                
                val titleAttr = linkElem.attr("title")
                
                val baslikElem = kart.selectFirst("h2")
                var title = baslikElem?.text()?.trim() ?: ""
                if (title.isEmpty() && titleAttr.isNotEmpty()) {
                    title = titleAttr.replace(" izle", "").trim()
                }
                
                if (title.isEmpty()) return@mapNotNull null
                
                val imgElem = kart.selectFirst("img")
                val posterUrl = imgElem?.let {
                    fixUrlNull(it.attr("data-src")) ?: fixUrlNull(it.attr("src"))
                }
                
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

    private data class CategoryApiResponse(
        @JsonProperty("state") val state: Boolean? = null,
        @JsonProperty("data") val data: CategoryApiData? = null
    )

    private data class CategoryApiData(
        @JsonProperty("html") val html: String? = null
    )

    // ========== M3U CANLI TV FONKSİYONLARI ==========
    
    /**
     * M3U kategorilerini işler ve canlı TV kanallarını döndürür
     */
    private suspend fun getM3uMainPage(url: String, categoryName: String, page: Int): HomePageResponse {
        try {
            // M3U içeriğini çek
            val m3uContent = app.get(m3uPlaylistUrl).text
            val parser = IptvPlaylistParser()
            val playlist = parser.parseM3U(m3uContent)
            
            val kategoriKey = url.substringAfter("m3u:")
            
            // Tüm kanallar
            if (kategoriKey == "all") {
                // Gruplara göre ayır
                val grouped = playlist.items.groupBy { it.attributes["group-title"] }
                
                return newHomePageResponse(
                    grouped.map { (groupName, channels) ->
                        val title = groupName ?: "Diğer"
                        val items = channels.mapNotNull { kanal ->
                            kanal.toM3uSearchResponse()
                        }
                        HomePageList(title, items, isHorizontalImages = true)
                    },
                    hasNext = false
                )
            }
            
            // Belirli bir grup
            val filteredChannels = playlist.items.filter { 
                it.attributes["group-title"] == kategoriKey 
            }.mapNotNull { it.toM3uSearchResponse() }
            
            return newHomePageResponse(categoryName, filteredChannels, hasNext = false)
            
        } catch (e: Exception) {
            return newHomePageResponse(categoryName, emptyList(), hasNext = false)
        }
    }
    
    /**
     * PlaylistItem'ı SearchResponse'a çevirir
     */
    private fun PlaylistItem.toM3uSearchResponse(): LiveSearchResponse? {
        val streamUrl = this.url ?: return null
        val channelName = this.title ?: return null
        val posterUrl = this.attributes["tvg-logo"]
        val group = this.attributes["group-title"] ?: ""
        val nation = this.attributes["tvg-country"] ?: ""
        
        // LoadData'yı JSON olarak encode et
        val loadData = M3uLoadData(streamUrl, channelName, posterUrl, group, nation, this.headers, this.userAgent)
        
        return newLiveSearchResponse(
            channelName,
            loadData.toJson(),
            TvType.Live
        ) {
            this.posterUrl = posterUrl
            this.lang = nation
        }
    }
    
    /**
     * M3U kanalı için load response oluşturur
     */
    private suspend fun loadM3uChannel(data: String): LoadResponse? {
        try {
            val loadData = parseJson<M3uLoadData>(data)
            
            val nation = if (loadData.group == "NSFW") {
                "⚠️🔞🔞🔞 » ${loadData.group} | ${loadData.nation} « 🔞🔞🔞⚠️"
            } else {
                "» ${loadData.group} | ${loadData.nation} «"
            }
            
            // Aynı gruptaki kanalları recommendations olarak ekle
            val m3uContent = app.get(m3uPlaylistUrl).text
            val parser = IptvPlaylistParser()
            val playlist = parser.parseM3U(m3uContent)
            
            val recommendations = playlist.items
                .filter { it.attributes["group-title"] == loadData.group && it.title != loadData.title }
                .take(10)
                .mapNotNull { it.toM3uSearchResponse() }
            
            return newLiveStreamLoadResponse(loadData.title, loadData.url, data) {
                this.posterUrl = loadData.poster
                this.plot = nation
                this.tags = listOf(loadData.group, loadData.nation)
                this.recommendations = recommendations
            }
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * M3U stream linklerini yükler
     */
    private suspend fun loadM3uLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        try {
            val loadData = parseJson<M3uLoadData>(data)
            
            callback.invoke(
                ExtractorLink(
                    source = name,
                    name = "$name - ${loadData.title}",
                    url = loadData.url,
                    referer = loadData.headers["referrer"] ?: "",
                    quality = Qualities.Unknown.value,
                    type = ExtractorLinkType.M3U8,
                    headers = loadData.headers
                )
            )
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * M3U kanal verisi için data class
     */
    data class M3uLoadData(
        val url: String,
        val title: String,
        val poster: String?,
        val group: String,
        val nation: String,
        val headers: Map<String, String> = emptyMap(),
        val userAgent: String? = null
    )
    private fun Element.toTrendResult(): SearchResponse? {
        val aTag = this.selectFirst("a") ?: return null
        var rawHref = aTag.attr("href")
        
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
        
        val slug = href.substringAfterLast("/").trim()
        if (slug.isEmpty()) return null
        
        val title = slug.split("-")
            .joinToString(" ") { word -> 
                word.replaceFirstChar { char -> char.uppercaseChar() }
            }
        
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
        
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))
        
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchEndpoint = "/bg/searchcontent"
        val cKey = "ca1d4a53d0f4761a949b85e51e18f096"
        val cValue = "MTc2NDgwNDAwMGFiNmI2ZDEzNDg1ZmE4MjQyZmU2YzRhNzc0OTE2NTM3NjQyMTU5MjljMWQzMzliNzY5NzFlZmViMzRhMGVmNjgwODU3MGIyZA=="
        
        if (query.length < 3) {
            return emptyList()
        }
        
        try {
            val formData = mapOf(
                "searchterm" to query,
                "cKey" to cKey,
                "cValue" to cValue
            )
            
            val headers = mapOf(
                "Accept" to "application/json, text/javascript, */*; q=0.01",
                "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "Referer" to mainUrl,
                "Origin" to mainUrl.replace(Regex("/$"), ""),
                "X-Requested-With" to "XMLHttpRequest"
            )
            
            val response = app.post(
                url = "${mainUrl.replace(Regex("/$"), "")}$searchEndpoint",
                headers = headers,
                data = formData,
                interceptor = interceptor
            )
            
            val jsonResponse = response.parsedSafe<SearchApiResponse>()
            
            if (jsonResponse?.data?.state != true) {
                return emptyList()
            }
            
            val jsonResults = jsonResponse.data?.result
            if (!jsonResults.isNullOrEmpty()) {
                return jsonResults.mapNotNull { result ->
                    val title = result.object_name ?: return@mapNotNull null
                    val slug = result.used_slug ?: return@mapNotNull null
                    val href = "${mainUrl.replace(Regex("/$"), "")}/$slug"
                    val posterUrl = result.object_poster_url?.let { fixUrlNull(it) }
                    
                    val isTvSeries = determineTvType(href, title)
                    val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie
                    
                    if (tvType == TvType.TvSeries) {
                        newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                            this.posterUrl = posterUrl
                        }
                    } else {
                        newMovieSearchResponse(title, href, TvType.Movie) {
                            this.posterUrl = posterUrl
                        }
                    }
                }
            }
            
            val htmlContent = jsonResponse.data?.html ?: return emptyList()
            
            val document = Jsoup.parse(htmlContent)
            
            var items = document.select("a[class*='item'], a[class*='result'], a[class*='card'], " +
                    "li[class*='item'], li[class*='result'], li[class*='card'], " +
                    "div[class*='item'], div[class*='result'], div[class*='card'], " +
                    "div.result-item article, a[href*='/bolum/'], a[href*='/series/'], a[href*='/film/']")
            
            if (items.isEmpty()) {
                items = document.select("a[href]")
            }
            
            return items.mapNotNull { element ->
                val linkElement = if (element.tagName() == "a") {
                    element
                } else {
                    element.selectFirst("a[href]")
                } ?: return@mapNotNull null
                
                val rawHref = linkElement.attr("href") ?: return@mapNotNull null
                val href = if (rawHref.startsWith("http")) {
                    rawHref
                } else {
                    "${mainUrl.replace(Regex("/$"), "")}$rawHref"
                }
                
                val title = linkElement.text()?.takeIf { it.isNotBlank() }
                    ?: element.selectFirst("img")?.attr("alt")
                    ?: element.text()?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                
                val imgElement = element.selectFirst("img")
                val posterUrl = when {
                    imgElement == null -> null
                    else -> {
                        val srcset = imgElement.attr("data-srcset")
                        when {
                            srcset.isNotBlank() -> {
                                val srcsetParts = srcset.split(",")
                                val url2x = srcsetParts.find { it.contains("2x") }
                                if (url2x != null) {
                                    url2x.trim().split(" ").firstOrNull()?.let { fixUrlNull(it) }
                                } else {
                                    srcsetParts.firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.let { fixUrlNull(it) }
                                }
                            }
                            imgElement.attr("data-src").isNotBlank() -> {
                                fixUrlNull(imgElement.attr("data-src"))
                            }
                            else -> {
                                val src = imgElement.attr("src")
                                if (src.isNotBlank() && !src.startsWith("data:image")) {
                                    fixUrlNull(src)
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }
                
                val isTvSeries = determineTvType(href, title)
                val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie
                
                val fixedHref = if (href.contains("/bolum/")) {
                    href.replace("/bolum/", "/series/").replace(Regex("-[0-9]+x.*$"), "")
                } else {
                    href
                }
                
                if (tvType == TvType.TvSeries) {
                    newTvSeriesSearchResponse(title, fixedHref, TvType.TvSeries) { 
                        this.posterUrl = posterUrl 
                    }
                } else {
                    newMovieSearchResponse(title, fixedHref, TvType.Movie) { 
                        this.posterUrl = posterUrl 
                    }
                }
            }.distinctBy { it.url }
        } catch (e: Exception) {
            return try {
                val document = app.get("${mainUrl}/?s=${query}", interceptor = interceptor).document
                document.select("div.result-item article").mapNotNull { it.toSearchResult() }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }
    
    private data class SearchApiResponse(
        @JsonProperty("data") val data: SearchData?,
        @JsonProperty("message") val message: String?
    )
    
    private data class SearchData(
        @JsonProperty("state") val state: Boolean?,
        @JsonProperty("html") val html: String?,
        @JsonProperty("result") val result: List<SearchResultItem>?
    )
    
    private data class SearchResultItem(
        @JsonProperty("object_name") val object_name: String?,
        @JsonProperty("used_slug") val used_slug: String?,
        @JsonProperty("object_poster_url") val object_poster_url: String?,
        @JsonProperty("object_back_url") val object_back_url: String?,
        @JsonProperty("object_release_year") val object_release_year: String?,
        @JsonProperty("object_language") val object_language: String?,
        @JsonProperty("object_categories") val object_categories: String?,
        @JsonProperty("object_related_imdb_point") val object_related_imdb_point: String?
    )

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.title a")?.text() ?: return null
        val rawHref   = this.selectFirst("div.title a")?.attr("href") ?: return null
        
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    private fun getSidebarValue(document: org.jsoup.nodes.Document, key: String, exactMatch: Boolean = true): String? {
        val sidebarItems = document.select("ul > li").filter {
            it.selectFirst(".key") != null && it.selectFirst(".value") != null
        }
        return sidebarItems.firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            if (exactMatch) {
                keyText == key
            } else {
                keyText.contains(key, ignoreCase = true)
            }
        }?.selectFirst(".value a, .value")?.text()?.trim()
    }
    
    private fun String.toRatingDouble(): Double? {
        return this.replace(",", ".").toDoubleOrNull()
    }

    override suspend fun load(url: String): LoadResponse? {
        // M3U kanalı mı kontrol et
        if (url.startsWith("{")) {
            return try {
                val loadData = parseJson<M3uLoadData>(url)
                loadM3uChannel(url)
            } catch (e: Exception) {
                null
            }
        }
        
        val document = app.get(url).document

        val title = document.selectFirst("h1")?.text()?.trim() ?: return null
        
        val poster = fixUrlNull(
            document.selectFirst(".page-top img")?.attr("src") ?:
            document.selectFirst(".page-top img.lazyload")?.attr("data-src") ?:
            document.selectFirst("div.poster img")?.attr("src")
        )
        
        val backdrop = fixUrlNull(
            document.selectFirst("img[data-fake]")?.attr("data-src")?.takeIf { it.isNotEmpty() }
                ?: document.selectFirst("img[data-fake]")?.attr("src")?.takeIf { it.isNotEmpty() }
        )
        
        val description = document.selectFirst("p.summary-paragraph")?.text()?.trim()
            ?: document.selectFirst("div.w-full p.text-white.text-base")?.text()?.trim()
            ?: document.selectFirst("div.w-full p.mt-4")?.text()?.trim()
            ?: document.selectFirst("div.wp-content p")?.text()?.trim()
        
        val sidebarItems = document.select("ul > li").filter {
            it.selectFirst(".key") != null && it.selectFirst(".value") != null
        }
        
        val year = sidebarItems.firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            keyText.contains("Yıl", ignoreCase = true) && !keyText.contains("Gösterim")
        }?.selectFirst(".value a, .value")?.text()?.trim()?.toIntOrNull()
            ?: sidebarItems.firstOrNull {
                val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
                keyText.contains("Gösterim", ignoreCase = true)
            }?.selectFirst(".value")?.text()?.trim()?.toIntOrNull()
            ?: document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
        
        val tags = (sidebarItems.firstOrNull {
            it.selectFirst(".key")?.text()?.trim() == "Kategoriler"
        }?.select(".value a")?.map { it.text().trim() } ?: emptyList<String>())
            .ifEmpty { document.select("div.sgeneros a").map { it.text().trim() } }
        
        val ratingString = sidebarItems.firstOrNull {
            val keyText = it.selectFirst(".key")?.text()?.trim() ?: ""
            keyText.contains("IMDB", ignoreCase = true) || keyText.contains("Puan", ignoreCase = true)
        }?.selectFirst(".value")?.text()?.trim()
            ?: document.selectFirst(".comment-like-avg .vote")?.text()?.trim()
            ?: document.selectFirst("span.dt_rating_vgs")?.text()?.trim()
        
        val rating = ratingString?.toRatingDouble()?.let { Score.from10(it) }
        
        val durationText = sidebarItems.firstOrNull {
            it.selectFirst(".key")?.text()?.trim() == "Süre"
        }?.selectFirst(".value")?.text()?.trim() ?: ""
        val duration = Regex("""(\d+)""").find(durationText)?.groupValues?.get(1)?.toIntOrNull()
            ?: document.selectFirst("span.runtime")?.text()?.split(" ")?.first()?.trim()?.toIntOrNull()
        
        val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
        
        val actors = document.select(".movie-actors a[href*='/oyuncu/']").mapNotNull {
            val name = it.selectFirst(".actor-item .name")?.text()?.trim()
                ?: it.selectFirst(".name")?.text()?.trim()
                ?: it.attr("title").takeIf { it.isNotBlank() }
            
            if (name == null || name.isBlank()) return@mapNotNull null
            
            val imageUrl = it.selectFirst("img")?.let { img ->
                fixUrlNull(img.attr("data-src").takeIf { it.isNotBlank() }
                    ?: img.attr("src"))
            }
            
            Actor(name, imageUrl)
        }.ifEmpty {
            document.select(".movie-actors .actor-item .name").mapNotNull {
                val name = it.text().trim()
                if (name.isBlank()) return@mapNotNull null
                
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
                
                Actor(name)
            }
        }
        
        val trailer = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { 
            "https://www.youtube.com/embed/$it" 
        }
        
        val voteCount = document.selectFirst(".vote-users")?.text()?.trim()
        
        val director = try {
            val jsonLdScripts = document.select("script[type='application/ld+json']")
            jsonLdScripts.mapNotNull { script ->
                try {
                    val jsonText = script.data() ?: script.html()
                    if (jsonText.contains("\"director\"")) {
                        val directorMatch = Regex("""\"director\"[^}]*\"name\"\s*:\s*\"([^\"]+)\"""").find(jsonText)
                        directorMatch?.groupValues?.get(1)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }.firstOrNull()
        } catch (e: Exception) {
            null
        }
        
        val studio = try {
            val jsonLdScripts = document.select("script[type='application/ld+json']")
            jsonLdScripts.mapNotNull { script ->
                try {
                    val jsonText = script.data() ?: script.html()
                    if (jsonText.contains("\"productionCompany\"")) {
                        val studioMatch = Regex("""\"productionCompany\"[^}]*\"name\"\s*:\s*\"([^\"]+)\"""").find(jsonText)
                        studioMatch?.groupValues?.get(1)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }.firstOrNull()
        } catch (e: Exception) {
            null
        }

        val episodes = mutableListOf<Episode>()
        
        val allSeasons = document.select(".season-lists")
        
        if (allSeasons.isNotEmpty()) {
            allSeasons.forEach { seasonDiv ->
                val seasonFromClass = Regex("""szn(\d+)""").find(seasonDiv.classNames().joinToString(" "))?.groupValues?.get(1)?.toIntOrNull()
                
                seasonDiv.select(".grid > div").forEach { episodeDiv ->
                    val epLink = episodeDiv.selectFirst("a[data-dizipal-pageloader]") ?: return@forEach
                    val epHref = fixUrlNull(epLink.attr("href")) ?: return@forEach
                    
                    val epTitle = epLink.selectFirst("h2")?.text()?.trim() ?: title
                    
                    val epInfo = epLink.selectFirst("div[style*='font-size:13px']")?.text()?.trim() ?: ""
                    val epSeasonFromInfo = Regex("""(\d+)\.?\s*[Ss]ezon""").find(epInfo)?.groupValues?.get(1)?.toIntOrNull()
                    val epEpisodeFromInfo = Regex("""(\d+)\.?\s*[Bb]ölüm""").find(epInfo)?.groupValues?.get(1)?.toIntOrNull()
                    
                    val urlMatch = Regex("""-(\d+)x(\d+)(?:\.|$)""").find(epHref)
                    val epSeasonFromUrl = urlMatch?.groupValues?.get(1)?.toIntOrNull()
                    val epEpisodeFromUrl = urlMatch?.groupValues?.get(2)?.toIntOrNull()
                    
                    val epNumber = episodeDiv.selectFirst("span.text-white.opacity-60")?.text()?.trim()?.toIntOrNull()
                    
                    val epSeason = seasonFromClass ?: epSeasonFromInfo ?: epSeasonFromUrl ?: 1
                    val epEpisode = epEpisodeFromInfo ?: epEpisodeFromUrl ?: epNumber
                    
                    episodes.add(newEpisode(epHref) {
                        this.name = epTitle
                        this.season = epSeason
                        this.episode = epEpisode
                        this.posterUrl = poster
                    })
                }
            }
        } else {
            episodes.addAll(document.select("div.episodes article, div.bolumler article, div.seasons article").mapNotNull {
                val epName = it.selectFirst("a")?.text()?.trim() ?: return@mapNotNull null
                val epHref = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val epEpisode = Regex("""(\d+)\.?\s*[Bb]ölüm""").find(epName)?.groupValues?.get(1)?.toIntOrNull()
                val epSeason = Regex("""(\d+)\.?\s*[Ss]ezon""").find(epName)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            newEpisode(epHref) {
                    this.name = epName
                    this.season = epSeason
                this.episode = epEpisode
                    this.posterUrl = poster
                }
            })
        }
        
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
            val moviePoster = backdrop ?: poster
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = moviePoster
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
        
        var href = if (rawHref.contains("/bolum/")) {
            val fixed = fixUrlNull(rawHref)?.replace("/bolum/", "/series/")
            fixed?.replace(Regex("-[0-9]+x.*$"), "") ?: return null
        } else {
            fixUrlNull(rawHref) ?: return null
        }
        
        val posterUrl = fixUrlNull(this.selectFirst("a img")?.attr("data-src"))
        
        val isTvSeries = determineTvType(href, title)
        val tvType = if (isTvSeries) TvType.TvSeries else TvType.Movie

        return if (tvType == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }
    
    private fun determineTvType(url: String, title: String = ""): Boolean {
        if (url.contains("/movies/") || url.contains("/movie/")) {
            return false
        }
        
        if (url.contains("/series/")) {
            return true
        }
        
        if (url.contains("/dizi/")) {
            return true
        }
        
        if (url.contains("/bolum/")) {
            return true
        }
        
        if (title.contains("Sezon", ignoreCase = true) || 
            title.contains("Bölüm", ignoreCase = true) ||
            title.contains("Season", ignoreCase = true) ||
            title.contains("Episode", ignoreCase = true)) {
            return true
        }
        
        return false
    }

    private val PASSPHRASE = "3hPn4uCjTVtfYWcjIcoJQ4cL1WWk1qxXI39egLYOmNv6IblA7eKJz68uU3eLzux1biZLCms0quEjTYniGv5z1JcKbNIsDQFSeIZOBZJz4is6pD7UyWDggWWzTLBQbHcQFpBQdClnuQaMNUHtLHTpzCvZy33p6I7wFBvL4fnXBYH84aUIyWGTRvM2G5cfoNf4705tO2kv"
    private val SOURCE2_PATH = "/source2.php?v="

    private data class EncryptedData(
        val ciphertext: String,
        val iv: String,
        val salt: String
    )

    private suspend fun extractEncryptedDataFromUrl(url: String): EncryptedData? {
        return try {
            val document = app.get(url, interceptor = interceptor).document
            val html = document.html()
            
            var match = Regex("""data-rm-k=["']?true["']?[^>]*>([^<]+)""", RegexOption.IGNORE_CASE).find(html)
            if (match == null) {
                match = Regex("""data-rm-k=["']?true["']?[^>]*>\s*(\{[^}]+\})""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).find(html)
            }
            
            if (match != null) {
                var dataText = match.groupValues[1].trim()
                dataText = dataText.replace("&quot;", "\"").replace("&amp;", "&")
                
                try {
                    val jsonStart = dataText.indexOf('{')
                    val jsonEnd = dataText.lastIndexOf('}') + 1
                    if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        val jsonText = dataText.substring(jsonStart, jsonEnd)
                        val json = JSONObject(jsonText)
                        
                        val ciphertext = if (json.has("ciphertext")) json.getString("ciphertext") else ""
                        val iv = if (json.has("iv")) json.getString("iv") else ""
                        val salt = if (json.has("salt")) json.getString("salt") else ""
                        
                        if (ciphertext.isNotEmpty() && iv.isNotEmpty() && salt.isNotEmpty()) {
                            return EncryptedData(ciphertext, iv, salt)
                        }
                    } else {
                        val json = JSONObject(dataText)
                        val ciphertext = if (json.has("ciphertext")) json.getString("ciphertext") else ""
                        val iv = if (json.has("iv")) json.getString("iv") else ""
                        val salt = if (json.has("salt")) json.getString("salt") else ""
                        
                        if (ciphertext.isNotEmpty() && iv.isNotEmpty() && salt.isNotEmpty()) {
                            return EncryptedData(ciphertext, iv, salt)
                        }
                    }
                    } catch (e: Exception) {
                    }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun decryptIframeUrl(encryptedData: EncryptedData): String? {
        return try {
            val saltBytes = hexStringToByteArray(encryptedData.salt)
            val ivBytes = hexStringToByteArray(encryptedData.iv)
            val ciphertextBytes = Base64.decode(encryptedData.ciphertext, Base64.DEFAULT)
            
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val spec = PBEKeySpec(PASSPHRASE.toCharArray(), saltBytes, 999, 256)
            val tmpKey = factory.generateSecret(spec)
            val key = SecretKeySpec(tmpKey.encoded, "AES")
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivBytes))
            val decrypted = cipher.doFinal(ciphertextBytes)
            val iframeUrl = String(decrypted, Charsets.UTF_8).trim()
            
            when {
                iframeUrl.startsWith("//") -> "https:$iframeUrl"
                iframeUrl.startsWith("http") -> iframeUrl
                else -> "https://$iframeUrl"
            }
        } catch (e: Exception) {
            null
        }
    }

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

    private fun fixM3u8Url(url: String, iframeUrl: String): String {
        var fixed = url.trim()
        
        fixed = fixed
            .replace("\\/", "/")
            .replace("\\u0026", "&")
            .replace("\\u003d", "=")
            .replace("\\u003f", "?")
            .replace("\\\\\"", "\"")
            .replace("\\\\", "")
            .replace("\\", "")
            .replace("\"", "")
            .replace("'", "")
        
        when {
            fixed.startsWith("//") -> fixed = "https:$fixed"
            !fixed.startsWith("http") -> {
                if (fixed.startsWith("/")) {
                    try {
                        val iframeDomain = URL(iframeUrl).let { "${it.protocol}://${it.host}" }
                        fixed = "$iframeDomain$fixed"
                    } catch (e: Exception) {
                        val baseUrl = mainUrl.replace(Regex("/$"), "")
                        fixed = "$baseUrl$fixed"
                    }
                } else {
                    try {
                        val iframeBase = URL(iframeUrl).let { "${it.protocol}://${it.host}${it.path.substringBeforeLast("/")}" }
                        fixed = "$iframeBase/$fixed"
                    } catch (e: Exception) {
                        fixed = "https://$fixed"
                    }
                }
            }
        }
        
        if (!fixed.contains("://")) {
            fixed = "https://$fixed"
        }
        
        return fixed
    }

    private suspend fun getPlayerUrl(iframeUrl: String): String? {
        return try {
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
            
            var match = Regex("""window\.openPlayer\('([^']+)'""", RegexOption.IGNORE_CASE).find(html)
            if (match != null) {
                return match.groupValues[1]
            }
            
            match = Regex("""window\.openPlayer\(["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
            if (match != null) {
                return match.groupValues[1]
            }
            
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
            
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getM3u8Url(playerUrl: String, iframeUrl: String): String? {
        return try {
            val url = URL(iframeUrl)
            val mainDomain = "${url.protocol}://${url.host}"
            val source2Url = "$mainDomain$SOURCE2_PATH$playerUrl"
            
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
            
            val patterns = listOf(
                Regex("""\"file\":\"([^\"]+)\""""),
                Regex("""\"file\":\"((?:\\\\\"|[^\"]+))\""""),
                Regex("""\"file\"\s*:\s*\"([^\"]+)\""""),
                Regex("""file["']?\s*:\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            )
            
                for (pattern in patterns) {
                val match = pattern.find(html)
                if (match != null) {
                    var m3u8Url = match.groupValues[1]
                    m3u8Url = m3u8Url
                        .replace("\\/", "/")
                        .replace("\\u0026", "&")
                        .replace("\\u003d", "=")
                        .replace("\\u003f", "?")
                        .replace("\\\\\"", "\"")
                        .replace("\\\\", "")
                        .replace("\\", "")
                        .trim()
                    
                    if (m3u8Url.isNotEmpty() && m3u8Url.startsWith("http")) {
                        return m3u8Url
                    }
                }
            }
            
            val m3u8Match = Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""", RegexOption.IGNORE_CASE).find(html)
            m3u8Match?.groupValues?.get(0)?.trim()?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        // M3U kanalı mı kontrol et
        if (data.startsWith("{")) {
            return try {
                loadM3uLinks(data, callback)
            } catch (e: Exception) {
                false
            }
        }
        
        var foundAnyLink = false
        
        try {
            val encryptedData = extractEncryptedDataFromUrl(data)
            if (encryptedData == null) {
                return loadLinksFallback(data, subtitleCallback, callback)
            }

            val iframeUrl = decryptIframeUrl(encryptedData)
            if (iframeUrl == null) {
                return loadLinksFallback(data, subtitleCallback, callback)
            }

            try {
                val extractorResult = loadExtractor(iframeUrl, data, subtitleCallback) { link ->
                    callback(link)
                    foundAnyLink = true
                }
            } catch (e: Exception) {
            }

            val playerUrl = getPlayerUrl(iframeUrl)
            
            if (playerUrl != null) {
                val m3u8Url = getM3u8Url(playerUrl, iframeUrl)
                if (m3u8Url != null && m3u8Url.isNotEmpty()) {
                    val fixedM3u8Url = fixM3u8Url(m3u8Url, iframeUrl)
                    
                    if (fixedM3u8Url.isNotEmpty() && (fixedM3u8Url.startsWith("http://") || fixedM3u8Url.startsWith("https://"))) {
                        callback.invoke(
                            newExtractorLink(
                                source = name,
                                name = name,
                                url = fixedM3u8Url,
                                type = ExtractorLinkType.M3U8
                            ) {
                                this.referer = iframeUrl
                                this.headers = mapOf(
                                    "Referer" to iframeUrl,
                                    "User-Agent" to USER_AGENT,
                                    "Accept" to "*/*",
                                    "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7"
                                )
                                this.quality = Qualities.Unknown.value
                            }
                        )
                        foundAnyLink = true
                    }
                }
            }
            
            if (!foundAnyLink) {
                return loadLinksFallback(data, subtitleCallback, callback)
            }
            
            return foundAnyLink
        } catch (e: Exception) {
            return loadLinksFallback(data, subtitleCallback, callback)
        }
    }

    private suspend fun loadLinksFallback(data: String, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        try {
            val document = app.get(data, referer = data, interceptor = interceptor).document
            val iframes = mutableListOf<String>()

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

            if (iframes.isNotEmpty()) {
                var foundAny = false
                for (iframe in iframes) {
                    if (iframe.isNotEmpty()) {
                        try {
                            val result = loadExtractor(iframe, data, subtitleCallback) { link ->
                                callback(link)
                                foundAny = true
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                if (foundAny) return true
            }

            try {
                var foundAny = false
                val result = loadExtractor(data, data, subtitleCallback) { link ->
                    callback(link)
                    foundAny = true
                }
                return foundAny || result
            } catch (e: Exception) {
                return false
            }
        } catch (e: Exception) {
            try {
                var foundAny = false
                val result = loadExtractor(data, data, subtitleCallback) { link ->
                    callback(link)
                    foundAny = true
                }
                return foundAny || result
            } catch (e2: Exception) {
                return false
            }
        }
    }
}

// ========== M3U PLAYLIST PARSER SYSTEM ==========
// NeonSpor eklentisinden alınmıştır - M3U playlist desteği için

data class Playlist(
    val items: List<PlaylistItem> = emptyList()
)

data class PlaylistItem(
    val title: String?                  = null,
    val attributes: Map<String, String> = emptyMap(),
    val headers: Map<String, String>    = emptyMap(),
    val url: String?                    = null,
    val userAgent: String?              = null
)

class IptvPlaylistParser {

    fun parseM3U(content: String): Playlist {
        return parseM3U(content.byteInputStream())
    }

    @Throws(PlaylistParserException::class)
    fun parseM3U(input: java.io.InputStream): Playlist {
        val reader = input.bufferedReader()

        if (!reader.readLine().isExtendedM3u()) {
            throw PlaylistParserException.InvalidHeader()
        }

        val playlistItems: MutableList<PlaylistItem> = mutableListOf()
        var currentIndex = 0

        var line: String? = reader.readLine()

        while (line != null) {
            if (line.isNotEmpty()) {
                if (line.startsWith(EXT_INF)) {
                    val title      = line.getTitle()
                    val attributes = line.getAttributes()

                    playlistItems.add(PlaylistItem(title, attributes))
                } else if (line.startsWith(EXT_VLC_OPT)) {
                    val item      = playlistItems[currentIndex]
                    val userAgent = item.userAgent ?: line.getTagValue("http-user-agent")
                    val referrer  = line.getTagValue("http-referrer")

                    val headers = mutableMapOf<String, String>()

                    if (userAgent != null) {
                        headers["user-agent"] = userAgent
                    }

                    if (referrer != null) {
                        headers["referrer"] = referrer
                    }

                    playlistItems[currentIndex] = item.copy(
                        userAgent = userAgent,
                        headers   = headers
                    )
                } else {
                    if (!line.startsWith("#")) {
                        val item       = playlistItems[currentIndex]
                        val url        = line.getUrl()
                        val userAgent  = line.getUrlParameter("user-agent")
                        val referrer   = line.getUrlParameter("referer")
                        val urlHeaders = if (referrer != null) {item.headers + mapOf("referrer" to referrer)} else item.headers

                        playlistItems[currentIndex] = item.copy(
                            url       = url,
                            headers   = item.headers + urlHeaders,
                            userAgent = userAgent ?: item.userAgent
                        )
                        currentIndex++
                    }
                }
            }

            line = reader.readLine()
        }
        return Playlist(playlistItems)
    }

    /** Replace "" (quotes) from given string. */
    private fun String.replaceQuotesAndTrim(): String {
        return replace("\"", "").trim()
    }

    /** Check if given content is valid M3U8 playlist. */
    private fun String.isExtendedM3u(): Boolean = startsWith(EXT_M3U)

    /**
     * Get title of media.
     *
     * Example:-
     *
     * Input:
     * ```
     * #EXTINF:-1 tvg-id="1234" group-title="Kids" tvg-logo="url/to/logo", Title
     * ```
     *
     * Result: Title
     */
    private fun String.getTitle(): String? {
        return split(",").lastOrNull()?.replaceQuotesAndTrim()
    }

    private fun String.getUrl(): String? {
        return split("|").firstOrNull()?.replaceQuotesAndTrim()
    }

    private fun String.getUrlParameter(key: String): String? {
        val urlRegex     = Regex("^(.*)\\|", RegexOption.IGNORE_CASE)
        val keyRegex     = Regex("$key=(\\w[^&]*)", RegexOption.IGNORE_CASE)
        val paramsString = replace(urlRegex, "").replaceQuotesAndTrim()

        return keyRegex.find(paramsString)?.groups?.get(1)?.value
    }

    private fun String.getAttributes(): Map<String, String> {
        val extInfRegex      = Regex("(#EXTINF:.?[0-9]+)", RegexOption.IGNORE_CASE)
        val attributesString = replace(extInfRegex, "").replaceQuotesAndTrim().split(",").first()

        return attributesString
            .split(Regex("\\s"))
            .mapNotNull {
                val pair = it.split("=")
                if (pair.size == 2) pair.first() to pair.last().replaceQuotesAndTrim() else null
            }
            .toMap()
    }

    private fun String.getTagValue(key: String): String? {
        val keyRegex = Regex("$key=(.*)", RegexOption.IGNORE_CASE)

        return keyRegex.find(this)?.groups?.get(1)?.value?.replaceQuotesAndTrim()
    }

    companion object {
        const val EXT_M3U     = "#EXTM3U"
        const val EXT_INF     = "#EXTINF"
        const val EXT_VLC_OPT = "#EXTVLCOPT"
    }
}

/** Exception thrown when an error occurs while parsing playlist. */
sealed class PlaylistParserException(message: String) : Exception(message) {

    /** Exception thrown if given file content is not valid. */
    class InvalidHeader : PlaylistParserException("Invalid file header. Header doesn't start with #EXTM3U")
}


// ========== M3U KULLANIM ÖRNEĞİ ==========
/**
 * M3U Playlist Parser Kullanım Örneği:
 * 
 * // 1. M3U içeriğini çek (URL'den veya string'den)
 * val m3uContent = app.get("https://example.com/playlist.m3u").text
 * 
 * // 2. Parse et
 * val parser = IptvPlaylistParser()
 * val playlist = parser.parseM3U(m3uContent)
 * 
 * // 3. Kanalları kullan
 * playlist.items.forEach { kanal ->
 *     val title = kanal.title                           // Kanal adı
 *     val url = kanal.url                               // Stream URL
 *     val logo = kanal.attributes["tvg-logo"]           // Logo URL
 *     val group = kanal.attributes["group-title"]       // Kategori
 *     val country = kanal.attributes["tvg-country"]     // Ülke
 *     val headers = kanal.headers                       // HTTP headers
 *     val userAgent = kanal.userAgent                   // User-Agent
 * }
 * 
 * // 4. Gruplama örneği
 * val grouped = playlist.items.groupBy { it.attributes["group-title"] }
 * grouped.forEach { (groupName, channels) ->
 *     println("Grup: $groupName - ${channels.size} kanal")
 * }
 * 
 * // 5. Arama örneği
 * val searchResults = playlist.items.filter { 
 *     it.title?.contains("Spor", ignoreCase = true) == true 
 * }
 * 
 * // 6. ExtractorLink oluşturma
 * callback.invoke(
 *     ExtractorLink(
 *         source = name,
 *         name = kanal.title ?: "Stream",
 *         url = kanal.url ?: "",
 *         referer = kanal.headers["referrer"] ?: "",
 *         quality = Qualities.Unknown.value,
 *         type = ExtractorLinkType.M3U8,
 *         headers = kanal.headers
 *     )
 * )
 */
