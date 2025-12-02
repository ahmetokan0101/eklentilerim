#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizilife Stream URL Test Scripti
Java dosyasındaki loadLinks mantığını Python'da test eder
"""

import requests
from bs4 import BeautifulSoup
import re
import sys
import io
import json
from urllib.parse import urljoin, urlparse
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
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Sec-Fetch-Dest': 'document',
            'Sec-Fetch-Mode': 'navigate',
            'Sec-Fetch-Site': 'none',
            'Cache-Control': 'max-age=0'
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
    
    def find_iframes_in_html(self, doc: BeautifulSoup) -> list:
        """HTML'de iframe'leri bul"""
        iframes = []
        
        # 1. Tüm iframe'leri bul
        for iframe in doc.select("iframe"):
            src = iframe.get("data-src") or iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        # 2. iframe-container içindeki iframe'leri bul
        for container in doc.select("div.iframe-container"):
            for iframe in container.select("iframe"):
                src = iframe.get("data-src") or iframe.get("src")
                if src:
                    iframes.append(self.fix_url(src))
        
        # 3. videoIframe ID'li iframe'i bul
        video_iframe = doc.select_one("#videoIframe")
        if video_iframe:
            src = video_iframe.get("data-src") or video_iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        # 4. Video player container'ları kontrol et
        for container in doc.select("div.video-player, div.player, div#player"):
            for iframe in container.select("iframe"):
                src = iframe.get("data-src") or iframe.get("src")
                if src:
                    iframes.append(self.fix_url(src))
        
        return list(set(iframes))  # Duplicate'leri kaldır
    
    def find_iframes_in_scripts(self, html_content: str) -> list:
        """Script içinde iframe URL'leri ara"""
        iframes = []
        
        patterns = [
            r'iframe\s+src\s*=\s*["\']([^"\']+)["\']',
            r'iframe\s+data-src\s*=\s*["\']([^"\']+)["\']',
            r'videoIframe\.src\s*=\s*["\']([^"\']+)["\']',
            r'setAttribute\(["\']src["\'],\s*["\']([^"\']+)["\']',
            r'iframe\.setAttribute\(["\']src["\'],\s*["\']([^"\']+)["\']',
            r'["\']src["\']\s*:\s*["\']([^"\']+)["\']',
            r'iframeUrl\s*[:=]\s*["\']([^"\']+)["\']',
            r'embedUrl\s*[:=]\s*["\']([^"\']+)["\']',
            r'playerUrl\s*[:=]\s*["\']([^"\']+)["\']',
        ]
        
        for pattern in patterns:
            matches = re.findall(pattern, html_content, re.IGNORECASE)
            for match in matches:
                if match and match.startswith(('http', '/', '//')):
                    iframes.append(self.fix_url(match))
        
        return list(set(iframes))
    
    def check_ajax_endpoints(self, doc: BeautifulSoup, url: str) -> list:
        """AJAX endpoint'lerini kontrol et"""
        iframes = []
        
        # Script içinde AJAX çağrılarını ara
        scripts = doc.select("script")
        for script in scripts:
            if script.string:
                # AJAX URL pattern'leri
                ajax_patterns = [
                    r'["\']([^"\']*ajax[^"\']*iframe[^"\']*)["\']',
                    r'["\']([^"\']*api[^"\']*video[^"\']*)["\']',
                    r'["\']([^"\']*player[^"\']*\.php[^"\']*)["\']',
                    r'["\']([^"\']*embed[^"\']*\.php[^"\']*)["\']',
                ]
                
                for pattern in ajax_patterns:
                    matches = re.findall(pattern, script.string, re.IGNORECASE)
                    for match in matches:
                        if match:
                            ajax_url = self.fix_url(match)
                            try:
                                # AJAX endpoint'ini çağır
                                response = self.session.get(ajax_url, timeout=10)
                                if response.status_code == 200:
                                    ajax_doc = BeautifulSoup(response.text, 'html.parser')
                                    ajax_iframes = self.find_iframes_in_html(ajax_doc)
                                    iframes.extend(ajax_iframes)
                            except:
                                pass
        
        return list(set(iframes))
    
    def extract_encrypted_urls(self, doc: BeautifulSoup) -> list:
        """Şifreli URL'leri çıkart (Java'daki opensslAesPassphraseDecrypt mantığı)"""
        iframes = []
        
        # Base64 encoded URL pattern'leri ara
        scripts = doc.select("script")
        for script in scripts:
            if script.string:
                # Base64 pattern
                base64_pattern = r'["\']([A-Za-z0-9+/=]{20,})["\']'
                matches = re.findall(base64_pattern, script.string)
                
                for match in matches:
                    try:
                        # Base64 decode dene
                        decoded = base64.b64decode(match)
                        decoded_str = decoded.decode('utf-8', errors='ignore')
                        
                        # URL pattern'i ara
                        url_pattern = r'https?://[^\s"\'<>]+'
                        urls = re.findall(url_pattern, decoded_str)
                        for url in urls:
                            if 'iframe' in url.lower() or 'embed' in url.lower() or 'player' in url.lower():
                                iframes.append(self.fix_url(url))
                    except:
                        pass
        
        return list(set(iframes))
    
    def check_data_attributes(self, doc: BeautifulSoup) -> list:
        """Data attribute'larında iframe URL'leri ara"""
        iframes = []
        
        # data-url, data-src, data-iframe gibi attribute'ları kontrol et
        for element in doc.select("[data-url], [data-src], [data-iframe], [data-embed], [data-player]"):
            for attr in ['data-url', 'data-src', 'data-iframe', 'data-embed', 'data-player']:
                value = element.get(attr)
                if value and ('iframe' in value.lower() or 'embed' in value.lower() or 'player' in value.lower() or value.startswith('http')):
                    iframes.append(self.fix_url(value))
        
        return list(set(iframes))
    
    def follow_iframe_chain(self, iframe_url: str, max_depth: int = 3) -> list:
        """Iframe zincirini takip et (nested iframe'ler)"""
        iframes = []
        visited = set()
        
        def follow(url: str, depth: int):
            if depth > max_depth or url in visited:
                return
            visited.add(url)
            
            try:
                response = self.session.get(url, timeout=10)
                if response.status_code == 200:
                    doc = BeautifulSoup(response.text, 'html.parser')
                    nested_iframes = self.find_iframes_in_html(doc)
                    
                    for nested in nested_iframes:
                        if nested not in visited:
                            iframes.append(nested)
                            follow(nested, depth + 1)
            except:
                pass
        
        follow(iframe_url, 0)
        return list(set(iframes))
    
    def find_stream_urls(self, url: str) -> dict:
        """Stream URL'lerini bul"""
        print(f"\n{'='*60}")
        print(f"[STREAM URL TEST] {url}")
        print(f"{'='*60}")
        
        doc = self.get_page(url)
        if not doc:
            return {"error": "Sayfa alınamadı"}
        
        all_iframes = []
        
        # 1. HTML'de iframe'leri bul
        print("\n[1] HTML'de iframe arama...")
        html_iframes = self.find_iframes_in_html(doc)
        print(f"   {len(html_iframes)} iframe bulundu")
        for i, iframe in enumerate(html_iframes, 1):
            print(f"   {i}. {iframe}")
        all_iframes.extend(html_iframes)
        
        # 2. Script içinde iframe URL'leri ara
        if not all_iframes:
            print("\n[2] Script içinde iframe arama...")
            script_iframes = self.find_iframes_in_scripts(doc.prettify())
            print(f"   {len(script_iframes)} iframe bulundu")
            for i, iframe in enumerate(script_iframes, 1):
                print(f"   {i}. {iframe}")
            all_iframes.extend(script_iframes)
        
        # 3. Data attribute'larını kontrol et
        if not all_iframes:
            print("\n[3] Data attribute'larında arama...")
            data_iframes = self.check_data_attributes(doc)
            print(f"   {len(data_iframes)} iframe bulundu")
            for i, iframe in enumerate(data_iframes, 1):
                print(f"   {i}. {iframe}")
            all_iframes.extend(data_iframes)
        
        # 4. Şifreli URL'leri çıkart
        if not all_iframes:
            print("\n[4] Şifreli URL'leri çıkartma...")
            encrypted_iframes = self.extract_encrypted_urls(doc)
            print(f"   {len(encrypted_iframes)} iframe bulundu")
            for i, iframe in enumerate(encrypted_iframes, 1):
                print(f"   {i}. {iframe}")
            all_iframes.extend(encrypted_iframes)
        
        # 5. AJAX endpoint'lerini kontrol et
        if not all_iframes:
            print("\n[5] AJAX endpoint'lerini kontrol etme...")
            ajax_iframes = self.check_ajax_endpoints(doc, url)
            print(f"   {len(ajax_iframes)} iframe bulundu")
            for i, iframe in enumerate(ajax_iframes, 1):
                print(f"   {i}. {iframe}")
            all_iframes.extend(ajax_iframes)
        
        # 6. Iframe zincirini takip et
        final_iframes = []
        for iframe in all_iframes:
            final_iframes.append(iframe)
            nested = self.follow_iframe_chain(iframe, max_depth=2)
            final_iframes.extend(nested)
        
        final_iframes = list(set(final_iframes))
        
        print(f"\n[SONUÇ] Toplam {len(final_iframes)} benzersiz iframe bulundu:")
        for i, iframe in enumerate(final_iframes, 1):
            print(f"   {i}. {iframe}")
        
        return {
            "url": url,
            "iframes": final_iframes,
            "count": len(final_iframes)
        }

def main():
    tester = DizilifeStreamTester()
    
    # Test URL'leri
    test_urls = [
        "https://dizi25.life/dizi/game-of-thrones",
        "https://dizi25.life/dizi/game-of-thrones/sezon-1/bolum-1",
        "https://dizi25.life/dizi/game-of-thrones/sezon-1/bolum-2",
        "https://dizi25.life/film/inception",  # Film örneği
    ]
    
    print("="*60)
    print("DIZILIFE STREAM URL TEST SCRIPTI")
    print("Java dosyasındaki loadLinks mantığını Python'da test eder")
    print("="*60)
    
    results = []
    for url in test_urls:
        result = tester.find_stream_urls(url)
        results.append(result)
        print("\n")
    
    # Özet
    print("\n" + "="*60)
    print("ÖZET")
    print("="*60)
    for result in results:
        print(f"\n{result.get('url', 'N/A')}")
        print(f"  Iframe sayısı: {result.get('count', 0)}")
        if result.get('iframes'):
            for iframe in result['iframes'][:3]:  # İlk 3'ünü göster
                print(f"    - {iframe}")

if __name__ == "__main__":
    main()

