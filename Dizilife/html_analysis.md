# HTML Yapısı Analizi - dizi25.life

## Ana Sayfa Kategorileri Yapısı

### İçerik Kartları
- **Selector**: `div.content-card`
- **Container**: `div.content-row.list-grid` veya `section.content-listing-section`

### Kart İçeriği:
```html
<div class="content-card" data-url="/dizi/game-of-thrones">
    <div class="card-image">
        <img src="/assets/images/..." alt="Game of Thrones">
        <div class="card-overlay"></div>
        <div class="card-badge">Dizi</div>  <!-- veya "Film" -->
        <div class="card-rating">
            <i class="fas fa-star"></i>
            9.2
        </div>
    </div>
    <div class="card-info">
        <h3 class="card-title">Game of Thrones</h3>
        <div class="card-meta">
            <span>2011</span>  <!-- Yıl -->
            <span>Aksiyon</span>  <!-- Kategori -->
        </div>
    </div>
</div>
```

### Önemli Selector'lar:
- **URL**: `div.content-card[data-url]` - `data-url` attribute
- **Başlık**: `h3.card-title` veya `div.card-info h3.card-title`
- **Poster**: `div.card-image img` - `src` attribute
- **Yıl**: `div.card-meta span:first-child`
- **Kategori**: `div.card-meta span:last-child`
- **Rating**: `div.card-rating` (içindeki text)
- **Tip (Dizi/Film)**: `div.card-badge`

### Sayfalama:
- **Selector**: `ul.pagination li.page-item a.page-link`
- **Format**: `?page=2&sort=popularity`

### Arama:
- Arama sonuçları muhtemelen aynı `div.content-card` yapısını kullanıyor

