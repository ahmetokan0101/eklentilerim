# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import requests
from bs4 import BeautifulSoup
import re
from urllib.parse import urljoin, urlparse
from typing import List, Dict, Optional
import sys
import io

# Windows terminal encoding sorunu için
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

class DizilifeTester:
    def __init__(self):
        self.main_url = "https://dizi25.life"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
        })
        
        self.main_pages = {
            f"{self.main_url}/tur/aksiyon/": "Aksiyon",
            f"{self.main_url}/tur/komedi/": "Komedi",
            f"{self.main_url}/tur/dram/": "Dram",
            f"{self.main_url}/tur/bilim-kurgu/": "Bilim Kurgu",
            f"{self.main_url}/tur/fantastik/": "Fantastik",
            f"{self.main_url}/tur/korku/": "Korku",
            f"{self.main_url}/tur/romantik/": "Romantik",
            f"{self.main_url}/tur/gerilim/": "Gerilim",
            f"{self.main_url}/tur/belgesel/": "Belgesel",
            f"{self.main_url}/tur/anime/": "Anime",
            f"{self.main_url}/tur/macera/": "Macera",
            f"{self.main_url}/tur/suc/": "Suç",
            f"{self.main_url}/tur/gizem/": "Gizem",
            f"{self.main_url}/tur/tarih/": "Tarih",
            f"{self.main_url}/tur/biyografi/": "Biyografi",
            f"{self.main_url}/tur/aile/": "Aile",
            f"{self.main_url}/tur/muzikal/": "Müzikal",
            f"{self.main_url}/tur/savas/": "Savaş",
            f"{self.main_url}/tur/western/": "Western",
            f"{self.main_url}/tur/spor/": "Spor",
            f"{self.main_url}/tur/gerceklik/": "Gerçeklik",
        }
    
    def fix_url(self, url: Optional[str]) -> Optional[str]:
        """URL'i düzelt ve tam URL yap"""
        if not url:
            return None
        if url.startswith("http"):
            return url
        if url.startswith("//"):
            return "https:" + url
        if url.startswith("/"):
            return self.main_url + url
        return urljoin(self.main_url, url)
    
    def get_page(self, url: str) -> BeautifulSoup:
        """Sayfayı getir ve parse et"""
        try:
            response = self.session.get(url, timeout=10)
            response.raise_for_status()
            return BeautifulSoup(response.text, 'html.parser')
        except Exception as e:
            print(f"[X] Hata: {url} - {e}")
            return BeautifulSoup("", 'html.parser')
    
    def get_main_page(self, category_url: str, page: int = 1) -> List[Dict]:
        """Ana sayfa içeriklerini getir"""
        url = f"{category_url}?page={page}&sort=popularity" if page > 1 else category_url
        print(f"\n[KATEGORI] {self.main_pages.get(category_url, 'Bilinmeyen')} - Sayfa {page}")
        print(f"[URL] {url}")
        
        doc = self.get_page(url)
        cards = doc.select("div.content-card")
        
        results = []
        for card in cards:
            result = self.to_main_page_result(card)
            if result:
                results.append(result)
        
        print(f"[OK] {len(results)} icerik bulundu\n")
        return results
    
    def to_main_page_result(self, card) -> Optional[Dict]:
        """Content card elementinden sonuç çıkar"""
        try:
            # URL - data-url attribute'undan
            data_url = card.get("data-url")
            href = self.fix_url(data_url) if data_url else None
            
            # Başlık - h3.card-title
            title_elem = card.select_one("h3.card-title")
            if not title_elem:
                title_elem = card.select_one("div.card-info h3")
            title = title_elem.text.strip() if title_elem else None
            
            # Poster - div.card-image img
            img_elem = card.select_one("div.card-image img")
            poster_url = None
            if img_elem:
                poster_url = (img_elem.get("data-src") or 
                             img_elem.get("data-lazy-src") or 
                             img_elem.get("src"))
                poster_url = self.fix_url(poster_url)
            
            if not title or not href:
                return None
            
            return {
                "title": title,
                "url": href,
                "poster": poster_url
            }
        except Exception as e:
            print(f"[!] Parse hatasi: {e}")
            return None
    
    def search(self, query: str) -> List[Dict]:
        """Arama yap"""
        url = f"{self.main_url}/?s={query}"
        print(f"\n[ARAMA] '{query}'")
        print(f"[URL] {url}")
        
        doc = self.get_page(url)
        # Önce content-card yapısını dene
        cards = doc.select("div.content-card")
        
        results = []
        if cards:
            for card in cards:
                result = self.to_main_page_result(card)
                if result:
                    results.append(result)
        else:
            # Alternatif yapı
            articles = doc.select("div.result-item article, article")
            for article in articles:
                result = self.to_search_result(article)
                if result:
                    results.append(result)
        
        print(f"[OK] {len(results)} sonuc bulundu\n")
        return results
    
    def to_search_result(self, article) -> Optional[Dict]:
        """Arama sonucu elementinden veri çıkar"""
        try:
            title_elem = article.select_one("div.title a")
            title = title_elem.text.strip() if title_elem else None
            href = self.fix_url(title_elem.get("href")) if title_elem else None
            
            img_elem = article.select_one("img")
            poster_url = None
            if img_elem:
                poster_url = (img_elem.get("data-src") or 
                             img_elem.get("data-lazy-src") or 
                             img_elem.get("src"))
                poster_url = self.fix_url(poster_url)
            
            if not title or not href:
                return None
            
            return {
                "title": title,
                "url": href,
                "poster": poster_url
            }
        except Exception as e:
            print(f"[!] Parse hatasi: {e}")
            return None
    
    def load_movie(self, url: str) -> Optional[Dict]:
        """Film detaylarını yükle"""
        print(f"\n[FILM] Film Detaylari")
        print(f"[URL] {url}")
        
        doc = self.get_page(url)
        
        # Başlık
        title = (doc.select_one("h1") and doc.select_one("h1").text.strip()) or \
                (doc.select_one("meta[property='og:title']") and 
                 doc.select_one("meta[property='og:title']").get("content", "").strip())
        
        if not title:
            print("[X] Baslik bulunamadi!")
            return None
        
        # Poster
        poster = None
        poster_img = doc.select_one("div.poster img")
        if poster_img:
            poster = (poster_img.get("data-src") or 
                     poster_img.get("data-lazy-src") or 
                     poster_img.get("src"))
        if not poster:
            og_image = doc.select_one("meta[property='og:image']")
            poster = og_image.get("content") if og_image else None
        poster = self.fix_url(poster)
        
        # Açıklama
        description = None
        wp_content = doc.select_one("div.wp-content p")
        if wp_content:
            description = wp_content.text.strip()
        if not description:
            og_desc = doc.select_one("meta[property='og:description']")
            if og_desc:
                description = og_desc.get("content", "").strip()
        if not description:
            desc_elem = doc.select_one("div.description")
            if desc_elem:
                description = desc_elem.text.strip()
        
        # Yıl
        year = None
        year_elem = doc.select_one("div.extra span.C a")
        if year_elem:
            try:
                year = int(year_elem.text.strip())
            except:
                pass
        if not year:
            extra_elem = doc.select_one("div.extra")
            if extra_elem:
                year_match = re.search(r'(\d{4})', extra_elem.text)
                if year_match:
                    try:
                        year = int(year_match.group(1))
                    except:
                        pass
        
        # Etiketler
        tags = []
        tag_elems = doc.select("div.sgeneros a")
        if tag_elems:
            tags = [tag.text.strip() for tag in tag_elems if tag.text.strip()]
        if not tags:
            tag_elems = doc.select("a[href*='/tur/']")
            tags = [tag.text.strip() for tag in tag_elems if tag.text.strip()]
        
        # Rating
        rating = None
        rating_elem = doc.select_one("span.dt_rating_vgs")
        if rating_elem:
            rating_text = rating_elem.text.strip()
            # Basit rating parse (örn: "8.5" -> 8.5)
            try:
                rating = float(rating_text)
            except:
                pass
        
        # Süre
        duration = None
        runtime_elem = doc.select_one("span.runtime")
        if runtime_elem:
            runtime_text = runtime_elem.text.split()[0] if runtime_elem.text else ""
            try:
                duration = int(runtime_text.strip())
            except:
                pass
        if not duration:
            duration_match = re.search(r'(\d+)\s*min', doc.text)
            if duration_match:
                try:
                    duration = int(duration_match.group(1))
                except:
                    pass
        
        # Oyuncular
        actors = []
        actor_elems = doc.select("span.valor a")
        if actor_elems:
            actors = [actor.text.strip() for actor in actor_elems if actor.text.strip()]
        if not actors:
            actor_elems = doc.select("div.cast a")
            actors = [actor.text.strip() for actor in actor_elems if actor.text.strip()]
        
        # Trailer
        trailer = None
        html_content = str(doc)
        trailer_match = re.search(r'embed/(.*)\?rel', html_content)
        if trailer_match:
            trailer = f"https://www.youtube.com/embed/{trailer_match.group(1)}"
        if not trailer:
            iframe = doc.select_one("iframe[src*='youtube']")
            if iframe:
                trailer = iframe.get("src")
        if not trailer:
            youtube_link = doc.select_one("a[href*='youtube.com']")
            if youtube_link:
                href = youtube_link.get("href", "")
                yt_match = re.search(r'(?:youtube\.com/watch\?v=|youtu\.be/|embed/)([^&\s]+)', href)
                if yt_match:
                    trailer = f"https://www.youtube.com/embed/{yt_match.group(1)}"
        
        # Öneriler
        recommendations = []
        rec_articles = doc.select("div.srelacionados article")
        if not rec_articles:
            rec_articles = doc.select("div.related article")
        
        for rec_article in rec_articles:
            rec_result = self.to_recommendation_result(rec_article)
            if rec_result:
                recommendations.append(rec_result)
        
        result = {
            "title": title,
            "poster": poster,
            "description": description,
            "year": year,
            "tags": tags,
            "rating": rating,
            "duration": duration,
            "actors": actors,
            "trailer": trailer,
            "recommendations": recommendations
        }
        
        print(f"[OK] Film yuklendi: {title}")
        return result
    
    def to_recommendation_result(self, article) -> Optional[Dict]:
        """Öneri elementinden veri çıkar"""
        try:
            img_elem = article.select_one("a img")
            title = None
            if img_elem:
                title = img_elem.get("alt", "").strip()
            if not title:
                link_elem = article.select_one("a")
                if link_elem:
                    title = link_elem.get("title", "").strip()
            
            link_elem = article.select_one("a")
            href = self.fix_url(link_elem.get("href")) if link_elem else None
            
            poster_url = None
            if img_elem:
                poster_url = (img_elem.get("data-src") or 
                             img_elem.get("data-lazy-src") or 
                             img_elem.get("src"))
                poster_url = self.fix_url(poster_url)
            
            if not title or not href:
                return None
            
            return {
                "title": title,
                "url": href,
                "poster": poster_url
            }
        except Exception as e:
            return None
    
    def load_links(self, url: str) -> List[str]:
        """Video linklerini bul"""
        print(f"\n[VIDEO] Video Linkleri")
        print(f"[URL] {url}")
        
        doc = self.get_page(url)
        iframes = []
        
        # İframe'leri bul
        for iframe in doc.select("iframe"):
            iframe_src = iframe.get("data-src") or iframe.get("src")
            if iframe_src:
                iframes.append(self.fix_url(iframe_src))
        
        # Eğer iframe yoksa, video player container'ı kontrol et
        if not iframes:
            video_container = doc.select_one("div.video-player, div.player, div#player")
            if video_container:
                iframe = video_container.select_one("iframe")
                if iframe:
                    iframe_src = iframe.get("data-src") or iframe.get("src")
                    if iframe_src:
                        iframes.append(self.fix_url(iframe_src))
        
        print(f"[OK] {len(iframes)} iframe bulundu")
        for i, iframe in enumerate(iframes, 1):
            print(f"   {i}. {iframe}")
        
        return iframes
    
    def print_result(self, result: Dict, title: str = ""):
        """Sonucu güzel yazdır"""
        if title:
            print(f"\n{'='*60}")
            print(f"[BILGI] {title}")
            print(f"{'='*60}")
        
        if isinstance(result, list):
            for i, item in enumerate(result, 1):
                print(f"\n{i}. {item.get('title', 'Başlıksız')}")
                if item.get('url'):
                    print(f"   [URL] {item['url']}")
                if item.get('poster'):
                    print(f"   [POSTER] {item['poster']}")
        else:
            for key, value in result.items():
                if value:
                    if key == "recommendations" and isinstance(value, list):
                        print(f"\n[ONERILER] Oneriler ({len(value)}):")
                        for i, rec in enumerate(value[:5], 1):  # İlk 5 öneri
                            print(f"   {i}. {rec.get('title', 'Başlıksız')}")
                    elif isinstance(value, list):
                        print(f"\n{key}: {', '.join(map(str, value))}")
                    else:
                        print(f"{key}: {value}")


def main():
    tester = DizilifeTester()
    
    print("="*60)
    print("[TEST] Dizilife Eklenti Test Scripti")
    print("="*60)
    
    # Test 1: Ana sayfa kategorileri
    print("\n" + "="*60)
    print("TEST 1: Ana Sayfa Kategorileri")
    print("="*60)
    
    for category_url, category_name in tester.main_pages.items():
        results = tester.get_main_page(category_url, page=1)
        if results:
            print(f"\n[KATEGORI] {category_name} Kategorisi - Ilk 3 Icerik:")
            for i, result in enumerate(results[:3], 1):
                print(f"   {i}. {result['title']}")
                print(f"      [URL] {result['url']}")
                if result.get('poster'):
                    print(f"      [POSTER] {result['poster']}")
        
        # Sayfa 2 testi
        print(f"\n[TEST] {category_name} - Sayfa 2 Testi:")
        results_page2 = tester.get_main_page(category_url, page=2)
        if results_page2:
            print(f"   Sayfa 2'den {len(results_page2)} icerik bulundu")
            print(f"   Ilk 3 icerik:")
            for i, result in enumerate(results_page2[:3], 1):
                print(f"      {i}. {result['title']}")
    
    # Test 2: Arama
    print("\n" + "="*60)
    print("TEST 2: Arama")
    print("="*60)
    
    search_results = tester.search("aksiyon")
    if search_results:
        print(f"\n[ARAMA] Arama Sonuclari - Ilk 3:")
        for i, result in enumerate(search_results[:3], 1):
            print(f"   {i}. {result['title']}")
            print(f"      [URL] {result['url']}")
            if result.get('poster'):
                print(f"      [POSTER] {result['poster']}")
    
    # Test 3: Film detayları (ilk kategoriden bir film)
    print("\n" + "="*60)
    print("TEST 3: Film Detayları")
    print("="*60)
    
    first_category_url = list(tester.main_pages.keys())[0]
    first_results = tester.get_main_page(first_category_url, page=1)
    if first_results:
        first_movie_url = first_results[0]['url']
        movie_details = tester.load_movie(first_movie_url)
        
        if movie_details:
            print(f"\n[FILM] Film: {movie_details['title']}")
            if movie_details.get('poster'):
                print(f"[POSTER] Poster: {movie_details['poster']}")
            if movie_details.get('description'):
                print(f"[ACIKLAMA] Aciklama: {movie_details['description'][:100]}...")
            if movie_details.get('year'):
                print(f"[YIL] Yil: {movie_details['year']}")
            if movie_details.get('tags'):
                print(f"[ETIKETLER] Etiketler: {', '.join(movie_details['tags'][:5])}")
            if movie_details.get('rating'):
                print(f"[RATING] Rating: {movie_details['rating']}")
            if movie_details.get('duration'):
                print(f"[SURE] Sure: {movie_details['duration']} dakika")
            if movie_details.get('actors'):
                print(f"[OYUNCULAR] Oyuncular: {', '.join(movie_details['actors'][:5])}")
            if movie_details.get('trailer'):
                print(f"[TRAILER] Trailer: {movie_details['trailer']}")
            if movie_details.get('recommendations'):
                print(f"[ONERILER] Oneriler: {len(movie_details['recommendations'])} adet")
            
            # Test 4: Video linkleri
            print("\n" + "="*60)
            print("TEST 4: Video Linkleri")
            print("="*60)
            
            video_links = tester.load_links(first_movie_url)
    
    print("\n" + "="*60)
    print("[OK] Testler Tamamlandi!")
    print("="*60)


if __name__ == "__main__":
    main()

