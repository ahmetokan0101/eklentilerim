import base64
import json
import re
import requests
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import hashlib
import struct
import hmac
import subprocess
import sys
import os

try:
    import cloudscraper
    CLOUDSCRAPER_AVAILABLE = True
except ImportError:
    CLOUDSCRAPER_AVAILABLE = False

PASSPHRASE = "3hPn4uCjTVtfYWcjIcoJQ4cL1WWk1qxXI39egLYOmNv6IblA7eKJz68uU3eLzux1biZLCms0quEjTYniGv5z1JcKbNIsDQFSeIZOBZJz4is6pD7UyWDggWWzTLBQbHcQFpBQdClnuQaMNUHtLHTpzCvZy33p6I7wFBvL4fnXBYH84aUIyWGTRvM2G5cfoNf4705tO2kv"
DIZIPAL_DOMAIN = "https://dizipal1515.com"
SOURCE2_PATH = "/source2.php?v="


def hex_to_bytes(hex_string):
    return bytes.fromhex(hex_string)


def extract_encrypted_data_from_url(url):
    print("=" * 60)
    print("ADIM -1: Sayfadan şifreli veriyi çıkarma")
    print("=" * 60)
    print(f"[INFO] Sayfa yükleniyor: {url}")
    
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7'
        }
        
        if CLOUDSCRAPER_AVAILABLE:
            scraper = cloudscraper.create_scraper(
                browser={
                    'browser': 'chrome',
                    'platform': 'windows',
                    'desktop': True
                }
            )
            response = scraper.get(url, headers=headers, timeout=30, allow_redirects=True)
        else:
            response = requests.get(url, headers=headers, timeout=15, allow_redirects=True)
        
        html = response.text
        print(f"[OK] Sayfa yüklendi: {len(html)} chars (Status: {response.status_code})")
        
        match = re.search(r'data-rm-k=["\']?true["\']?[^>]*>([^<]+)', html)
        if not match:
            match = re.search(r'data-rm-k=["\']?true["\']?[^>]*>\s*({[^}]+})', html, re.DOTALL)
        
        if match:
            data_text = match.group(1).strip()
            data_text = data_text.replace('&quot;', '"').replace('&amp;', '&')
            
            try:
                encrypted_data = json.loads(data_text)
                print("[OK] Şifreli veri bulundu!")
                print(f" - ciphertext: {len(encrypted_data.get('ciphertext', ''))} chars")
                print(f" - iv: {len(encrypted_data.get('iv', ''))} chars")
                print(f" - salt: {len(encrypted_data.get('salt', ''))} chars")
                return encrypted_data
            except json.JSONDecodeError as e:
                print(f"[ERROR] JSON parse hatası: {e}")
                print(f"[DEBUG] Data text: {data_text[:200]}...")
                return None
        else:
            print("[ERROR] data-rm-k bulunamadı!")
            return None
            
    except Exception as e:
        print(f"[ERROR] Sayfa yükleme hatası: {e}")
        import traceback
        traceback.print_exc()
        return None


def decrypt_iframe_url(encrypted_data=None):
    print("=" * 60)
    print("ADIM 0: Şifreli veriyi çözme (Decrypt)")
    print("=" * 60)
    
    if not encrypted_data:
        print("[ERROR] Şifreli veri bulunamadı!")
        return None
    
    passphrase = PASSPHRASE
    print(f"[OK] Passphrase: {len(passphrase)} chars")
    
    try:
        salt_bytes = hex_to_bytes(encrypted_data["salt"])
        iv_bytes = hex_to_bytes(encrypted_data["iv"])
        ciphertext_bytes = base64.b64decode(encrypted_data["ciphertext"])
        
        print(f"[OK] Salt: {len(salt_bytes)} bytes")
        print(f"[OK] IV: {len(iv_bytes)} bytes")
        print(f"[OK] Ciphertext: {len(ciphertext_bytes)} bytes")
    except Exception as e:
        print(f"[ERROR] Hex/Base64 decode hatası: {e}")
        return None
    
    try:
        def prf(password, salt):
            return hmac.new(password, salt, hashlib.sha512).digest()
        
        dk = b''
        block_count = 1
        
        while len(dk) < 32:
            u = prf(passphrase.encode('utf-8'), salt_bytes + struct.pack('>I', block_count))
            t = u
            
            for i in range(998):
                u = prf(passphrase.encode('utf-8'), u)
                t = bytes(a ^ b for a, b in zip(t, u))
            
            dk += t
            block_count += 1
        
        key = dk[:32]
        print(f"[OK] Key derived (PBKDF2-SHA512, 999 iterations): {len(key)} bytes")
    except Exception as e:
        print(f"[ERROR] Key derivation hatası: {e}")
        import traceback
        traceback.print_exc()
        return None
    
    # AES-CBC decrypt
    try:
        cipher = AES.new(key, AES.MODE_CBC, iv_bytes)
        decrypted = cipher.decrypt(ciphertext_bytes)
        decrypted = unpad(decrypted, AES.block_size)
        iframe_url = decrypted.decode('utf-8').strip()
        
        if iframe_url.startswith('//'):
            iframe_url = 'https:' + iframe_url
        elif not iframe_url.startswith('http'):
            iframe_url = 'https://' + iframe_url
        
        print(f"[OK] iframe URL: {iframe_url}")
        return iframe_url
    except Exception as e:
        print(f"[ERROR] Decrypt hatası: {e}")
        import traceback
        traceback.print_exc()
        return None


def get_player_url(iframe_url):
    print("\n" + "=" * 60)
    print("ADIM 1: iframe.php sayfasını fetch etme (Cloudflare bypass)")
    print("=" * 60)
    
    try:
        headers = {
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
            'Referer': f'{DIZIPAL_DOMAIN}/',
            'Origin': DIZIPAL_DOMAIN,
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Sec-Fetch-Dest': 'iframe',
            'Sec-Fetch-Mode': 'navigate',
            'Sec-Fetch-Site': 'cross-site'
        }
        
        if CLOUDSCRAPER_AVAILABLE:
            print("[INFO] cloudscraper kullanılıyor (Java WebView mantığı)")
            scraper = cloudscraper.create_scraper(
                browser={
                    'browser': 'chrome',
                    'platform': 'windows',
                    'desktop': True
                }
            )
            response = scraper.get(iframe_url, headers=headers, timeout=30, allow_redirects=True)
        else:
            session = requests.Session()
            session.headers.update(headers)
            response = session.get(iframe_url, timeout=15, allow_redirects=True)
        
        html = response.text
        server_header = response.headers.get('Server', '').lower()
        status_code = response.status_code
        
        print(f"[OK] Sayfa yüklendi: {len(html)} chars (Status: {status_code}, Server: {server_header})")
        
        is_cloudflare = (
            "Just a moment" in html or
            "cf-browser-verification" in html or
            "challenges.cloudflare.com" in html or
            (server_header in ["cloudflare-nginx", "cloudflare"] and status_code in [403, 503])
        )
        
        if is_cloudflare and not CLOUDSCRAPER_AVAILABLE:
            print("[ERROR] Cloudflare koruması tespit edildi!")
            print("[INFO] Cloudflare bypass için: pip install cloudscraper")
            return None
        elif is_cloudflare and CLOUDSCRAPER_AVAILABLE:
            print("[WARN] Cloudflare tespit edildi, ama cloudscraper ile bypass edilmeye çalışılıyor...")
            # cloudscraper zaten bypass etmeye çalıştı, tekrar deneyelim
            if "Just a moment" in html:
                print("[ERROR] Cloudflare bypass başarısız, HTML hala challenge sayfası")
                return None
        
        print("\n" + "=" * 60)
        print("ADIM 2: window.openPlayer() regex ile player URL çıkarma")
        print("=" * 60)
        
        match = re.search(r"window\.openPlayer\('([^']+)'", html)
        if match:
            player_url = match.group(1)
            print(f"[OK] Player URL bulundu: {player_url}")
            return player_url
        else:
            print("[ERROR] window.openPlayer bulunamadı")
            alt_patterns = [
                r"openPlayer\(['\"]([^'\"]+)['\"]",
                r"player.*?['\"]([^'\"]+source2[^'\"]+)['\"]",
                r"source2\.php\?v=([a-zA-Z0-9]+)"
            ]
            return None
            
    except Exception as e:
        print(f"[ERROR] iframe fetch hatası: {e}")
        import traceback
        traceback.print_exc()
        return None


def get_m3u8_url(player_url, iframe_url):
    print("\n" + "=" * 60)
    print("ADIM 3: source2.php'ye istek atma (ContentX mantığı)")
    print("=" * 60)
    
    from urllib.parse import urlparse
    parsed = urlparse(iframe_url)
    main_url = f"{parsed.scheme}://{parsed.netloc}"
    source2_url = f"{main_url}{SOURCE2_PATH}{player_url}"
    
    print(f"[OK] mainUrl: {main_url}")
    print(f"[OK] player_url (iExtract): {player_url[:100]}..." if len(player_url) > 100 else f"[OK] player_url (iExtract): {player_url}")
    print(f"[OK] source2.php URL: {source2_url[:150]}..." if len(source2_url) > 150 else f"[OK] source2.php URL: {source2_url}")
    
    try:
        headers = {
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
            'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
            'Referer': iframe_url,
            'Origin': iframe_url.split('/')[0] + '//' + iframe_url.split('/')[2],
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Sec-Fetch-Dest': 'document',
            'Sec-Fetch-Mode': 'navigate',
            'Sec-Fetch-Site': 'cross-site'
        }
        
        if CLOUDSCRAPER_AVAILABLE:
            scraper = cloudscraper.create_scraper(
                browser={
                    'browser': 'chrome',
                    'platform': 'windows',
                    'desktop': True
                }
            )
            response = scraper.get(source2_url, headers=headers, timeout=30, allow_redirects=True)
        else:
            session = requests.Session()
            session.headers.update(headers)
            response = session.get(source2_url, timeout=15, allow_redirects=True)
        
        html = response.text
        server_header = response.headers.get('Server', '').lower()
        status_code = response.status_code
        
        print(f"[OK] Sayfa yüklendi: {len(html)} chars (Status: {status_code}, Server: {server_header})")
        
        # Cloudflare kontrolü
        is_cloudflare = (
            "Just a moment" in html or
            "cf-browser-verification" in html or
            (server_header in ["cloudflare-nginx", "cloudflare"] and status_code in [403, 503])
        )
        
        if is_cloudflare:
            print("[WARN] Cloudflare koruması tespit edildi!")
            if not CLOUDSCRAPER_AVAILABLE:
                print("[INFO] Cloudflare bypass için: pip install cloudscraper")
                return None
        
        print("\n" + "=" * 60)
        print("ADIM 4: M3U8 URL çıkarma (ContentX mantığı)")
        print("=" * 60)
        
        pattern = r'"file":"([^"]+)"'
        match = re.search(pattern, html)
        
        if match:
            m3u8_url = match.group(1)
            m3u8_url = m3u8_url.replace('\\', '')
            print(f"[OK] M3U8 URL bulundu (ContentX pattern): {m3u8_url}")
            return m3u8_url
        
        alt_patterns = [
            r'"file":"((?:\\\\"|[^"])+)"',
            r'"file"\s*:\s*"([^"]+)"',
            r'file["\']?\s*:\s*["\']([^"\']+)["\']',
        ]
        
        for alt_pattern in alt_patterns:
            alt_match = re.search(alt_pattern, html)
            if alt_match:
                m3u8_url = alt_match.group(1)
                m3u8_url = m3u8_url.replace('\\/', '/').replace('\\u0026', '&').replace('\\', '')
                print(f"[OK] M3U8 URL bulundu (alternatif pattern): {m3u8_url}")
                return m3u8_url
        
        print("[ERROR] file URL bulunamadı (tüm pattern'ler denendi)")
        
        m3u8_match = re.search(r'https?://[^\s"\'<>]+\.m3u8[^\s"\'<>]*', html)
        if m3u8_match:
            print(f"[INFO] Direkt M3U8 URL bulundu: {m3u8_match.group(0)}")
            return m3u8_match.group(0)
        
        return None
        
    except Exception as e:
        print(f"[ERROR] source2.php fetch hatası: {e}")
        import traceback
        traceback.print_exc()
        return None


def test_m3u8_url(m3u8_url, iframe_url):
    print("\n" + "=" * 60)
    print("ADIM 5: M3U8 URL test (Referer ile)")
    print("=" * 60)
    
    try:
        headers = {
            'Referer': iframe_url,
            'Accept': '*/*',
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        }
        
        if CLOUDSCRAPER_AVAILABLE:
            scraper = cloudscraper.create_scraper(
                browser={
                    'browser': 'chrome',
                    'platform': 'windows',
                    'desktop': True
                }
            )
            response = scraper.get(m3u8_url, headers=headers, timeout=30, allow_redirects=True)
        else:
            response = requests.get(m3u8_url, headers=headers, timeout=15, allow_redirects=True)
        
        content = response.text
        print(f"[OK] M3U8 fetch başarılı: {len(content)} chars (Status: {response.status_code})")
        
        if content.strip().startswith('#EXTM3U'):
            print("[OK] M3U8 format doğrulandı!")
            return True
        else:
            print("[WARN] M3U8 format değil!")
            return False
            
    except Exception as e:
        print(f"[ERROR] M3U8 test hatası: {e}")
        return False


def find_vlc():
    common_paths = [
        r"C:\Program Files\VideoLAN\VLC\vlc.exe",
        r"C:\Program Files (x86)\VideoLAN\VLC\vlc.exe",
        r"C:\ProgramData\Microsoft\Windows\Start Menu\Programs\VideoLAN\VLC media player.lnk",
    ]
    
    for path in common_paths:
        if os.path.exists(path):
            return path
    
    try:
        result = subprocess.run(['where', 'vlc'], capture_output=True, text=True, timeout=5)
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip().split('\n')[0]
    except:
        pass
    
    return None


def play_with_vlc(m3u8_url, iframe_url):
    vlc_path = find_vlc()
    if not vlc_path:
        print("\n[ERROR] VLC bulunamadı!")
        print("VLC'yi yükleyin: https://www.videolan.org/vlc/")
        print("Veya VLC tam yolunu girin (seçim 3)")
        return False
    
    print(f"\n[OK] VLC bulundu: {vlc_path}")
    print("[INFO] VLC başlatılıyor...")
    
    try:
        cmd = [
            vlc_path,
            '--http-referrer', iframe_url,
            m3u8_url
        ]
        subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print("[OK] VLC başlatıldı!")
        return True
    except Exception as e:
        print(f"[ERROR] VLC başlatılamadı: {e}")
        return False


def play_with_vlc_path(vlc_path, m3u8_url, iframe_url):
    if not os.path.exists(vlc_path):
        print(f"[ERROR] Dosya bulunamadı: {vlc_path}")
        return False
    
    print(f"\n[OK] VLC yolu: {vlc_path}")
    print("[INFO] VLC başlatılıyor...")
    
    try:
        cmd = [
            vlc_path,
            '--http-referrer', iframe_url,
            m3u8_url
        ]
        subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print("[OK] VLC başlatıldı!")
        return True
    except Exception as e:
        print(f"[ERROR] VLC başlatılamadı: {e}")
        return False


def main():
    try:
        url = None
        if len(sys.argv) > 1:
            url = sys.argv[1]
        else:
            url = input("\nDizipal sayfa URL'si (Enter = varsayılan test verisi): ").strip()
        
        if not url:
            print("[INFO] Varsayılan test verisi kullanılıyor...")
            encrypted_data = {
                "ciphertext": "CQiUb6ScR/b/VLr/uP1/xdovNUwBAYKuLob7WBLz0qUFGreWipiPuM85ppQ/08RLtG6/vr2s6nJPUn3uqCquZFyygwJgDTSf2mFXfWXNxYU=",
                "iv": "1f17efd9ae40e310d1e5113ac0112eb0",
                "salt": "1ca9a86a5558bb2ead4e4d9b31e8f5df89801f552152088195c84f2526e9c981ab3dc891684e53c8233ea174fb5e388cca3c577ad4db58a6a1ec810b410ace2514a6080e471ebd9a51ad337960168c0d2929042f8c5de7c9e0e247341b0271c99e7c695722ca5c24998b5de6dbcd9c2df19aa87aa693451313de9b12f9bc95d6347b16fda9c6bd8660d377b33ebc01ad4c2eb91a21871b04cbd7cddb7fa406ee68fc18e9a3629886a4e8b5a809c9e62c8d61eb6df86922193f313005a0c929353b60da1d0de5a9887faa0803ba50158f18656e8de6ba8ad8c5b9548ec26185442df36371587ca3aae6bb05dd306eafa12220de54c1b3d6e2f8964d0d133474d8"
            }
        else:
            encrypted_data = extract_encrypted_data_from_url(url)
        
        if not encrypted_data:
            print("\n[ERROR] Şifreli veri çıkarılamadı!")
            return None
        
        iframe_url = decrypt_iframe_url(encrypted_data)
        if not iframe_url:
            print("\n[ERROR] iframe URL bulunamadı")
            return None
        
        player_url = get_player_url(iframe_url)
        if not player_url:
            print("\n[ERROR] Player URL bulunamadı")
            return None
        
        m3u8_url = get_m3u8_url(player_url, iframe_url)
        if not m3u8_url:
            print("\n[ERROR] M3U8 URL bulunamadı")
            return None
        
        print("\n" + "=" * 60)
        print(">>> GERÇEK STREAM URL (M3U8) BULUNDU:")
        print("=" * 60)
        print(m3u8_url)
        print("=" * 60)
        
        test_m3u8_url(m3u8_url, iframe_url)
        
        print("\n[SUCCESS] Tüm adımlar tamamlandı!")
        
        print("\n" + "=" * 60)
        print("VLC İLE OYNATMA:")
        print("=" * 60)
        print("1. VLC ile otomatik oynat")
        print("2. VLC tam yol ile oynat")
        print("3. Çıkış")
        
        choice = input("\nSeçim (1-3): ").strip()
        
        if choice == "1":
            play_with_vlc(m3u8_url, iframe_url)
        elif choice == "2":
            vlc_path = input("VLC tam yolu (örnek: C:\\Program Files\\VideoLAN\\VLC\\vlc.exe): ").strip()
            if vlc_path:
                play_with_vlc_path(vlc_path, m3u8_url, iframe_url)
        
        return m3u8_url
        
    except ImportError as e:
        print("\n[ERROR] Gerekli kütüphane eksik!")
        print(" pip install pycryptodome requests cloudscraper")
        return None
    except Exception as e:
        print(f"\n[ERROR] Hata: {e}")
        import traceback
        traceback.print_exc()
        return None


if __name__ == "__main__":
    main()
