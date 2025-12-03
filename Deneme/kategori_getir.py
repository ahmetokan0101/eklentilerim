#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizipal'dan kategori içeriklerini çekme scripti (Genel)
Kategori ID'sini parametre olarak alır
"""

import requests
import json
import sys
from bs4 import BeautifulSoup

# Kategori ID'leri (Action/35 hariç)
KATEGORILER = {
    'aile': {'id': '20', 'ad': 'Aile'},
    'aksiyon': {'id': '13', 'ad': 'Aksiyon'},
    'aksiyon-macera': {'id': '27', 'ad': 'Aksiyon & Macera'},
    'animasyon': {'id': '19', 'ad': 'Animasyon'},
    'belgesel': {'id': '12', 'ad': 'Belgesel'},
    'bilim-kurgu': {'id': '17', 'ad': 'Bilim Kurgu'},
    'bilim-kurgu-fantazi': {'id': '28', 'ad': 'Bilim Kurgu & Fantazi'},
    'biyografi': {'id': '2', 'ad': 'Biyografi'},
    'cocuklar': {'id': '31', 'ad': 'Çocuklar'},
    'comedy': {'id': '33', 'ad': 'Comedy'},
    'dram': {'id': '4', 'ad': 'Dram'},
    'drama': {'id': '34', 'ad': 'Drama'},
    'fantastik': {'id': '21', 'ad': 'Fantastik'},
    'game-show': {'id': '9', 'ad': 'Game-Show'},
    'gerceklik': {'id': '26', 'ad': 'Gerçeklik'},
    'gerilim': {'id': '15', 'ad': 'Gerilim'},
    'gizem': {'id': '7', 'ad': 'Gizem'},
    'haberler': {'id': '24', 'ad': 'Haberler'},
    'kisa': {'id': '14', 'ad': 'Kısa'},
    'komedi': {'id': '3', 'ad': 'Komedi'},
    'korku': {'id': '22', 'ad': 'Korku'},
    'macera': {'id': '18', 'ad': 'Macera'},
    'muzik': {'id': '10', 'ad': 'Müzik'},
    'muzikal': {'id': '23', 'ad': 'Müzikal'},
    'reality-tv': {'id': '11', 'ad': 'Reality-TV'},
    'romance': {'id': '37', 'ad': 'Romance'},
    'romantik': {'id': '8', 'ad': 'Romantik'},
    'savas': {'id': '16', 'ad': 'Savaş'},
    'savas-politik': {'id': '30', 'ad': 'Savaş & Politik'},
    'spor': {'id': '25', 'ad': 'Spor'},
    'suc': {'id': '1', 'ad': 'Suç'},
    'talk': {'id': '32', 'ad': 'Talk'},
    'talk-show': {'id': '5', 'ad': 'Talk-Show'},
    'tarih': {'id': '6', 'ad': 'Tarih'},
    'thriller': {'id': '36', 'ad': 'Thriller'},
    'western': {'id': '29', 'ad': 'Western'},
}

def kategori_getir(kategori_id, kategori_adi, sayfa=1, sayfa_basina=30):
    """
    Dizipal'dan belirtilen kategorideki içerikleri çeker
    
    Args:
        kategori_id: Kategori ID'si (string)
        kategori_adi: Kategori adı (gösterim için)
        sayfa: Kaçıncı sayfa (varsayılan: 1)
        sayfa_basina: Sayfada kaç öğe gösterilecek (varsayılan: 30)
    
    Returns:
        list: Parse edilmiş dizi listesi
    """
    
    # API endpoint
    url = 'https://dizipal1515.com/bg/findseries'
    
    # POST isteği için gerekli veriler
    data = {
        'cKey': 'c61f91c5141d178450934fe81c0a2029',
        'cValue': 'MTc2NDcwMDgwMGFiNmI2ZDEzNDg1ZmE4MjQyZmU2YzRhNzc0OTE2NTM3NjQyMTU5Mjk2YTI4YTU0NjUyMjI2ZmVjMzFkYzBkMWQyMWY4YzdiNA==',
        'currentPage': str(sayfa),
        'currentPageCount': str(sayfa_basina),
        'categoryIdsComma': kategori_id,
        'imdbPointMin': '0',
        'imdbPointMax': '10',
        'releaseYearStart': '1923',
        'releaseYearEnd': '2024',
        'countryIdsComma': '',
        'orderType': 'date_desc',
        'yerliCountry': '9'
    }
    
    # HTTP header'ları
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://dizipal1515.com/',
        'Origin': 'https://dizipal1515.com',
        'Accept': 'application/json, text/javascript, */*; q=0.01',
        'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7'
    }
    
    try:
        response = requests.post(url, data=data, headers=headers, timeout=30)
        
        if response.status_code == 200:
            try:
                result = response.json()
                
                if result.get('state') == True or result.get('data'):
                    html_content = result.get('data', {}).get('html', '')
                    
                    if html_content:
                        diziler = parse_html(html_content)
                        
                        if diziler:
                            print(json.dumps(diziler, ensure_ascii=False, indent=2))
                            return diziler
                        else:
                            return []
                    else:
                        return []
                        
                else:
                    return []
                    
            except json.JSONDecodeError:
                return []
        else:
            return []
            
    except Exception as e:
        return []


def parse_html(html_content):
    """HTML içeriğinden dizilerin bilgilerini çıkarır"""
    if not html_content:
        return []
    
    try:
        soup = BeautifulSoup(html_content, 'html.parser')
        diziler = []
        
        kartlar = soup.find_all('div', class_='bg-[#22232a]')
        
        for kart in kartlar:
            try:
                link_elem = kart.find('a', href=True)
                if not link_elem:
                    continue
                
                href = link_elem.get('href', '')
                title_attr = link_elem.get('title', '')
                
                baslik_elem = kart.find('h2')
                baslik = baslik_elem.get_text(strip=True) if baslik_elem else ''
                if not baslik and title_attr:
                    baslik = title_attr.replace(' izle', '').strip()
                
                yil_elem = kart.find('span', class_='text-white text-sm')
                yil = yil_elem.get_text(strip=True) if yil_elem else ''
                
                imdb_puan = None
                imdb_elem = kart.find('h4', class_='text-sm text-white font-bold')
                if imdb_elem:
                    imdb_text = imdb_elem.get_text(strip=True)
                    try:
                        imdb_puan = float(imdb_text)
                    except ValueError:
                        pass
                
                img_elem = kart.find('img')
                resim_url = ''
                if img_elem:
                    resim_url = img_elem.get('data-src', '') or img_elem.get('src', '')
                
                dizi_info = {
                    'baslik': baslik,
                    'link': href,
                    'yil': yil,
                    'imdb_puani': imdb_puan,
                    'resim_url': resim_url,
                    'tam_link': href if href.startswith('http') else f'https://dizipal1515.com{href}'
                }
                
                diziler.append(dizi_info)
                
            except Exception:
                continue
        
        return diziler
        
    except Exception:
        return []


def main():
    if len(sys.argv) < 2:
        print("Kullanim: python kategori_getir.py <kategori-adi> [sayfa] [sayfa-basina]")
        print("\nMevcut kategoriler:")
        for key, value in KATEGORILER.items():
            print(f"  {key}: {value['ad']}")
        sys.exit(1)
    
    kategori_key = sys.argv[1].lower()
    
    if kategori_key not in KATEGORILER:
        print(f"[HATA] Gecersiz kategori: {kategori_key}")
        print("\nMevcut kategoriler:")
        for key, value in KATEGORILER.items():
            print(f"  {key}: {value['ad']}")
        sys.exit(1)
    
    kategori = KATEGORILER[kategori_key]
    
    sayfa = 1
    if len(sys.argv) > 2:
        try:
            sayfa = int(sys.argv[2])
        except ValueError:
            sayfa = 1
    
    sayfa_basina = 30
    if len(sys.argv) > 3:
        try:
            sayfa_basina = int(sys.argv[3])
        except ValueError:
            sayfa_basina = 30
    
    result = kategori_getir(kategori['id'], kategori['ad'], sayfa, sayfa_basina)
    
    if not result:
        sys.exit(1)


if __name__ == '__main__':
    try:
        import requests
    except ImportError:
        print("[HATA] 'requests' kutuphanesi bulunamadi!")
        print("[*] Yuklemek icin: pip install requests")
        sys.exit(1)
    
    try:
        from bs4 import BeautifulSoup
    except ImportError:
        print("[HATA] 'beautifulsoup4' kutuphanesi bulunamadi!")
        print("[*] Yuklemek icin: pip install beautifulsoup4")
        sys.exit(1)
    
    main()

