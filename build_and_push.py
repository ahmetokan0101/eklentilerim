import subprocess
import os
import sys
from pathlib import Path

# Renk kodları
GREEN = '\033[92m'
YELLOW = '\033[93m'
RED = '\033[91m'
BLUE = '\033[94m'
RESET = '\033[0m'

def print_status(message, color=BLUE):
    """Renkli status mesajı yazdır"""
    print(f"{color}[*] {message}{RESET}")

def print_success(message):
    """Başarı mesajı yazdır"""
    print(f"{GREEN}[✓] {message}{RESET}")

def print_error(message):
    """Hata mesajı yazdır"""
    print(f"{RED}[✗] {message}{RESET}")

def print_warning(message):
    """Uyarı mesajı yazdır"""
    print(f"{YELLOW}[!] {message}{RESET}")

def run_command(command, cwd=None, check=True):
    """Komut çalıştır ve sonucu döndür"""
    try:
        print_status(f"Çalıştırılıyor: {command}")
        result = subprocess.run(
            command,
            shell=True,
            cwd=cwd or os.getcwd(),
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='ignore'
        )
        
        if result.stdout:
            print(result.stdout)
        if result.stderr:
            print(result.stderr)
            
        if check and result.returncode != 0:
            print_error(f"Komut başarısız: {command}")
            print_error(f"Exit code: {result.returncode}")
            return False
            
        return True
    except Exception as e:
        print_error(f"Komut çalıştırma hatası: {e}")
        return False

def check_file_exists(file_path):
    """Dosyanın varlığını kontrol et"""
    return Path(file_path).exists()

def main():
    """Ana fonksiyon"""
    print(f"\n{BLUE}{'='*60}{RESET}")
    print(f"{BLUE}DiziPal Build ve GitHub Push Script{RESET}")
    print(f"{BLUE}{'='*60}{RESET}\n")
    
    # Mevcut dizini al
    script_dir = Path(__file__).parent.absolute()
    os.chdir(script_dir)
    
    print_status(f"Çalışma dizini: {script_dir}")
    
    # 1. Build al
    print_status("ADIM 1: Build alınıyor...", YELLOW)
    if not run_command(".\\gradlew.bat :DiziPal:make"):
        print_error("Build başarısız!")
        sys.exit(1)
    
    build_file = "DiziPal\\build\\DiziPal.cs3"
    if not check_file_exists(build_file):
        print_error(f"Build dosyası bulunamadı: {build_file}")
        sys.exit(1)
    
    print_success("Build başarılı!")
    
    # 2. Dosyayı kopyala
    print_status("ADIM 2: Dosya kopyalanıyor...", YELLOW)
    dest_file = "DiziPal.cs3"
    
    try:
        import shutil
        shutil.copy2(build_file, dest_file)
        if check_file_exists(dest_file):
            print_success(f"Dosya kopyalandı: {dest_file}")
        else:
            print_error(f"Dosya kopyalanamadı: {dest_file}")
            sys.exit(1)
    except Exception as e:
        print_error(f"Dosya kopyalama hatası: {e}")
        sys.exit(1)
    
    # 3. Git add
    print_status("ADIM 3: Git'e ekleniyor...", YELLOW)
    if not run_command(f"git add {dest_file}"):
        print_warning("Git add başarısız, devam ediliyor...")
    
    # 4. Git commit
    print_status("ADIM 4: Git commit yapılıyor...", YELLOW)
    commit_message = "Update DiziPal.cs3 build"
    if not run_command(f'git commit -m "{commit_message}"', check=False):
        print_warning("Commit başarısız (dosya değişmemiş olabilir)")
    
    # 5. Git push
    print_status("ADIM 5: GitHub'a gönderiliyor...", YELLOW)
    if not run_command("git push", check=False):
        print_warning("Push başarısız (zaten güncel olabilir)")
    
    print(f"\n{GREEN}{'='*60}{RESET}")
    print_success("Tüm işlemler tamamlandı! 🚀")
    print(f"{GREEN}{'='*60}{RESET}\n")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print_error("\n\nİşlem kullanıcı tarafından iptal edildi.")
        sys.exit(1)
    except Exception as e:
        print_error(f"\n\nBeklenmeyen hata: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

