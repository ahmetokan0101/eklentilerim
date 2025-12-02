# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import requests
from bs4 import BeautifulSoup
import sys
import io

# Windows terminal encoding sorunu için
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

def test_episode_parsing():
    """Dizi detay sayfasından episode parsing testi"""
    url = "https://dizi25.life/dizi/breaking-bad"
    
    print("="*60)
    print("[TEST] Episode Parsing Testi")
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
        
        # Episode items bul
        episodes = soup.select('div.episode-item')
        print(f"[OK] {len(episodes)} episode-item bulundu\n")
        
        if len(episodes) == 0:
            print("[UYARI] Episode-item bulunamadi, alternatif selector'lar deneniyor...")
            # Alternatif selector'lar
            episodes = soup.select('div.episode, a[href*="/bolum"], a[href*="/episode"]')
            print(f"[OK] {len(episodes)} alternatif episode bulundu\n")
        
        # İlk 5 episode'u göster
        for i, ep in enumerate(episodes[:5], 1):
            ep_name = ep.select_one('div.name')
            ep_name_text = ep_name.text.strip() if ep_name else "N/A"
            
            ep_episode = ep.select_one('div.episode')
            ep_episode_text = ep_episode.text.strip() if ep_episode else "N/A"
            
            ep_href = ep.select_one('a')
            ep_href_url = ep_href.get('href') if ep_href else "N/A"
            
            ep_poster = ep.select_one('img')
            ep_poster_url = None
            if ep_poster:
                ep_poster_url = (ep_poster.get('data-src') or 
                               ep_poster.get('data-lazy-src') or 
                               ep_poster.get('src'))
            
            print(f"{i}. Episode Name: {ep_name_text[:50]}")
            print(f"   Episode Text: {ep_episode_text[:50]}")
            print(f"   Href: {ep_href_url[:80]}")
            if ep_poster_url:
                print(f"   Poster: {ep_poster_url[:80]}")
            
            # Episode ve Season numaralarını parse et
            if ep_episode_text:
                parts = ep_episode_text.split()
                season = None
                episode_num = None
                if len(parts) >= 3:
                    try:
                        season = int(parts[0].replace('.', ''))
                        episode_num = int(parts[2].replace('.', ''))
                    except:
                        pass
                print(f"   Parsed -> Season: {season}, Episode: {episode_num}")
            print()
        
        # Detay sayfası bilgilerini de göster
        print("\n" + "="*60)
        print("[DETAY] Dizi Detay Bilgileri")
        print("="*60)
        
        title = soup.select_one('h1')
        if title:
            print(f"Title: {title.text.strip()}")
        
        poster = soup.select_one('div.poster img')
        if poster:
            poster_url = (poster.get('data-src') or 
                         poster.get('data-lazy-src') or 
                         poster.get('src'))
            print(f"Poster: {poster_url}")
        
        description = soup.select_one('div.wp-content p')
        if description:
            print(f"Description: {description.text.strip()[:100]}...")
        
        year = soup.select_one('div.extra span.C a')
        if year:
            print(f"Year: {year.text.strip()}")
        
        rating = soup.select_one('span.dt_rating_vgs')
        if rating:
            print(f"Rating: {rating.text.strip()}")
        
        tags = soup.select('div.sgeneros a')
        if tags:
            tag_list = [tag.text.strip() for tag in tags]
            print(f"Tags: {', '.join(tag_list[:5])}")
        
        recommendations = soup.select('div.srelacionados article')
        if recommendations:
            print(f"\nRecommendations: {len(recommendations)} bulundu")
            for i, rec in enumerate(recommendations[:3], 1):
                rec_title = rec.select_one('h3.card-title')
                if rec_title:
                    print(f"   {i}. {rec_title.text.strip()}")
        
        print("\n[OK] Test tamamlandi!")
        
    except Exception as e:
        print(f"[HATA] Test basarisiz: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_episode_parsing()

