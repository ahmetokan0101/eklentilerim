#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizilife Stream URL Advanced Test Scripti
Episode card'ları ve JavaScript event'lerini analiz eder
"""

import requests
from bs4 import BeautifulSoup
import re
import sys
import io
import json
from urllib.parse import urljoin, urlparse, parse_qs

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

class DizilifeStreamAdvancedTester:
    def __init__(self):
        self.main_url = "https://dizi25.life"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
            'Referer': self.main_url
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
    
    def analyze_episode_cards(self, doc: BeautifulSoup) -> list:
        """Episode card'larını analiz et"""
        print("\n[EPISODE CARDS] Episode card'larını analiz ediyorum...")
        
        episode_cards = doc.select("div.episode-card")
        print(f"   {len(episode_cards)} episode card bulundu")
        
        urls = []
        for i, card in enumerate(episode_cards[:5], 1):  # İlk 5'i analiz et
            print(f"\n   Episode Card {i}:")
            
            # Data attribute'ları
            episode_id = card.get("data-episode-id")
            season_id = card.get("data-season-id")
            season_num = card.get("data-season-number")
            episode_num = card.get("data-episode-number")
            enabled = card.get("data-enabled")
            
            print(f"      data-episode-id: {episode_id}")
            print(f"      data-season-id: {season_id}")
            print(f"      data-season-number: {season_num}")
            print(f"      data-episode-number: {episode_num}")
            print(f"      data-enabled: {enabled}")
            
            # Onclick attribute
            onclick = card.get("onclick")
            if onclick:
                print(f"      onclick: {onclick[:100]}...")
                # URL çıkart
                url_match = re.search(r'["\']([^"\']+)["\']', onclick)
                if url_match:
                    url = url_match.group(1)
                    urls.append(self.fix_url(url))
            
            # Episode play button
            play_button = card.select_one("button.episode-play-button")
            if play_button:
                play_onclick = play_button.get("onclick")
                if play_onclick:
                    print(f"      play button onclick: {play_onclick[:100]}...")
                    url_match = re.search(r'["\']([^"\']+)["\']', play_onclick)
                    if url_match:
                        url = url_match.group(1)
                        urls.append(self.fix_url(url))
            
            # Episode URL oluştur
            if season_num and episode_num:
                # Ana dizinin slug'ını bul
                current_url = doc.select_one("link[rel='canonical']")
                if current_url:
                    canonical = current_url.get("href", "")
                    if "/dizi/" in canonical:
                        slug = canonical.split("/dizi/")[1].split("?")[0].split("#")[0]
                        episode_url = f"{self.main_url}/dizi/{slug}/sezon-{season_num}/bolum-{episode_num}"
                        urls.append(episode_url)
                        print(f"      Oluşturulan URL: {episode_url}")
        
        return list(set(urls))
    
    def analyze_javascript_events(self, doc: BeautifulSoup) -> list:
        """JavaScript event'lerini analiz et"""
        print("\n[JAVASCRIPT EVENTS] JavaScript event'lerini analiz ediyorum...")
        
        urls = []
        scripts = doc.select("script")
        
        for script in scripts:
            if script.string:
                content = script.string
                
                # Episode click handler'ları
                patterns = [
                    r'episode-card.*?onclick.*?["\']([^"\']+)["\']',
                    r'episode.*?click.*?["\']([^"\']+)["\']',
                    r'loadEpisode.*?["\']([^"\']+)["\']',
                    r'playEpisode.*?["\']([^"\']+)["\']',
                    r'watchEpisode.*?["\']([^"\']+)["\']',
                    r'window\.location.*?["\']([^"\']+)["\']',
                    r'location\.href.*?["\']([^"\']+)["\']',
                ]
                
                for pattern in patterns:
                    matches = re.findall(pattern, content, re.IGNORECASE | re.DOTALL)
                    for match in matches:
                        if match and ('sezon' in match.lower() or 'bolum' in match.lower() or 'episode' in match.lower()):
                            urls.append(self.fix_url(match))
                            print(f"   Bulunan URL: {match}")
        
        return list(set(urls))
    
    def check_api_endpoints(self, doc: BeautifulSoup) -> list:
        """API endpoint'lerini kontrol et"""
        print("\n[API ENDPOINTS] API endpoint'lerini kontrol ediyorum...")
        
        urls = []
        scripts = doc.select("script")
        
        for script in scripts:
            if script.string:
                content = script.string
                
                # API pattern'leri
                api_patterns = [
                    r'["\']([^"\']*api[^"\']*episode[^"\']*)["\']',
                    r'["\']([^"\']*api[^"\']*video[^"\']*)["\']',
                    r'["\']([^"\']*ajax[^"\']*episode[^"\']*)["\']',
                    r'fetch\(["\']([^"\']+)["\']',
                    r'axios\.get\(["\']([^"\']+)["\']',
                    r'\.post\(["\']([^"\']+)["\']',
                    r'\.get\(["\']([^"\']+)["\']',
                ]
                
                for pattern in api_patterns:
                    matches = re.findall(pattern, content, re.IGNORECASE)
                    for match in matches:
                        if match and ('api' in match.lower() or 'ajax' in match.lower()):
                            api_url = self.fix_url(match)
                            urls.append(api_url)
                            print(f"   API URL: {api_url}")
                            
                            # API'yi çağır
                            try:
                                response = self.session.get(api_url, timeout=10)
                                if response.status_code == 200:
                                    try:
                                        data = response.json()
                                        print(f"      Response: {json.dumps(data, indent=2)[:200]}...")
                                    except:
                                        print(f"      Response (text): {response.text[:200]}...")
                            except Exception as e:
                                print(f"      Error: {e}")
        
        return list(set(urls))
    
    def test_episode_url(self, episode_url: str) -> dict:
        """Episode URL'sini test et"""
        print(f"\n{'='*60}")
        print(f"[EPISODE TEST] {episode_url}")
        print(f"{'='*60}")
        
        doc = self.get_page(episode_url)
        if not doc:
            return {"error": "Sayfa alınamadı"}
        
        # Iframe'leri bul
        iframes = []
        
        # 1. HTML'de iframe
        for iframe in doc.select("iframe"):
            src = iframe.get("data-src") or iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        # 2. iframe-container
        for container in doc.select("div.iframe-container"):
            for iframe in container.select("iframe"):
                src = iframe.get("data-src") or iframe.get("src")
                if src:
                    iframes.append(self.fix_url(src))
        
        # 3. videoIframe ID
        video_iframe = doc.select_one("#videoIframe")
        if video_iframe:
            src = video_iframe.get("data-src") or video_iframe.get("src")
            if src:
                iframes.append(self.fix_url(src))
        
        # 4. Script içinde iframe
        html_content = doc.prettify()
        iframe_patterns = [
            r'iframe\s+src\s*=\s*["\']([^"\']+)["\']',
            r'videoIframe\.src\s*=\s*["\']([^"\']+)["\']',
            r'setAttribute\(["\']src["\'],\s*["\']([^"\']+)["\']',
        ]
        
        for pattern in iframe_patterns:
            matches = re.findall(pattern, html_content, re.IGNORECASE)
            for match in matches:
                if match:
                    iframes.append(self.fix_url(match))
        
        iframes = list(set(iframes))
        
        print(f"\n[SONUÇ] {len(iframes)} iframe bulundu:")
        for i, iframe in enumerate(iframes, 1):
            print(f"   {i}. {iframe}")
        
        return {
            "url": episode_url,
            "iframes": iframes,
            "count": len(iframes)
        }
    
    def comprehensive_test(self, series_url: str):
        """Kapsamlı test"""
        print("="*60)
        print("DIZILIFE STREAM URL ADVANCED TEST")
        print("="*60)
        
        doc = self.get_page(series_url)
        if not doc:
            print("[ERROR] Sayfa alınamadı")
            return
        
        # 1. Episode card'larını analiz et
        episode_urls = self.analyze_episode_cards(doc)
        
        # 2. JavaScript event'lerini analiz et
        js_urls = self.analyze_javascript_events(doc)
        
        # 3. API endpoint'lerini kontrol et
        api_urls = self.check_api_endpoints(doc)
        
        # 4. Episode URL'lerini test et
        all_urls = list(set(episode_urls + js_urls))
        
        print(f"\n[ÖZET] Toplam {len(all_urls)} URL bulundu")
        for url in all_urls[:5]:  # İlk 5'ini test et
            result = self.test_episode_url(url)
            if result.get("iframes"):
                print(f"\n[BAŞARILI] {url} için iframe bulundu!")
                break

def main():
    tester = DizilifeStreamAdvancedTester()
    
    # Test URL'leri
    test_urls = [
        "https://dizi25.life/dizi/game-of-thrones",
    ]
    
    for url in test_urls:
        tester.comprehensive_test(url)

if __name__ == "__main__":
    main()

