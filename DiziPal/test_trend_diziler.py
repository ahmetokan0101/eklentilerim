#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizipal Trend Diziler Test Scripti
Ana sayfadaki Trend Diziler bölümünden verileri çeker ve test eder
"""

import requests
from bs4 import BeautifulSoup
import sys
import io

# Windows terminal encoding sorunu için
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

def fix_url(url: str, main_url: str) -> str:
    """URL'i düzelt"""
    if not url:
        return ""
    if url.startswith("//"):
        return "https:" + url
    if url.startswith("/"):
        return main_url.rstrip("/") + url
    if not url.startswith("http"):
        return main_url.rstrip("/") + "/" + url
    return url

def extract_title_from_url(url: str) -> str:
    """URL'den başlık çıkar (örn: /series/stranger-things -> Stranger Things)"""
    if not url:
        return ""
    
    # URL'den son kısmı al
    slug = url.rstrip("/").split("/")[-1]
    
    # Tire ile ayrılmış kelimeleri büyük harfle başlat
    title = " ".join(word.capitalize() for word in slug.split("-"))
    
    return title

def test_trend_diziler():
    """Trend Diziler bölümünü test et"""
    main_url = "https://dizipal1515.com/"
    
    print("="*70)
    print("[TEST] Dizipal - Trend Diziler Testi")
    print("="*70)
    print(f"[URL] {main_url}\n")
    
    session = requests.Session()
    session.headers.update({
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
        'Referer': main_url,
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1'
    })
    
    try:
        print("[1/3] Ana sayfa yükleniyor...")
        response = session.get(main_url, timeout=15)
        response.raise_for_status()
        print(f"[OK] Sayfa yüklendi (Status: {response.status_code})\n")
        
        print("[2/3] HTML parse ediliyor...")
        soup = BeautifulSoup(response.text, 'html.parser')
        print("[OK] HTML parse edildi\n")
        
        print("[3/3] Trend Diziler bölümü aranıyor...")
        trends_list = soup.select("ul.trends li")
        
        if not trends_list:
            print("[HATA] Trend Diziler bölümü bulunamadı!")
            print("\n[DEBUG] Alternatif selector'lar deneniyor...")
            
            # Alternatif selector'lar
            alt_selectors = [
                "ul.trends",
                ".trends li",
                "ul[class*='trend'] li",
                "div[class*='trend'] li"
            ]
            
            for selector in alt_selectors:
                elements = soup.select(selector)
                print(f"  - '{selector}': {len(elements)} element bulundu")
            
            return
        
        print(f"[OK] {len(trends_list)} dizi bulundu!\n")
        
        print("="*70)
        print("TREND DİZİLER LİSTESİ")
        print("="*70)
        print()
        
        results = []
        
        for idx, li in enumerate(trends_list, 1):
            # URL'yi bul
            a_tag = li.select_one("a")
            if not a_tag:
                continue
            
            href = a_tag.get("href", "")
            href = fix_url(href, main_url)
            
            # Poster URL'sini bul
            img_tag = li.select_one("img")
            poster_url = ""
            if img_tag:
                poster_url = img_tag.get("data-src") or img_tag.get("src", "")
                poster_url = fix_url(poster_url, main_url)
            
            # Başlık çıkar - önce URL'den
            title = extract_title_from_url(href)
            
            # Eğer URL'den çıkarılamadıysa, alt text'ten dene
            if not title or title == "Izle":
                if img_tag and img_tag.get("alt"):
                    alt_text = img_tag.get("alt").strip()
                    if alt_text and alt_text != " izle":
                        title = alt_text.replace(" izle", "").strip()
            
            # Hala başlık yoksa, URL'den slug'ı düzgün parse et
            if not title or title == "Izle":
                # URL'den slug'ı al ve düzgün formatla
                slug = href.rstrip("/").split("/")[-1]
                # Tire ile ayrılmış kelimeleri büyük harfle başlat
                title = " ".join(word.capitalize() for word in slug.split("-"))
            
            results.append({
                "index": idx,
                "title": title,
                "url": href,
                "poster": poster_url
            })
            
            print(f"[{idx:2d}] {title}")
            print(f"     URL: {href}")
            print(f"     Poster: {poster_url}")
            print()
        
        print("="*70)
        print(f"TOPLAM: {len(results)} dizi bulundu")
        print("="*70)
        
        # Özet
        print("\n[ÖZET]")
        print(f"  - Toplam dizi sayısı: {len(results)}")
        print(f"  - URL'ler: {'✓' if all(r['url'] for r in results) else '✗'}")
        print(f"  - Poster'ler: {'✓' if any(r['poster'] for r in results) else '✗'}")
        print(f"  - Başlıklar: {'✓' if all(r['title'] for r in results) else '✗'}")
        
        # İlk 3 örneği detaylı göster
        if results:
            print("\n[İLK 3 ÖRNEK DETAY]")
            for r in results[:3]:
                print(f"\n  Başlık: {r['title']}")
                print(f"  URL: {r['url']}")
                print(f"  Poster: {r['poster'][:80]}..." if len(r['poster']) > 80 else f"  Poster: {r['poster']}")
        
    except requests.exceptions.RequestException as e:
        print(f"[HATA] İstek hatası: {e}")
    except Exception as e:
        print(f"[HATA] Beklenmeyen hata: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_trend_diziler()

