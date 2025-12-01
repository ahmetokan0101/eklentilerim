# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import requests
from bs4 import BeautifulSoup
import sys
import io

# Windows terminal encoding sorunu için
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

def test_banner():
    """Banner/Cover image testi"""
    url = "https://dizi25.life/dizi/game-of-thrones"
    
    print("="*60)
    print("[TEST] Banner/Cover Image Testi")
    print("="*60)
    print(f"[URL] {url}\n")
    
    session = requests.Session()
    session.headers.update({
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    })
    
    try:
        r = session.get(url)
        r.raise_for_status()
        soup = BeautifulSoup(r.text, 'html.parser')
        
        # Test 1: div.dizi-cover-image img
        print("[TEST 1] div.dizi-cover-image img")
        cover_image = soup.select_one('div.dizi-cover-image img')
        if cover_image:
            cover_src = cover_image.get('src') or cover_image.get('data-src') or cover_image.get('data-lazy-src')
            print(f"  [OK] Bulundu!")
            print(f"  [SRC] {cover_src}")
            print(f"  [FULL URL] https://dizi25.life{cover_src if cover_src and cover_src.startswith('/') else '/' + cover_src if cover_src else ''}")
            print(f"  [ALT] {cover_image.get('alt', 'N/A')}")
        else:
            print("  [HATA] Bulunamadı!")
        
        # Test 2: div.dizi-cover-container içinde
        print("\n[TEST 2] div.dizi-cover-container içinde")
        cover_container = soup.select_one('div.dizi-cover-container')
        if cover_container:
            print(f"  [OK] div.dizi-cover-container bulundu")
            cover_image_in_container = cover_container.select_one('div.dizi-cover-image img')
            if cover_image_in_container:
                cover_src = cover_image_in_container.get('src') or cover_image_in_container.get('data-src') or cover_image_in_container.get('data-lazy-src')
                print(f"  [OK] İçinde img bulundu: {cover_src}")
            else:
                print("  [HATA] İçinde img bulunamadı!")
            
            # Overlay kontrolü
            overlay = cover_container.select_one('div.dizi-cover-overlay')
            if overlay:
                print(f"  [OK] div.dizi-cover-overlay bulundu (boş div, CSS overlay için)")
            else:
                print("  [HATA] div.dizi-cover-overlay bulunamadı!")
        else:
            print("  [HATA] div.dizi-cover-container bulunamadı!")
        
        # Test 3: div.content-poster img (küçük poster)
        print("\n[TEST 3] div.content-poster img (küçük poster)")
        content_poster = soup.select_one('div.content-poster img')
        if content_poster:
            poster_src = content_poster.get('src') or content_poster.get('data-src') or content_poster.get('data-lazy-src')
            print(f"  [OK] Bulundu: {poster_src}")
        else:
            print("  [HATA] Bulunamadı!")
        
        # Test 4: Tüm cover ile ilgili elementler
        print("\n[TEST 4] Tüm cover ile ilgili elementler")
        cover_section = soup.select_one('section.dizi-cover-section')
        if cover_section:
            print(f"  [OK] section.dizi-cover-section bulundu")
            print(f"  [HTML] {str(cover_section)[:300]}...")
        else:
            print("  [HATA] section.dizi-cover-section bulunamadı!")
        
        print("\n" + "="*60)
        print("[SONUC]")
        print("="*60)
        if cover_image:
            banner_url = cover_image.get('src') or cover_image.get('data-src') or cover_image.get('data-lazy-src')
            if banner_url:
                full_url = f"https://dizi25.life{banner_url}" if banner_url.startswith('/') else banner_url
                print(f"Banner URL: {full_url}")
                print(f"\nResmi görmek için tarayıcıda aç: {full_url}")
        
        print("\n[OK] Test tamamlandi!")
        
    except Exception as e:
        print(f"[HATA] Test basarisiz: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_banner()

