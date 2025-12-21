package com.movix.extractors

import android.util.Base64
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

/**
 * RapidVid Video Extractor
 * Domain: rapidvid.net
 * Custom AV decoding
 */
open class RapidVid : ExtractorApi() {
    override val name = "RapidVid"
    override val mainUrl = "https://rapidvid.net"
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
            
            // Encoded video URL pattern'ini ara
            val encodedPattern = Regex("""data-av\s*=\s*["']([^"']+)["']""")
            val encodedMatch = encodedPattern.find(pageContent)
            
            if (encodedMatch != null) {
                val encoded = encodedMatch.groupValues[1]
                try {
                    val decoded = decodeAv(encoded)
                    
                    // Decoded URL'i kullan
                    if (decoded.contains(".m3u8")) {
                        callback.invoke(
                            newExtractorLink(
                                source = name,
                                name = name,
                                url = decoded,
                                type = ExtractorLinkType.M3U8
                            ) {
                                this.referer = extRef
                                this.quality = Qualities.Unknown.value
                                this.headers = mapOf("Referer" to extRef)
                            }
                        )
                    } else if (decoded.startsWith("http")) {
                        callback.invoke(
                            newExtractorLink(
                                source = name,
                                name = name,
                                url = decoded
                            ) {
                                this.referer = extRef
                                this.quality = Qualities.Unknown.value
                                this.headers = mapOf("Referer" to extRef)
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
    
    /**
     * Custom AV decoding
     * 1. Reverse string
     * 2. Base64 decode
     * 3. Subtract key values
     * 4. Base64 decode again
     */
    private fun decodeAv(input: String): String {
        // Step 1: Reverse the string
        val reversed = input.reversed()
        
        // Step 2: First Base64 decode
        val firstPass = Base64.decode(reversed, Base64.DEFAULT)
        
        // Step 3: Subtract key values (K9L pattern)
        val key = "K9L"
        val adjusted = ByteArray(firstPass.size)
        for (i in firstPass.indices) {
            val sub = (firstPass[i].toInt() and 0xFF) - ((key[i % 3].code % 5) + 1)
            adjusted[i] = sub.toByte()
        }
        
        // Step 4: Second Base64 decode
        val secondPass = Base64.decode(adjusted, Base64.DEFAULT)
        
        return String(secondPass, Charsets.UTF_8)
    }
}
