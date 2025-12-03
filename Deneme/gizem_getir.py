#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizipal'dan Gizem içeriklerini çekme scripti
"""

import requests
import json
import sys
from bs4 import BeautifulSoup

def gizem_getir(sayfa=1, sayfa_basina=30):
    """
    Dizipal'dan Gizem kategorisindeki içerikleri çeker
    
    Args:
        sayfa: Kaçıncı sayfa (varsayılan: 1)
        sayfa_basina: Sayfada kaç öğe gösterilecek (varsayılan: 30)
    
    Returns:
        list: Parse edilmiş dizi listesi
    """
    
    url = 'https://dizipal1515.com/bg/findseries'
    
    data = {
        'cKey': 'c61f91c5141d178450934fe81c0a2029',
        'cValue': 'MTc2NDcwMDgwMGFiNmI2ZDEzNDg1ZmE4MjQyZmU2YzRhNzc0OTE2NTM3NjQyMTU5Mjk2YTI4YTU0NjUyMjI2ZmVjMzFkYzBkMWQyMWY4YzdiNA==',
        'currentPage': str(sayfa),
        'currentPageCount': str(sayfa_basina),
        'categoryIdsComma': '7',
        'imdbPointMin': '0',
        'imdbPointMax': '10',
        'releaseYearStart': '1923',
        'releaseYearEnd': '2024',
        'countryIdsComma': '',
        'orderType': 'date_desc',
        'yerliCountry': '9'
    }
    
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
    sayfa = 1
    if len(sys.argv) > 1:
        try:
            sayfa = int(sys.argv[1])
        except ValueError:
            sayfa = 1
    
    sayfa_basina = 30
    if len(sys.argv) > 2:
        try:
            sayfa_basina = int(sys.argv[2])
        except ValueError:
            sayfa_basina = 30
    
    result = gizem_getir(sayfa=sayfa, sayfa_basina=sayfa_basina)
    
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
