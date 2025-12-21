package com.movix.extractors

import android.util.Base64
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * HotStream Video Extractor
 * Domain: hotstream.club
 * AES decryption desteği
 */
open class HotStreamExtractor : ExtractorApi() {
    override val name = "HotStream"
    override val mainUrl = "https://hotstream.club"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val response = app.get(url)
            val doc = response.document
            val pageContent = response.text
            
            // Encrypted data pattern'lerini ara
            val encryptedPattern = Regex("""data-enc\s*=\s*["']([^"']+)["']""")
            val passwordPattern = Regex("""data-pass\s*=\s*["']([^"']+)["']""")
            val ivPattern = Regex("""data-iv\s*=\s*["']([^"']+)["']""")
            val saltPattern = Regex("""data-salt\s*=\s*["']([^"']+)["']""")
            
            val encryptedMatch = encryptedPattern.find(pageContent)
            val passwordMatch = passwordPattern.find(pageContent)
            val ivMatch = ivPattern.find(pageContent)
            val saltMatch = saltPattern.find(pageContent)
            
            if (encryptedMatch != null && passwordMatch != null && ivMatch != null && saltMatch != null) {
                val encrypted = encryptedMatch.groupValues[1]
                val password = passwordMatch.groupValues[1]
                val iv = ivMatch.groupValues[1]
                val salt = saltMatch.groupValues[1]
                
                try {
                    val decrypted = decryptAES(encrypted, password, iv, salt)
                    
                    // Decrypted içinde M3U8 linklerini ara
                    val m3u8Pattern = Regex("""(https?://[^\s"'<>]+\.m3u8[^\s"'<>]*)""")
                    val m3u8Links = m3u8Pattern.findAll(decrypted).map { it.value }.toList()
                    
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
                            }
                        )
                    }
                } catch (e: Exception) {
                    // Decryption hatası
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
                    }
                )
            }
            
        } catch (e: Exception) {
            // Ana hata durumunda sessizce geç
        }
    }
    
    /**
     * AES CBC decryption with MD5 key derivation
     */
    private fun decryptAES(ct: String, password: String, ivHex: String, saltHex: String): String {
        val salt = hexToBytes(saltHex)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        
        // MD5 key derivation (OpenSSL compatible)
        val md5_1 = MessageDigest.getInstance("MD5")
        md5_1.update(passwordBytes)
        md5_1.update(salt)
        val d1 = md5_1.digest()
        
        val md5_2 = MessageDigest.getInstance("MD5")
        md5_2.update(d1)
        md5_2.update(passwordBytes)
        md5_2.update(salt)
        val d2 = md5_2.digest()
        
        val key = d1 + d2
        val iv = hexToBytes(ivHex)
        val ciphertext = Base64.decode(ct, Base64.DEFAULT)
        
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        
        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, Charsets.UTF_8)
    }
    
    /**
     * Convert hex string to byte array
     */
    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
