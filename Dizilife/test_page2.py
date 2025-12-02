import requests
from bs4 import BeautifulSoup

url = "https://dizi25.life/tur/aksiyon?page=2&sort=popularity"
print(f"Test URL: {url}\n")

r = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
soup = BeautifulSoup(r.text, 'html.parser')

cards = soup.select('div.content-card')
print(f"Sayfa 2 - Toplam {len(cards)} card bulundu\n")

if cards:
    print("Ilk 5 icerik:")
    for i, card in enumerate(cards[:5], 1):
        title = card.select_one("h3.card-title")
        data_url = card.get("data-url")
        if title and data_url:
            print(f"{i}. {title.text.strip()}")
            print(f"   URL: {data_url}")
else:
    print("Hic card bulunamadi!")

