#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizilife Stream URL Complete Test Scripti
Java dosyasındaki loadLinks mantığını Python'da detaylıca test eder
Player sayfasındaki şifreli URL'leri çözer
"""

import requests
from bs4 import BeautifulSoup
import re
import sys
import io
import json
from urllib.parse import urljoin, urlparse, parse_qs
import base64
from Crypto.Cipher import AES
from Crypto.Protocol.KDF import PBKDF2
import hashlib

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

class DizilifeStreamTester:
    def __init__(self):
        self.main_url = "https://dizi25.life"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
            'Referer': self.main_url,
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1'
        })
    
    def fix_url(self, url: str) -> str:
        """URL'i düzelt"""
        if not url:
            return ""
        if url.startswith("//"):
            return "https:" + url
        if url.startswith("/"):
            return self.main_url + url
        if not url.startswith("http"):
            return self.main_url + "/" + url
        return url
    
    def get_page(self, url: str) -> BeautifulSoup:
        """Sayfayı al"""
        try:
            response = self.session.get(url, timeout=15)
            response.raise_for_status()
            return BeautifulSoup(response.text, 'html.parser')
        except Exception as e:
            print(f"[ERROR] Sayfa alınamadı: {e}")
            return None
    
    # ========== OPENSSL AES DECRYPT (Java'daki mantık) ==========
    def openssl_aes_decrypt(self, base64_cipher: str, passphrase: str) -> str:
        """OpenSSL AES decrypt - Java'daki opensslAesPassphraseDecrypt mantığı"""
        try:
            cipher_data = base64.b64decode(base64_cipher)
            salted = b"Salted__"
            
            if len(cipher_data) >= 16 and cipher_data[:8] == salted:
                salt = cipher_data[8:16]
                enc = cipher_data[16:]
                
                # EVP_BytesToKey - MD5 hash ile key ve IV oluştur
                key, iv = self.evp_bytes_to_key(passphrase.encode('utf-8'), salt, 32, 16)
                
                # AES CBC decrypt
                cipher = AES.new(key, AES.MODE_CBC, iv)
                plain = cipher.decrypt(enc)
                
                # PKCS5 padding kaldır
                padding = plain[-1]
                plain = plain[:-padding]
                
                return plain.decode('utf-8', errors='ignore')
        except Exception as e:
            print(f"   [DECRYPT ERROR] {e}")
        return None
    
    def evp_bytes_to_key(self, passphrase: bytes, salt: bytes, key_len: int, iv_len: int) -> tuple:
        """EVP_BytesToKey - Java'daki mantık"""
        total_len = key_len + iv_len
        result = []
        prev = b''
        
        while sum(len(x) for x in result) < total_len:
            data = prev + passphrase + salt
            prev = hashlib.md5(data).digest()
            result.append(prev)
        
        derived = b''.join(result)
        key = derived[:key_len]
        iv = derived[key_len:key_len+iv_len]
        return key, iv
    
    # ========== HTML'DE IFRAME BULMA ==========
    def find_iframes_in_html(self, doc: BeautifulSoup) -> list:
        """HTML'de iframe'leri bul"""
        iframes = []
        
        for iframe in doc.select("iframe"):
            src = iframe.get("data-src") or iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        for container in doc.select("div.iframe-container"):
            for iframe in container.select("iframe"):
                src = iframe.get("data-src") or iframe.get("src")
                if src:
                    iframes.append(self.fix_url(src))
        
        video_iframe = doc.select_one("#videoIframe")
        if video_iframe:
            src = video_iframe.get("data-src") or video_iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        for container in doc.select("div.video-player, div.player, div#player"):
            for iframe in container.select("iframe"):
                src = iframe.get("data-src") or iframe.get("src")
                if src:
                    iframes.append(self.fix_url(src))
        
        return list(set(iframes))
    
    # ========== SCRIPT İÇİNDE IFRAME URL'LERİ ==========
    def find_iframes_in_scripts(self, html_content: str) -> list:
        """Script içinde iframe URL'leri ara"""
        iframes = []
        
        patterns = [
            r'iframe\s+src\s*=\s*["\']([^"\']+)["\']',
            r'iframe\s+data-src\s*=\s*["\']([^"\']+)["\']',
            r'videoIframe\.src\s*=\s*["\']([^"\']+)["\']',
            r'setAttribute\(["\']src["\'],\s*["\']([^"\']+)["\']',
        ]
        
        for pattern in patterns:
            matches = re.findall(pattern, html_content, re.IGNORECASE)
            for match in matches:
                if match and match.startswith(('http', '/', '//')):
                    iframes.append(self.fix_url(match))
        
        return list(set(iframes))
    
    # ========== EPISODE CARD ANALİZİ ==========
    def analyze_episode_cards(self, doc: BeautifulSoup) -> list:
        """Episode card'larını analiz et ve watch URL'lerini oluştur"""
        episode_urls = []
        
        episode_cards = doc.select("div.episode-card")
        print(f"   {len(episode_cards)} episode card bulundu")
        
        canonical = doc.select_one("link[rel='canonical']")
        slug = ""
        if canonical:
            canonical_url = canonical.get("href", "")
            if "/dizi/" in canonical_url:
                slug = canonical_url.split("/dizi/")[1].split("?")[0].split("#")[0]
        
        for card in episode_cards[:10]:
            episode_id = card.get("data-episode-id")
            season_num = card.get("data-season-number")
            episode_num = card.get("data-episode-number")
            
            if episode_id and season_num and episode_num and slug:
                watch_url = f"{self.main_url}/dizi/{slug}/{season_num}-sezon-{episode_num}-bolum/{episode_id}"
                episode_urls.append(watch_url)
        
        return list(set(episode_urls))
    
    # ========== PLAYER SAYFASINDAN STREAM URL ÇIKARMA ==========
    def extract_stream_from_player(self, player_url: str) -> list:
        """Player sayfasından gerçek stream URL'lerini çıkar"""
        print(f"\n[PLAYER] {player_url}")
        
        try:
            response = self.session.get(player_url, timeout=15)
            response.raise_for_status()
            html_content = response.text
            doc = BeautifulSoup(html_content, 'html.parser')
        except Exception as e:
            print(f"   [ERROR] Player sayfası alınamadı: {e}")
            return []
        
        stream_urls = []
        
        # 1. Şifreli string'i bul (C.A.dct pattern)
        scripts = doc.select("script")
        for script in scripts:
            if script.string:
                # C.A.dct("...", "...") pattern'i ara
                decrypt_pattern = r'C\.A\.dct\(["\']([^"\']+)["\'],\s*["\']([^"\']+)["\']\)'
                matches = re.findall(decrypt_pattern, script.string)
                
                for encrypted_str, passphrase in matches:
                    print(f"   [DECRYPT] Şifreli string bulundu (uzunluk: {len(encrypted_str)})")
                    print(f"   [DECRYPT] Passphrase: {passphrase}")
                    
                    # Decrypt et
                    decrypted = self.openssl_aes_decrypt(encrypted_str, passphrase)
                    if decrypted:
                        print(f"   [DECRYPT] Çözülen string: {decrypted[:200]}...")
                        
                        # Çözülen string'de URL'leri ara
                        url_patterns = [
                            r'https?://[^\s"\'<>]+',
                            r'src=["\']([^"\']+)["\']',
                            r'iframe[^>]+src=["\']([^"\']+)["\']',
                        ]
                        
                        for pattern in url_patterns:
                            url_matches = re.findall(pattern, decrypted, re.IGNORECASE)
                            for url_match in url_matches:
                                if isinstance(url_match, tuple):
                                    url_match = url_match[0] if url_match else ""
                                if url_match and url_match.startswith('http'):
                                    stream_urls.append(url_match)
                                    print(f"      [STREAM] {url_match}")
                
                # document.write içinde de olabilir
                write_pattern = r'document\.write\([^)]*C\.A\.dct\(["\']([^"\']+)["\'],\s*["\']([^"\']+)["\']\)'
                write_matches = re.findall(write_pattern, script.string, re.DOTALL)
                
                for encrypted_str, passphrase in write_matches:
                    print(f"   [DECRYPT] document.write içinde şifreli string bulundu")
                    decrypted = self.openssl_aes_decrypt(encrypted_str, passphrase)
                    if decrypted:
                        print(f"   [DECRYPT] Çözülen string: {decrypted[:200]}...")
                        
                        # replace pattern'i de kontrol et
                        replace_pattern = r'\.replace\(["\']([^"\']+)["\'],\s*["\']([^"\']+)["\']\)'
                        replace_match = re.search(replace_pattern, script.string)
                        if replace_match:
                            old_str = replace_match.group(1)
                            new_str = replace_match.group(2)
                            decrypted = decrypted.replace(old_str, new_str)
                            print(f"   [REPLACE] {old_str} -> {new_str}")
                        
                        # URL'leri ara
                        url_matches = re.findall(r'https?://[^\s"\'<>]+', decrypted, re.IGNORECASE)
                        for url in url_matches:
                            if any(ext in url.lower() for ext in ['.m3u8', '.mp4', '.mpd', 'iframe', 'embed', 'player']):
                                stream_urls.append(url)
                                print(f"      [STREAM] {url}")
        
        # 2. HTML içinde direkt stream URL'leri
        stream_patterns = [
            r'["\'](https?://[^"\']*\.m3u8[^"\']*)["\']',
            r'["\'](https?://[^"\']*\.mp4[^"\']*)["\']',
            r'["\'](https?://[^"\']*\.mpd[^"\']*)["\']',
        ]
        
        for pattern in stream_patterns:
            matches = re.findall(pattern, html_content, re.IGNORECASE)
            for match in matches:
                stream_urls.append(match)
        
        # 3. Iframe'leri de kontrol et
        html_iframes = self.find_iframes_in_html(doc)
        for iframe in html_iframes:
            stream_urls.append(iframe)
        
        stream_urls = list(set(stream_urls))
        
        print(f"   {len(stream_urls)} stream URL bulundu")
        for i, url in enumerate(stream_urls[:10], 1):
            print(f"      {i}. {url}")
        
        return stream_urls
    
    # ========== WATCH SAYFASI TESTİ ==========
    def test_watch_page(self, watch_url: str) -> dict:
        """Watch sayfasını test et ve gerçek stream URL'lerini bul"""
        print(f"\n[WATCH PAGE] {watch_url}")
        
        doc = self.get_page(watch_url)
        if not doc:
            return {"error": "Sayfa alınamadı", "iframes": [], "streams": []}
        
        all_iframes = []
        
        # HTML'de iframe
        html_iframes = self.find_iframes_in_html(doc)
        all_iframes.extend(html_iframes)
        
        # Script içinde iframe
        if not all_iframes:
            script_iframes = self.find_iframes_in_scripts(doc.prettify())
            all_iframes.extend(script_iframes)
        
        # Player sayfalarından stream URL'leri çıkar
        all_streams = []
        for iframe_url in all_iframes:
            print(f"\n   Player iframe bulundu: {iframe_url}")
            streams = self.extract_stream_from_player(iframe_url)
            all_streams.extend(streams)
        
        # Iframe zinciri
        final_iframes = []
        for iframe in all_iframes:
            final_iframes.append(iframe)
        
        final_iframes = list(set(final_iframes))
        all_streams = list(set(all_streams))
        
        print(f"\n   ÖZET:")
        print(f"   - {len(final_iframes)} player iframe bulundu")
        print(f"   - {len(all_streams)} gerçek stream URL bulundu")
        
        if all_streams:
            print(f"\n   GERÇEK STREAM URL'LERİ:")
            for i, stream in enumerate(all_streams[:10], 1):
                print(f"      {i}. {stream}")
        
        return {
            "url": watch_url,
            "iframes": final_iframes,
            "streams": all_streams,
            "iframe_count": len(final_iframes),
            "stream_count": len(all_streams)
        }
    
    # ========== ANA TEST FONKSİYONU ==========
    def comprehensive_test(self, series_url: str):
        """Kapsamlı test"""
        print("="*70)
        print("DIZILIFE STREAM URL COMPLETE TEST")
        print("Player sayfasındaki şifreli URL'leri çözer")
        print("="*70)
        
        doc = self.get_page(series_url)
        if not doc:
            print("[ERROR] Sayfa alınamadı")
            return
        
        print(f"\n[TEST URL] {series_url}")
        
        # Episode card analizi
        print("\n[1] Episode card analizi...")
        episode_urls = self.analyze_episode_cards(doc)
        print(f"   {len(episode_urls)} watch URL oluşturuldu")
        if episode_urls:
            print(f"   İlk 3 watch URL:")
            for i, url in enumerate(episode_urls[:3], 1):
                print(f"      {i}. {url}")
        
        # Watch sayfalarını test et
        if episode_urls:
            print("\n[2] Watch sayfalarını test ediyorum ve gerçek stream URL'lerini arıyorum...")
            for watch_url in episode_urls[:3]:  # İlk 3 watch URL'ini test et
                result = self.test_watch_page(watch_url)
                if result.get("streams"):
                    print(f"\n[BAŞARILI] {watch_url} için {result['stream_count']} gerçek stream URL bulundu!")
                    print("   İlk 5 stream URL:")
                    for i, stream in enumerate(result['streams'][:5], 1):
                        print(f"      {i}. {stream}")
                    break  # İlk başarılı sonuçta dur
                elif result.get("iframes"):
                    print(f"\n[KISMEN] {watch_url} için {result['iframe_count']} player iframe bulundu")
        
        print("\n" + "="*70)
        print("TEST TAMAMLANDI")
        print("="*70)

def main():
    tester = DizilifeStreamTester()
    
    # Test URL'leri
    test_urls = [
        "https://dizi25.life/dizi/game-of-thrones",
    ]
    
    for url in test_urls:
        tester.comprehensive_test(url)
        print("\n\n")

if __name__ == "__main__":
    main()
