import requests
from bs4 import BeautifulSoup

url = "https://dizi25.life/tur/aksiyon/"
r = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
soup = BeautifulSoup(r.text, 'html.parser')

print("="*60)
print("HTML YAPISI ANALIZI")
print("="*60)

# Farklı selector'ları dene
selectors = [
    "div.items",
    "div.items article",
    ".items",
    "article",
    "[class*='item']",
    "[class*='film']",
    "[class*='movie']",
    "div[class*='card']",
    "div[class*='post']",
]

for selector in selectors:
    elements = soup.select(selector)
    print(f"\nSelector: {selector}")
    print(f"  Bulunan: {len(elements)}")
    if elements:
        first = elements[0]
        print(f"  Ilk element class: {first.get('class')}")
        print(f"  Ilk element tag: {first.name}")
        print(f"  Ilk 200 karakter: {str(first)[:200]}")

print("\n" + "="*60)
print("CONTENT CARD YAPISI")
print("="*60)
cards = soup.select('div.content-card')
print(f"Toplam {len(cards)} card bulundu")
if cards:
    card = cards[0]
    print("\nIlk card HTML:")
    print(str(card)[:800])
    print("\nCard icindeki elementler:")
    print(f"  - data-url: {card.get('data-url')}")
    img = card.select_one('img')
    if img:
        print(f"  - img src: {img.get('src')}")
        print(f"  - img alt: {img.get('alt')}")
    title_elem = card.select_one('.card-info, .card-title, h2, h3, a')
    if title_elem:
        print(f"  - title element: {title_elem.name}")
        print(f"  - title text: {title_elem.text.strip()[:50]}")
    link = card.select_one('a')
    if link:
        print(f"  - link href: {link.get('href')}")

