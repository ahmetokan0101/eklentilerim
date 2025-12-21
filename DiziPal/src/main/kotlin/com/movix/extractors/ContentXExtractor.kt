package com.movix.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

/**
 * ContentX Video Extractor
 * Domain: contentx.me
 */
open class ContentX : ExtractorApi() {
    override val name = "ContentX"
    override val mainUrl = "https://contentx.me"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            // İlk sayfayı çek
            val response = app.get(url, referer = extRef)
            val doc = response.document
            
            // iframe source'ları bul
            val iframeSources = doc.select("iframe[src]").mapNotNull { 
                it.attr("src").takeIf { src -> src.isNotEmpty() }
            }
            
            // Video source'ları bul
            val videoSources = doc.select("source[src]").mapNotNull {
                it.attr("src").takeIf { src -> src.isNotEmpty() }
            }
            
            // M3U8 linklerini bul
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val pageContent = response.text
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            // Tüm bulunan linkleri işle
            val allLinks = (iframeSources + videoSources + m3u8Links).distinct()
            
            allLinks.forEach { link ->
                try {
                    when {
                        link.contains(".m3u8") -> {
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
                                        "Referer" to extRef,
                                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                    )
                                }
                            )
                        }
                        link.contains(".mp4") -> {
                            callback.invoke(
                                newExtractorLink(
                                    source = name,
                                    name = name,
                                    url = link
                                ) {
                                    this.referer = extRef
                                    this.quality = Qualities.Unknown.value
                                    this.headers = mapOf(
                                        "Referer" to extRef,
                                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                    )
                                }
                            )
                        }
                        else -> {
                            // iframe ise, loadExtractor ile işle
                            loadExtractor(link, extRef, subtitleCallback, callback)
                        }
                    }
                } catch (e: Exception) {
                    // Hata durumunda devam et
                }
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
}


// ========== ContentX Varyantları ==========

/**
 * Dplayer - ContentX varyantı
 * Domain: dplayer82.site
 */
class Dplayer : ContentX() {
    override val name = "Dplayer"
    override val mainUrl = "https://dplayer82.site"
}

/**
 * FourCX - ContentX varyantı
 * Domain: four.contentx.me
 */
class FourCX : ContentX() {
    override val name = "FourCX"
    override val mainUrl = "https://four.contentx.me"
}

/**
 * FourDplayer - ContentX varyantı
 * Domain: four.dplayer82.site
 */
class FourDplayer : ContentX() {
    override val name = "Dplayer82"
    override val mainUrl = "https://four.dplayer82.site"
}

// ========== DonilasPlay Extractor ==========

/**
 * DonilasPlay Video Extractor
 * Domain: donilasplay.com
 * Subtitle desteği var
 */
class DonilasPlay : ExtractorApi() {
    override val name = "DonilasPlay"
    override val mainUrl = "https://donilasplay.com"
    override val requiresReferer = true

    data class Track(
        @JsonProperty("file") val file: String?,
        @JsonProperty("label") val label: String?,
        @JsonProperty("kind") val kind: String?,
        @JsonProperty("language") val language: String?,
        @JsonProperty("default") val default: String?
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: url
        
        try {
            // Sayfayı çek
            val response = app.get(url, referer = extRef)
            val doc = response.document
            
            // M3U8 linklerini bul
            val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
            val pageContent = response.text
            val m3u8Links = m3u8Pattern.findAll(pageContent).map { it.value }.toList()
            
            // Video source'ları bul
            val videoSources = doc.select("source[src]").mapNotNull {
                it.attr("src").takeIf { src -> src.isNotEmpty() }
            }
            
            // Tüm linkleri işle
            val allLinks = (m3u8Links + videoSources).distinct()
            
            allLinks.forEach { link ->
                try {
                    if (link.contains(".m3u8")) {
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
                                    "Referer" to extRef,
                                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
                                )
                            }
                        )
                    }
                } catch (e: Exception) {
                    // Hata durumunda devam et
                }
            }
            
            // Subtitle'ları bul
            try {
                val trackPattern = Regex("""tracks:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
                val trackMatch = trackPattern.find(pageContent)
                
                if (trackMatch != null) {
                    val tracksJson = "[${trackMatch.groupValues[1]}]"
                    try {
                        val tracks: List<Track> = parseJson(tracksJson)
                        
                        tracks.forEach { track ->
                            val subUrl = track.file
                            val label = track.label
                            
                            if (subUrl != null && label != null) {
                                subtitleCallback.invoke(
                                    SubtitleFile(
                                        lang = label,
                                        url = subUrl
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // JSON parse hatası
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


// ========== Pichive Varyantları ==========

/**
 * Pichive - ContentX varyantı
 * Domain: pichive.online
 */
class Pichive : ContentX() {
    override val name = "Pichive"
    override val mainUrl = "https://pichive.online"
}

/**
 * FourPichive - ContentX varyantı
 * Domain: four.pichive.online
 */
class FourPichive : ContentX() {
    override val name = "FourPichive"
    override val mainUrl = "https://four.pichive.online"
}

/**
 * FourPichiveOnline - ContentX varyantı
 * Domain: four.pichive.online (duplicate)
 */
class FourPichiveOnline : ContentX() {
    override val name = "FourPichiveOnline"
    override val mainUrl = "https://four.pichive.online"
}

// ========== PlayRu Varyantları ==========

/**
 * PlayRu - ContentX varyantı
 * Domain: playru.net
 */
class PlayRu : ContentX() {
    override val name = "PlayRu"
    override val mainUrl = "https://playru.net"
}

/**
 * FourPlayRu - ContentX varyantı
 * Domain: four.playru.net
 */
class FourPlayRu : ContentX() {
    override val name = "FourPlayRu"
    override val mainUrl = "https://four.playru.net"
}


// ========== Hotlinger ==========

/**
 * Hotlinger - ContentX varyantı
 * Domain: hotlinger.com
 */
class Hotlinger : ContentX() {
    override val name = "Hotlinger"
    override val mainUrl = "https://hotlinger.com"
}

// ========== ORGDplayer ==========

/**
 * ORGDplayer - ContentX varyantı
 * Domain: org.dplayer82.site
 */
class ORGDplayer : ContentX() {
    override val name = "ORGDplayer"
    override val mainUrl = "https://org.dplayer82.site"
}


// ========== SNDplayer ==========

/**
 * SNDplayer - ContentX varyantı
 * Domain: sn.dplayer82.site
 */
class SNDplayer : ContentX() {
    override val name = "SNDplayer"
    override val mainUrl = "https://sn.dplayer82.site"
}
