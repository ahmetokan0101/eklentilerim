#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Dizipal'dan Aksiyon içeriklerini çekme scripti
"""

import requests
import json
import sys
import re
from datetime import datetime
from bs4 import BeautifulSoup

def aksiyon_getir(sayfa=1, sayfa_basina=30):
    """
    Dizipal'dan Aksiyon kategorisindeki içerikleri çeker
    
    Args:
        sayfa: Kaçıncı sayfa (varsayılan: 1)
        sayfa_basina: Sayfada kaç öğe gösterilecek (varsayılan: 30)
    
    Returns:
        dict: API yanıtı
    """
    
    # API endpoint
    url = 'https://dizipal1515.com/bg/findseries'
    
    # POST isteği için gerekli veriler
    # categoryIdsComma string formatında gönderilmeli (virgülle ayrılmış, örnek: "13" veya "13,27")
    data = {
        'cKey': 'c61f91c5141d178450934fe81c0a2029',
        'cValue': 'MTc2NDcwMDgwMGFiNmI2ZDEzNDg1ZmE4MjQyZmU2YzRhNzc0OTE2NTM3NjQyMTU5Mjk2YTI4YTU0NjUyMjI2ZmVjMzFkYzBkMWQyMWY4YzdiNA==',
        'currentPage': str(sayfa),
        'currentPageCount': str(sayfa_basina),
        'categoryIdsComma': '13',  # Virgülle ayrılmış string formatı - Aksiyon ID
        'imdbPointMin': '0',
        'imdbPointMax': '10',
        'releaseYearStart': '1923',
        'releaseYearEnd': '2024',
        'countryIdsComma': '',
        'orderType': 'date_desc',  # Yıla göre en yeni
        'yerliCountry': '9'  # Yabancı dizi
    }
    
    # HTTP header'ları (tarayıcı gibi görünmek için)
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://dizipal1515.com/',
        'Origin': 'https://dizipal1515.com',
        'Accept': 'application/json, text/javascript, */*; q=0.01',
        'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7'
    }
    
    try:
        # POST isteği gönder
        response = requests.post(url, data=data, headers=headers, timeout=30)
        
        # HTTP durum kodu kontrolü
        if response.status_code == 200:
            try:
                result = response.json()
                
                # Başarı kontrolü
                if result.get('state') == True or result.get('data'):
                    
                    # HTML içeriğini al
                    html_content = result.get('data', {}).get('html', '')
                    
                    if html_content:
                        # HTML'i parse et
                        diziler = parse_html(html_content)
                        
                        if diziler:
                            # JSON formatında terminale yazdır
                            print(json.dumps(diziler, ensure_ascii=False, indent=2))
                            return {'diziler': diziler, 'toplam': len(diziler)}
                        else:
                            print("[!] HTML parse edilemedi veya dizi bulunamadi")
                            return None
                    else:
                        print("[!] HTML icerigi bulunamadi")
                        print("Yanit:", json.dumps(result, indent=2, ensure_ascii=False))
                    
                    return result
                    
                else:
                    print("[HATA] API'den hata dondu")
                    print("Yanit:", json.dumps(result, indent=2, ensure_ascii=False))
                    return None
                    
            except json.JSONDecodeError:
                print("[HATA] JSON parse hatasi")
                print("Yanit (ilk 500 karakter):", response.text[:500])
                return None
        else:
            print(f"[HATA] HTTP Hatasi! Kod: {response.status_code}")
            print("Yanit:", response.text[:500])
            return None
            
    except requests.exceptions.Timeout:
        print("[HATA] Istek zaman asimina ugradi (30 saniye)")
        return None
    except requests.exceptions.ConnectionError:
        print("[HATA] Baglanti hatasi. Internet baglantinizi kontrol edin.")
        return None
    except Exception as e:
        print(f"[HATA] Beklenmeyen hata: {e}")
        return None


def parse_html(html_content):
    """
    HTML içeriğinden dizilerin bilgilerini çıkarır
    
    Args:
        html_content: HTML içeriği (string)
    
    Returns:
        list: Dizi bilgilerini içeren dict listesi
    """
    if not html_content:
        return []
    
    try:
        soup = BeautifulSoup(html_content, 'html.parser')
        diziler = []
        
        # Her bir dizi kartını bul
        kartlar = soup.find_all('div', class_='bg-[#22232a]')
        
        for kart in kartlar:
            try:
                # Link ve başlık bilgisi
                link_elem = kart.find('a', href=True)
                if not link_elem:
                    continue
                
                href = link_elem.get('href', '')
                title_attr = link_elem.get('title', '')
                
                # Başlık - önce title attribute, sonra h2 içeriği
                baslik_elem = kart.find('h2')
                baslik = baslik_elem.get_text(strip=True) if baslik_elem else ''
                if not baslik and title_attr:
                    # "X izle" formatından sadece başlığı al
                    baslik = title_attr.replace(' izle', '').strip()
                
                # Yıl bilgisi
                yil_elem = kart.find('span', class_='text-white text-sm')
                yil = yil_elem.get_text(strip=True) if yil_elem else ''
                
                # IMDb puanı
                imdb_puan = None
                imdb_elem = kart.find('h4', class_='text-sm text-white font-bold')
                if imdb_elem:
                    imdb_text = imdb_elem.get_text(strip=True)
                    try:
                        imdb_puan = float(imdb_text)
                    except ValueError:
                        pass
                
                # Resim URL'si
                img_elem = kart.find('img')
                resim_url = ''
                if img_elem:
                    resim_url = img_elem.get('data-src', '') or img_elem.get('src', '')
                
                # Dizi bilgisi oluştur
                dizi_info = {
                    'baslik': baslik,
                    'link': href,
                    'yil': yil,
                    'imdb_puani': imdb_puan,
                    'resim_url': resim_url,
                    'tam_link': href if href.startswith('http') else f'https://dizipal1515.com{href}'
                }
                
                diziler.append(dizi_info)
                
            except Exception as e:
                # Bir kart parse edilemezse devam et
                continue
        
        return diziler
        
    except Exception as e:
        print(f"[HATA] HTML parse hatasi: {e}")
        return []


def main():
    """
    Ana fonksiyon
    """
    
    # Kullanıcıdan sayfa bilgisi al (opsiyonel)
    if len(sys.argv) > 1:
        try:
            sayfa = int(sys.argv[1])
        except ValueError:
            print("[!] Gecersiz sayfa numarasi, varsayilan deger (1) kullaniliyor.")
            sayfa = 1
    else:
        sayfa = 1
    
    if len(sys.argv) > 2:
        try:
            sayfa_basina = int(sys.argv[2])
        except ValueError:
            print("[!] Gecersiz sayfa basina deger, varsayilan deger (30) kullaniliyor.")
            sayfa_basina = 30
    else:
        sayfa_basina = 30
    
    # İçerikleri çek ve JSON olarak göster
    result = aksiyon_getir(sayfa=sayfa, sayfa_basina=sayfa_basina)
    
    if not result:
        sys.exit(1)


if __name__ == '__main__':
    # Gerekli kütüphaneleri kontrol et
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

