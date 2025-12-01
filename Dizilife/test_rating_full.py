# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import sys
sys.path.insert(0, '.')
from test_dizilife import DizilifeTester

tester = DizilifeTester()
result = tester.load_movie('https://dizi25.life/dizi/game-of-thrones')

if result:
    print(f"\n{'='*60}")
    print("[SONUC] Film Detaylari")
    print(f"{'='*60}")
    print(f"Title: {result.get('title', 'N/A')}")
    print(f"Year: {result.get('year', 'N/A')}")
    print(f"Rating: {result.get('rating', 'N/A')}")
    print(f"Tags: {result.get('tags', [])}")
    print(f"Description: {result.get('description', 'N/A')[:100]}...")
else:
    print("[HATA] Sonuc bulunamadi!")

