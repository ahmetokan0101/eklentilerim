# 🎬 Dizipal Aksiyon İçerikleri Çekme

Dizipal sitesinden Aksiyon kategorisindeki dizileri çekmek için Python scripti.

## 📦 Kurulum

```bash
pip install requests
```

## 🚀 Kullanım

### Temel Kullanım (Varsayılan - 1. sayfa, 30 öğe)
```bash
python aksiyon_getir.py
```

### Özel Sayfa
```bash
# 2. sayfayı çek
python aksiyon_getir.py 2
```

### Özel Sayfa + Öğe Sayısı
```bash
# 3. sayfa, sayfada 50 öğe
python aksiyon_getir.py 3 50
```

## 📁 Çıktı

Script çalıştığında:
- HTML dosyası oluşturulur: `aksiyon_diziler_sayfa_1_20241215_143022.html`
- Konsola önizleme gösterilir

## 📝 Özellikler

- ✅ Tek dosya, bağımsız çalışır
- ✅ Otomatik HTML kaydetme
- ✅ Hata yönetimi
- ✅ Önizleme gösterimi
- ✅ Sayfalama desteği

## 🔧 Özelleştirme

Script içinde değiştirebileceğin parametreler:
- `imdbPointMin/Max`: IMDb puanı aralığı
- `releaseYearStart/End`: Yıl aralığı
- `orderType`: Sıralama tipi

