# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import requests
from bs4 import BeautifulSoup
import sys
import io

# Windows terminal encoding sorunu için
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

def test_rating():
    """Rating parsing testi"""
    url = "https://dizi25.life/dizi/game-of-thrones"
    
    print("="*60)
    print("[TEST] Rating Parsing Testi")
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
        
        # Test 1: span.rating-value
        print("[TEST 1] span.rating-value")
        rating_value = soup.select_one('span.rating-value')
        if rating_value:
            print(f"  [OK] Bulundu: {rating_value.text.strip()}")
            try:
                rating_float = float(rating_value.text.strip())
                print(f"  [OK] Float değeri: {rating_float}")
            except Exception as e:
                print(f"  [HATA] Float'a çevrilemedi: {e}")
        else:
            print("  [HATA] Bulunamadı!")
        
        # Test 2: div.content-rating içinde
        print("\n[TEST 2] div.content-rating içinde")
        content_rating = soup.select_one('div.content-rating')
        if content_rating:
            print(f"  [OK] div.content-rating bulundu")
            print(f"  [HTML] {str(content_rating)[:200]}")
            rating_in_content = content_rating.select_one('span.rating-value')
            if rating_in_content:
                print(f"  [OK] İçinde span.rating-value bulundu: {rating_in_content.text.strip()}")
            else:
                print("  [HATA] İçinde span.rating-value bulunamadı!")
        else:
            print("  [HATA] div.content-rating bulunamadı!")
        
        # Test 3: span.dt_rating_vgs (fallback)
        print("\n[TEST 3] span.dt_rating_vgs (fallback)")
        rating_dt = soup.select_one('span.dt_rating_vgs')
        if rating_dt:
            print(f"  [OK] Bulundu: {rating_dt.text.strip()}")
        else:
            print("  [HATA] Bulunamadı!")
        
        # Test 4: span.rating (fallback)
        print("\n[TEST 4] span.rating (fallback)")
        rating_span = soup.select_one('span.rating')
        if rating_span:
            print(f"  [OK] Bulundu: {rating_span.text.strip()}")
        else:
            print("  [HATA] Bulunamadı!")
        
        # Test 5: Tüm rating span'leri
        print("\n[TEST 5] Tüm rating ile ilgili span'ler")
        all_ratings = soup.select('span[class*="rating"], span[class*="Rating"]')
        print(f"  [OK] {len(all_ratings)} adet bulundu")
        for i, rating_span in enumerate(all_ratings[:10], 1):
            print(f"    {i}. class='{rating_span.get('class')}' -> text: '{rating_span.text.strip()}'")
        
        print("\n[OK] Test tamamlandi!")
        
    except Exception as e:
        print(f"[HATA] Test basarisiz: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_rating()

