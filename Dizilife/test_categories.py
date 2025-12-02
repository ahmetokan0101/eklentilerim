import requests
from bs4 import BeautifulSoup

categories = [
    ("komedi", "Komedi"),
    ("dram", "Dram"),
    ("bilim-kurgu", "Bilim Kurgu"),
    ("fantastik", "Fantastik"),
    ("korku", "Korku"),
]

for cat_slug, cat_name in categories:
    url = f"https://dizi25.life/tur/{cat_slug}/"
    r = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
    soup = BeautifulSoup(r.text, 'html.parser')
    cards = soup.select('div.content-card')
    print(f"{cat_name}: {len(cards)} icerik bulundu")
    if cards:
        first = cards[0].select_one("h3.card-title")
        if first:
            print(f"  Ilk icerik: {first.text.strip()}")
    print()

