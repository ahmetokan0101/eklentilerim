package com.movix.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

// ========== Sobreatsesuyp Extractor ==========

/**
 * Sobreatsesuyp Video Extractor
 * Domain: sobreatsesuyp.com
 * JSON-based video data extraction
 */
open class Sobreatsesuyp : ExtractorApi() {
    override val name = "Sobreatsesuyp"
    override val mainUrl = "https://sobreatsesuyp.com"
    override val requiresReferer = true

    data class SobreatsesuypVideoData(
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("file") val file: String? = null
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            val response = app.get(url, referer = extRef)
            val doc = response.document
            val pageContent = response.text
            
            // JSON data pattern'ini ara
            val jsonPattern = Regex("""sources:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(pageContent)
            
            if (jsonMatch != null) {
                val jsonContent = "[${jsonMatch.groupValues[1]}]"
                try {
                    val videoDataList: List<SobreatsesuypVideoData> = parseJson(jsonContent)
                    
                    videoDataList.forEach { videoData ->
                        val videoUrl = videoData.file
                        if (videoUrl != null && videoUrl.isNotEmpty()) {
                            callback.invoke(
                                newExtractorLink(
                                    source = name,
                                    name = videoData.title ?: name,
                                    url = videoUrl,
                                    type = if (videoUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                                ) {
                                    this.referer = extRef
                                    this.quality = Qualities.Unknown.value
                                    this.headers = mapOf("Referer" to extRef)
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    // JSON parse hatası
                }
            }
            
            // Fallback: M3U8 arama
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf("Referer" to extRef)
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}


// ========== TRsTX Extractor ==========

/**
 * TRsTX Video Extractor
 * Domain: trstx.org
 * JSON-based video data extraction with quality detection
 */
open class TRsTX : ExtractorApi() {
    override val name = "TRsTX"
    override val mainUrl = "https://trstx.org"
    override val requiresReferer = true

    data class TrstxVideoData(
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("file") val file: String? = null
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            val response = app.get(url, referer = extRef)
            val doc = response.document
            val pageContent = response.text
            
            // JSON data pattern'ini ara
            val jsonPattern = Regex("""sources:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(pageContent)
            
            if (jsonMatch != null) {
                val jsonContent = "[${jsonMatch.groupValues[1]}]"
                try {
                    val videoDataList: List<TrstxVideoData> = parseJson(jsonContent)
                    
                    videoDataList.forEach { videoData ->
                        val videoUrl = videoData.file
                        val title = videoData.title
                        
                        if (videoUrl != null && videoUrl.isNotEmpty()) {
                            // Quality detection from title
                            val quality = when {
                                title?.contains("1080", ignoreCase = true) == true -> Qualities.P1080.value
                                title?.contains("720", ignoreCase = true) == true -> Qualities.P720.value
                                title?.contains("480", ignoreCase = true) == true -> Qualities.P480.value
                                title?.contains("360", ignoreCase = true) == true -> Qualities.P360.value
                                else -> Qualities.Unknown.value
                            }
                            
                            callback.invoke(
                                newExtractorLink(
                                    source = name,
                                    name = title ?: name,
                                    url = videoUrl,
                                    type = if (videoUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                                ) {
                                    this.referer = extRef
                                    this.quality = quality
                                    this.headers = mapOf("Referer" to extRef)
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    // JSON parse hatası
                }
            }
            
            // Fallback: M3U8 arama
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf("Referer" to extRef)
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}


// ========== TurboImgz Extractor ==========

/**
 * TurboImgz Video Extractor
 * Domain: turbo.imgz.me
 * Simple M3U8 extraction
 */
open class TurboImgz : ExtractorApi() {
    override val name = "TurboImgz"
    override val mainUrl = "https://turbo.imgz.me"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            val response = app.get(url, referer = extRef)
            val doc = response.document
            val pageContent = response.text
            
            // M3U8 linklerini bul
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf("Referer" to extRef)
                    }
                )
            }
            
            // Video source'ları bul
            val videoSources = doc.select("source[src]").mapNotNull {
                it.attr("src").takeIf { src -> src.isNotEmpty() }
            }
            
            videoSources.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf("Referer" to extRef)
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}


// ========== TurkeyPlayer Extractor ==========

/**
 * TurkeyPlayer Video Extractor
 * Domain: watch.turkeyplayer.com
 * M3U8 extraction with subtitle support
 */
open class TurkeyPlayer : ExtractorApi() {
    override val name = "TurkeyPlayer"
    override val mainUrl = "https://watch.turkeyplayer.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            val response = app.get(url, referer = extRef)
            val doc = response.document
            val pageContent = response.text
            
            // M3U8 linklerini bul
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
                        )
                    }
                )
            }
            
            // Video source'ları bul
            val videoSources = doc.select("source[src]").mapNotNull {
                it.attr("src").takeIf { src -> src.isNotEmpty() }
            }
            
            videoSources.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link
                    ) {
                        this.referer = extRef
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
                        )
                    }
                )
            }
            
            // Subtitle'ları bul
            try {
                val trackPattern = Regex("""<track[^>]+kind="captions"[^>]+src="([^"]+)"[^>]+label="([^"]+)"""")
                val trackMatches = trackPattern.findAll(pageContent)
                
                trackMatches.forEach { match ->
                    val subUrl = match.groupValues[1]
                    val label = match.groupValues[2]
                    
                    if (subUrl.isNotEmpty() && label.isNotEmpty()) {
                        subtitleCallback.invoke(
                            SubtitleFile(
                                lang = label,
                                url = subUrl
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Subtitle hatası önemli değil
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}


// ========== VidMoly Extractors ==========

/**
 * VidMoly Video Extractor
 * Domain: vidmoly.net
 * Complex extraction with packed JS
 * KRİTİK: Referer SABİT "https://vidmoly.to/" olmalı (dinamik değil!)
 * KRİTİK: User-Agent MOBİL Android olmalı
 */
open class VidMolyExtractor : ExtractorApi() {
    override val name = "VidMoly"
    override val mainUrl = "https://vidmoly.net"
    override val requiresReferer = true

    data class VideoData(
        @JsonProperty("video_location") val video_location: String
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        // KRİTİK: Referer her zaman "https://vidmoly.to/" olmalı
        val fixedReferer = "https://vidmoly.to/"
        
        // KRİTİK: User-Agent MOBİL Android olmalı
        val mobileUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
        
        try {
            val response = app.get(url, referer = referer ?: url)
            val doc = response.document
            val pageContent = response.text
            
            // video_location JSON pattern'ini ara
            val videoLocationPattern = Regex("""video_location["\s:]+["']([^"']+)["']""")
            val videoLocationMatch = videoLocationPattern.find(pageContent)
            
            if (videoLocationMatch != null) {
                val videoUrl = videoLocationMatch.groupValues[1]
                if (videoUrl.isNotEmpty()) {
                    callback.invoke(
                        newExtractorLink(
                            source = name,
                            name = name,
                            url = videoUrl,
                            type = if (videoUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                        ) {
                            this.referer = fixedReferer
                            this.quality = Qualities.Unknown.value
                            this.headers = mapOf(
                                "User-Agent" to mobileUserAgent
                            )
                        }
                    )
                }
            }
            
            // Fallback: M3U8 arama
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = fixedReferer
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf(
                            "User-Agent" to mobileUserAgent
                        )
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}

/**
 * VidMolyTo - VidMoly varyantı
 * Domain: vidmoly.to
 */
class VidMolyTo : VidMolyExtractor() {
    override val name = "VidMolyTo"
    override val mainUrl = "https://vidmoly.to"
}


// ========== VidMoxy Extractor ==========

/**
 * VidMoxy Video Extractor
 * Domain: vidmoxy.com
 * Hex escape decoding support
 */
open class VidMoxy : ExtractorApi() {
    override val name = "VidMoxy"
    override val mainUrl = "https://vidmoxy.com"
    override val requiresReferer = true

    /**
     * Decode hex escapes like \x48\x65\x6c\x6c\x6f
     */
    fun decodeHexEscapes(input: String): String {
        val hexPattern = Regex("""\\x([0-9A-Fa-f]{2})""")
        return hexPattern.replace(input) { matchResult ->
            val hexValue = matchResult.groupValues[1]
            val charCode = hexValue.toInt(16)
            charCode.toChar().toString()
        }
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val response = app.get(url, referer = referer ?: url)
            val doc = response.document
            val pageContent = response.text
            
            // Hex encoded data'yı bul ve decode et
            val hexPattern = Regex("""["']([^"']*\\x[0-9A-Fa-f]{2}[^"']*)["']""")
            val hexMatches = hexPattern.findAll(pageContent)
            
            hexMatches.forEach { match ->
                val encoded = match.groupValues[1]
                try {
                    val decoded = decodeHexEscapes(encoded)
                    
                    // Decoded içinde M3U8 ara
                    val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
                    val m3u8Links = m3u8Pattern.findAll(decoded).map { it.value }.toList()
                    
                    m3u8Links.forEach { link ->
                        callback.invoke(
                            newExtractorLink(
                                source = name,
                                name = name,
                                url = link,
                                type = ExtractorLinkType.M3U8
                            ) {
                                this.referer = url
                                this.quality = Qualities.Unknown.value
                                this.headers = mapOf(
                                    "Origin" to mainUrl,
                                    "Referer" to url
                                )
                            }
                        )
                    }
                } catch (e: Exception) {
                    // Decode hatası
                }
            }
            
            // Fallback: Normal M3U8 arama
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            m3u8Links.forEach { link ->
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = url
                        this.quality = Qualities.Unknown.value
                        this.headers = mapOf(
                            "Origin" to mainUrl,
                            "Referer" to url
                        )
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}
