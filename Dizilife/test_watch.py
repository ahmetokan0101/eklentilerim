import requests
from bs4 import BeautifulSoup
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

def test_watch_page():
    """Watch sayfasını test et"""
    # Örnek episode URL'si
    test_urls = [
        "https://dizi25.life/dizi/game-of-thrones/sezon-1/bolum-1",
        "https://dizi25.life/dizi/game-of-thrones",
        "https://dizi25.life/dizi/game-of-thrones/sezon-1/bolum-2",
    ]
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    
    for url in test_urls:
        print(f"\n[TEST] URL: {url}")
        try:
            response = requests.get(url, headers=headers, timeout=10)
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # İframe'leri bul
                iframes = soup.select("iframe")
                print(f"[OK] {len(iframes)} iframe bulundu")
                for i, iframe in enumerate(iframes, 1):
                    src = iframe.get("src") or iframe.get("data-src")
                    print(f"   {i}. src: {src}")
                    print(f"      data-src: {iframe.get('data-src')}")
                    print(f"      id: {iframe.get('id')}")
                    print(f"      class: {iframe.get('class')}")
                
                # iframe-container içindeki iframe'leri bul
                containers = soup.select("div.iframe-container, div.video-player, div.player, div#player")
                print(f"[OK] {len(containers)} video container bulundu")
                for container in containers:
                    container_iframes = container.select("iframe")
                    print(f"   Container içinde {len(container_iframes)} iframe")
                    for iframe in container_iframes:
                        src = iframe.get("src") or iframe.get("data-src")
                        print(f"      iframe src: {src}")
                
                # videoIframe ID'li iframe'i bul
                video_iframe = soup.select_one("#videoIframe")
                if video_iframe:
                    print(f"[OK] videoIframe ID'li iframe bulundu")
                    print(f"   src: {video_iframe.get('src')}")
                    print(f"   data-src: {video_iframe.get('data-src')}")
                
                # Script içinde iframe URL'leri ara
                scripts = soup.select("script")
                for script in scripts:
                    if script.string:
                        if "iframe" in script.string.lower() or "video" in script.string.lower():
                            # İlk 500 karakteri göster
                            content = script.string[:500]
                            if "iframe" in content.lower() or "src" in content.lower():
                                print(f"[SCRIPT] Script içinde iframe/video referansı bulundu")
                                print(f"   İlk 500 karakter: {content}")
            else:
                print(f"[ERROR] Status code: {response.status_code}")
        except Exception as e:
            print(f"[ERROR] {e}")

if __name__ == "__main__":
    test_watch_page()

