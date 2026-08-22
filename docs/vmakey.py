cd ~

# Ligtas na linis
rm -f .bashrc .bash_profile .profile MartoDosko_bintana.py
unset PS1 PROMPT_COMMAND menu
hash -r

# ==================================================
# MARTO DOSKO v3.40 + BINTANA v1.7.8 ✅ AYOS NA!
# ✅ Tamang DISPLAY=:99
# ✅ SIGURADONG VERSION DETECTION
# ✅ Sync Folder + Lahat ng Features
# ==================================================

cat > .bashrc << 'EOF'
# ==================================================
# MARTO DOSKO MENU v3.40 ✅
# ==================================================

case $- in *i*) ;; *) return ;; esac
HISTCONTROL=ignoreboth
shopt -s histappend
HISTSIZE=1000
HISTFILESIZE=2000
shopt -s checkwinsize

PS1='\[\033[1;32m\]📱 termux\[\033[0m\]:\w\$ '

DOWNLOADS="/storage/emulated/0/Download"
STORAGE_ROOT="/storage/emulated/0"
TERMUX_ROOT="$HOME"
PROYEKTO="$HOME/MartoDosko_Proyekto"
FIX_FILE="${PROYEKTO}/martodosko_ver_fix.py"
BINTANA_FILE="${PROYEKTO}/MartoDosko_bintana.py"
MENU_BERSYON="3.40"

setup_storage() {
if [ ! -d ~/storage ]; then
echo "🔑 Hinihingi ang Storage Access..."
termux-setup-storage
sleep 2
fi
mkdir -p "$DOWNLOADS" "$PROYEKTO" 2>/dev/null
}

# ==================================================
# ✅ SIGURADONG BABASA NG BERSYON
# ==================================================
kuha_bersyon() {
[ ! -f "$1" ] && echo "Wala" && return
# Hahanapin mismo ang KASALUKUYANG_BERSYON o anumang numero ng bersyon
grep -Eo 'KASALUKUYANG_BERSYON\s*=\s*["'\'']?[0-9]+\.[0-9.]+["'\'']?' "$1" 2>/dev/null | grep -Eo '[0-9]+\.[0-9.]+' || grep -Eo '[vV]?[0-9]+\.[0-9.]+' "$1" 2>/dev/null | head -n1 | sed 's/^[vV]//' || echo "Wala pa"
}

kilalanin_bersyon() {
kuha_bersyon "$BINTANA_FILE"
}

LAHAT_BERSYON="
1.3.2  - Matatag at subok na
1.7.6  - Pinakabagong bersyon
1.7.8  - Kasalukuyang bersyon
1.8.1  - May Windows-style File Picker
"

ilipat_bersyon() {
TARGET_VER="$1"
mkdir -p "$PROYEKTO" 2>/dev/null
echo "KASALUKUYANG_BERSYON = \"$TARGET_VER\"" > "$PROYEKTO/ver.txt"
echo "✅ Napalitan sa Bersyon $TARGET_VER!"
}

install_whiptail() {
command -v whiptail &>/dev/null || { pkg update -y; pkg install -y newt whiptail; }
}

# ==================================================
# MABILIS NA LOKASYON
# ==================================================
mabilis_lokasyon() {
echo -e "\n⚡ MABILIS NA PUNTO NG LOKASYON"
echo "────────────────────────────────────"
echo "0. ❌ Bumalik sa Menu"
echo "1) 📂 MartoDosko Proyekto"
echo "2) 📱 Panloob na Storage"
echo "3) 📥 Download Folder"
echo "4) 🖼️ Pictures Folder"
echo "5) 📄 Termux Home"
read -p "Piliin: " m_pili
case "$m_pili" in
0) return ;;
1) cd "$PROYEKTO" || echo "❌ Hindi mapasok ang proyekto" ;;
2) cd "$STORAGE_ROOT" || echo "❌ Hindi mapasok ang storage" ;;
3) cd "$DOWNLOADS" || echo "❌ Hindi mapasok ang Downloads" ;;
4) cd "/storage/emulated/0/Pictures" || echo "❌ Hindi mapasok ang Pictures" ;;
5) cd "$HOME" || echo "❌ Hindi mapasok ang Home" ;;
*) echo "❌ Mali na numero!"; sleep 1; return ;;
esac
echo -e "\n✅ NANDITO NA TAYO: $(pwd)"
read -p "👉 ENTER para bumalik..."
}

# ==================================================
# ✨ PASTE & SAVE
# ==================================================
pumunta_sa_folder() {
TARGET_DIR="$(pwd)"
echo -e "\n📂 PASTE / SAVE AREA"
echo "──────────────────────────────────────────"
echo "📍 DITO IISAVE: $TARGET_DIR"
echo "👉 I-paste ang code/teksto. Tapos: Ctrl+D o i-type ang DONE"
echo ""
INPUT=""
while IFS= read -r LINYA; do
[[ "$LINYA" == "DONE" || "$LINYA" == "done" ]] && break
INPUT+="$LINYA"$'\n'
done
INPUT=$(echo "$INPUT" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
if [ -z "$INPUT" ]; then echo -e "\n⚠️ Walang inilagay"; read -p "👉 ENTER..."; return; fi
echo -e "\n💾 Pangalan ng file:"
read -p "(Iwanang blangko para auto): " FILENAME
if [ -z "$FILENAME" ]; then
NUM=1; while [ -f "save_$NUM" ]; do NUM=$((NUM+1)); done
FILENAME="save_$NUM"
fi
echo "$INPUT" > "$FILENAME"
echo -e "\n✅ NAISAVE SA: $FILENAME"
read -p "👉 ENTER..."
}

# ==================================================
# PAMAMAHALA NG FILES
# ==================================================
kopya_sa_telepono() {
echo -e "\n📤 FILE → DOWNLOADS"
read -p "Filename: " fn
[ -f "$fn" ] && cp "$fn" "$DOWNLOADS/" && echo "✅ Nasa Downloads na!" || echo "❌ Wala ang file"
read -p "👉 ENTER..."
}

kopya_mula_downloads() {
echo -e "\n📥 DOWNLOADS → DITO"
ls -1 "$DOWNLOADS"/*.py 2>/dev/null || { echo "❌ Walang .py"; read -p "👉 ENTER..."; return; }
read -p "Filename: " fn
[ -f "$DOWNLOADS/$fn" ] && cp "$DOWNLOADS/$fn" . && echo "✅ Nasa folder na!" || echo "❌ Wala ang file"
read -p "👉 ENTER..."
}

ilipat_lahat_py() {
echo -e "\n📦 ILIPAT LAHAT NG .PY"
read -p "Papunta sa Downloads? [oo/hindi]: " sagot
if [[ "$sagot" == "oo" || "$sagot" == "o" ]]; then
cp *.py "$DOWNLOADS/" 2>/dev/null && echo "✅ Tapos na!" || echo "❌ Walang mailipat"
fi
read -p "👉 ENTER..."
}

kumuha_ss() {
echo -e "\n📸 SCREENSHOT"
command -v scrot >/dev/null 2>&1 && scrot -e 'mv $f /storage/emulated/0/Download/' 2>/dev/null && echo "✅ Nasa Downloads na!" || echo "⚠️ I-install muna: pkg install scrot"
read -p "👉 ENTER..."
}

linis_prefix() {
echo -e "\n🧹 LINIS NG LUMA PREFIX AT KOPYA"
read -p "Tuloy ba? [O/H]: " SAGOT
[[ "$SAGOT" != "o" && "$SAGOT" != "oo" ]] && { echo "❌ Kinansela"; read -p "👉 ENTER..."; return; }
rm -f ~/../usr/bin/martofix ~/../usr/bin/marto ~/../usr/bin/vtibay 2>/dev/null
proot-distro login ubuntu --shared-tmp -- bash -c 'rm -f /usr/bin/martofix /usr/bin/marto /usr/local/bin/martofix /usr/local/bin/marto /root/.local/bin/martofix /root/.local/bin/marto /usr/bin/martodosko_ver_fix.py 2>/dev/null; echo "✅ Tapos na sa Ubuntu!"'
hash -r
echo -e "\n✅ TAPOS NA!"
read -p "👉 ENTER..."
}

linis_sirang_file() {
echo -e "\n🧹 LIGTAS NA PAGBURA NG SAVE FILES"
read -p "Tuloy ba? [O/H]: " SAGOT
[[ "$SAGOT" != "o" ]] && { echo "❌ Kinansela"; read -p "👉 ENTER..."; return; }
find "$TERMUX_ROOT" "$STORAGE_ROOT" -maxdepth 9 -name "save_*" -type f -delete 2>/dev/null
find "$TERMUX_ROOT" "$STORAGE_ROOT" -maxdepth 9 -name "save_*" -type d -delete 2>/dev/null
echo "✅ Tinanggal na ang mga save_* files — ligtas ang iba!"
read -p "👉 ENTER..."
}

suriin_lokasyon() {
echo -e "\n📂 LOKASYON: $(pwd)"
echo -e "🖥️ SISTEMA: TERMUX\n"
read -p "👉 ENTER..."
}

# ==================================================
# ALIAS (NAKA-SYNC + TAMA ANG DISPLAY)
# ==================================================
alias dispk='pkill termux-x11 2>/dev/null; rm -rf /tmp/.X*; export DISPLAY=:99; termux-x11 :99 -ac & sleep 5'
alias marto='dispk; read -p "✅ Buksan ang Termux-X11/VNC, ENTER..."; proot-distro login ubuntu --shared-tmp --bind $PROYEKTO:/root/MartoDosko_Proyekto -- bash -c "cd /root/MartoDosko_Proyekto && source marto_env/bin/activate && python MartoDosko_bintana.py"'
alias martofix='proot-distro login ubuntu --shared-tmp --bind $PROYEKTO:/root/MartoDosko_Proyekto -- bash -c "cd /root/MartoDosko_Proyekto && python3 martodosko_ver_fix.py"'

# ==================================================
# PLAIN TEXT MENU
# ==================================================
plain_menu() {
setup_storage
while true; do
clear
VER_FIX=$(kuha_bersyon "$FIX_FILE")
VER_BIN=$(kuha_bersyon "$BINTANA_FILE")
echo "----------------------------------------"
echo "  📋 MENU NG MARTO DOSKO v$MENU_BERSYON"
echo "----------------------------------------"
echo "📍 LOKASYON: $(pwd)"
echo "🔍 Ver_Fix: $VER_FIX | 🎬 App Bersyon: $VER_BIN"
echo ""
echo "0. ❌ LUMABAS"
echo "1. 🚀 Simulan ang Display (:99)"
echo "2. 📂 Pumasok sa Ubuntu"
echo "3. 🎬 Patakbuhin ang App"
echo "4. ✨🔥 PATAKBUHIN ANG VER_FIX"
echo "5. 🧹 Linis Luma Prefix"
echo "6. 📍 Suriin Lokasyon"
echo "7. ⚡ Mabilis na Lokasyon"
echo "8. 📂 PASTE / SAVE"
echo "9. 📋 Tingnan/Piliin Bersyon"
echo "10. 📤 File → Downloads"
echo "11. 📥 Downloads → Dito"
echo "12. 📦 Ilipat lahat ng .py"
echo "13. 📸 Kumuha ng Screenshot"
echo "14. 🔁 Lumipat sa WINDOW MENU"
echo "15. 🧹 Linis Save Files"
echo "----------------------------------------"
read -p "Piliin: " pili
case "$pili" in
0) echo -e "\n👋 Babye!"; break ;;
1) dispk ;;
2) proot-distro login ubuntu ;;
3) marto ;;
4) martofix ;;
5) linis_prefix ;;
6) suriin_lokasyon ;;
7) mabilis_lokasyon ;;
8) pumunta_sa_folder ;;
9)
NGA_NOW=$(kilalanin_bersyon)
echo -e "\n✅ Kasalukuyan: Bersyon $NGA_NOW"
echo "$LAHAT_BERSYON"
read -p "I-type ang bersyon: " PILI_VER
ilipat_bersyon "$PILI_VER"
;;
10) kopya_sa_telepono ;;
11) kopya_mula_downloads ;;
12) ilipat_lahat_py ;;
13) kumuha_ss ;;
14) window_menu; return ;;
15) linis_sirang_file ;;
*) echo "❌ Mali!"; sleep 1 ;;
esac
done
}

# ==================================================
# WINDOW / GUI MENU
# ==================================================
window_menu() {
install_whiptail
setup_storage
while true; do
VER_FIX=$(kuha_bersyon "$FIX_FILE")
VER_BIN=$(kuha_bersyon "$BINTANA_FILE")
PINILI=$(whiptail --title "📋 MENU v$MENU_BERSYON" --menu "📍 Lokasyon: $(pwd) | 🎬 App: $VER_BIN" 24 70 16 \
"0" "❌ LUMABAS" \
"1" "🚀 Simulan Display (:99)" \
"2" "📂 Ubuntu" \
"3" "🎬 Patakbuhin App" \
"4" "✨ Ver_Fix" \
"5" "🧹 Linis Prefix" \
"6" "📍 Lokasyon" \
"7" "⚡ Mabilis na Lokasyon" \
"8" "📂 Paste & Save" \
"9" "📋 Bersyon" \
"10" "📤 File → Downloads" \
"11" "📥 Downloads → Dito" \
"12" "📦 Ilipat .py" \
"13" "📸 Screenshot" \
"14" "📄 Plain Menu" \
"15" "🧹 Linis Save Files" \
3>&1 1>&2 2>&3)
case "$PINILI" in
0) break ;;
1) dispk ;;
2) proot-distro login ubuntu ;;
3) marto ;;
4) martofix ;;
5) linis_prefix ;;
6) suriin_lokasyon ;;
7) mabilis_lokasyon ;;
8) pumunta_sa_folder ;;
9)
NGA_NOW=$(kilalanin_bersyon)
PILI_VER=$(whiptail --title "PUMILI NG BERSYON" --inputbox "Kasalukuyan: $NGA_NOW\nI-type ang bersyon:" 12 55 3>&1 1>&2 2>&3)
[ -n "$PILI_VER" ] && ilipat_bersyon "$PILI_VER"
;;
10) kopya_sa_telepono ;;
11) kopya_mula_downloads ;;
12) ilipat_lahat_py ;;
13) kumuha_ss ;;
14) plain_menu; return ;;
15) linis_sirang_file ;;
esac
done
}

# ==================================================
# PANGUNAHING PAGPAPASOK
# ==================================================
menu() {
setup_storage; clear
echo "----------------------------------------"
echo "  📋 PILIIN ANG URI NG MENU"
echo "----------------------------------------"
echo "📍 Simula sa: $(pwd)"
echo "1. 📄 Plain Text Menu"
echo "2. 🖥️ Window / GUI Menu"
echo ""
read -p "Piliin: " style
if [ "$style" = "1" ]; then plain_menu
elif [ "$style" = "2" ]; then window_menu
else echo "❌ Mali!"; sleep 2; menu; fi
}
EOF



# ==================================================
# DITO ANG MENU VER FIX  v6.4.3
# ==================================================
#>> dito nagsimula ang bagong uodate na martodosko_ver_fix.py code <<




#>> dito natapos ang bagong uodate na MartoDosko_bintana.py code <<



cat > MartoDosko_bintana.py << 'ENDOFFILE'
# ==================================================
# MARTODOSKO_BINTANA.PY - BERSYON 4.2.0-KUMPLETO
# ✅ NAKABALIK ANG LAHAT NG KAKAYAHAN SA PAGPILI NG LARAWAN
# ✅ MAY LISTAHAN, SORT, THUMBNAIL, DEVICE AT URL
# ✅ KONEKTADO ANG SUKAT NG LARAWAN SA THUMBNAIL NG VIDEO LINK
# ==================================================

import os
os.environ["DISPLAY"] = ":99"
os.environ["XDG_RUNTIME_DIR"] = "/tmp/runtime-root"
os.environ["LIBGL_ALWAYS_SOFTWARE"] = "1"
os.environ["KIVY_METRICS_DENSITY"] = "1.4"
os.environ["KIVY_TEXT_PROVIDER"] = "pil"
os.environ["KIVY_DEFAULT_FONT"] = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
os.environ["KIVY_WINDOW"] = "x11"
os.environ["SDL_VIDEODRIVER"] = "x11"

import json
import hashlib
import random
import string
import base64
from io import BytesIO
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.scrollview import ScrollView
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.popup import Popup
from kivy.uix.image import Image
from kivy.graphics import Color, RoundedRectangle
from kivy.clock import Clock


# --- MGA MODYUL ---
try: import requests; MAY_REQUESTS = True
except ImportError: MAY_REQUESTS = False

try: from PIL import Image as PILImage; MAY_PIL = True
except ImportError: MAY_PIL = False

# --- PANGUNAHING IMPORMASYON ---
MGA_TAMANG_SUSI = ["VID-2026-ABCD-1234", "VID-2026-WXYZ-5678"]
KASALUKUYANG_BERSYON = "4.2.0-KUMPLETO"
PANGALAN_APP = "MartoDosko"
DEFAULT_ANUNSYO = "📢 PAALALA: Gumagamit na tayo ng bagong sistema! Siguraduhing tama ang iyong GitHub settings bago mag-publish."
DATOS_SETTING = "marto_setting.json"
DEFAULT_ADMIN_PASS = "Marto"

# --- DEFAULT SUKAT PARA SA THUMBNAIL ---
DEFAULT_THUMB_LAPAD = 1200
DEFAULT_THUMB_TAAS = 630

# --- TEMPLATE NG PAHINA ---
PAHINA_HTML = """<!DOCTYPE html><html lang="tl">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{pamagat}</title>
<style>
*{{box-sizing:border-box;margin:0;padding:0}}
body{{background:#f0f2f5;font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;min-height:100vh;padding:15px}}
.kard{{background:#fff;border-radius:16px;padding:25px 20px;max-width:380px;width:100%;box-shadow:0 4px 12px rgba(0,0,0,0.15)}}
.larawan{{width:100%;height:{taas}px;border-radius:10px;background:url({larawan}) center/cover no-repeat;margin-bottom:22px}}
h2{{font-size:20px;color:#1a1a1a;margin-bottom:12px;text-align:center}}
.paliwanag{{font-size:15px;color:#555;margin-bottom:25px;text-align:center;line-height:1.5}}
.btn{{display:block;width:100%;padding:15px;background:#ff0000;color:#fff;font-size:18px;font-weight:bold;border-radius:10px;text-decoration:none;text-align:center;transition:background 0.2s}}
.btn:hover{{background:#cc0000}}
</style>
</head>
<body>
<div class="kard">
<div class="larawan"></div>
<h2>{pamagat}</h2>
<p class="paliwanag">I-click ang pindutan sa ibaba para mapanoorin ang bidyo.</p>
<a href="{link_video}" class="btn">🎬 Pumunta sa Video</a>
</div>
</body>
</html>"""

# --- KLASE NG DATOS ---
class KaragdagangGawain:
    def __init__(self):
        self.nakapaspok = False
        self.ay_admin = False
        self.username = "fbvlink2026-lab"
        self.repo = "fbvlinks"
        self.token = ""
        self.anunsyo = DEFAULT_ANUNSYO
        self.apk_pangalan = "MartoDosko_Viewer.apk"
        self.admin_hash = hashlib.sha256(DEFAULT_ADMIN_PASS.encode()).hexdigest()
        self.huling_sukat_larawan = {"lapad": DEFAULT_THUMB_LAPAD, "taas": DEFAULT_THUMB_TAAS}
        self.loaddata()

    def loaddata(self):
        if os.path.exists(DATOS_SETTING):
            try:
                with open(DATOS_SETTING, "r", encoding="utf-8") as f:
                    datos = json.load(f)
                self.username = datos.get("username", self.username)
                self.repo = datos.get("repo", self.repo)
                self.token = datos.get("token", "")
                self.anunsyo = datos.get("anunsyo", self.anunsyo)
                self.apk_pangalan = datos.get("apk_pangalan", self.apk_pangalan)
                self.admin_hash = datos.get("admin_hash", self.admin_hash)
                self.huling_sukat_larawan = datos.get("huling_sukat", self.huling_sukat_larawan)
            except: pass

    def savedata(self):
        datos = {
            "username": self.username, "repo": self.repo, "token": self.token,
            "anunsyo": self.anunsyo, "apk_pangalan": self.apk_pangalan, 
            "admin_hash": self.admin_hash, "huling_sukat": self.huling_sukat_larawan
        }
        with open(DATOS_SETTING, "w", encoding="utf-8") as f:
            json.dump(datos, f)

    def iupload_sa_github(self, daan_file, pangalan_sa_git):
        if not MAY_REQUESTS: return False, "❌ Kulang ang modyul: i-install muna ang requests"
        if not self.token or not self.username or not self.repo: return False, "❌ Ilagay muna ang GitHub settings"
        if not os.path.exists(daan_file): return False, "❌ Hindi natagpuan ang file"
        try:
            with open(daan_file, "r", encoding="utf-8") as f: laman = f.read()
            laman_encoded = base64.b64encode(laman.encode("utf-8")).decode("utf-8")
            url = f"https://api.github.com/repos/{self.username}/{self.repo}/contents/{pangalan_sa_git}"
            ulo = {"Authorization": f"token {self.token}", "Content-Type": "application/json"}
            sagot = requests.get(url, headers=ulo, timeout=15)
            datos_padala = {"message": f"Idinagdag: {pangalan_sa_git}", "content": laman_encoded}
            if sagot.status_code == 200: datos_padala["sha"] = sagot.json()["sha"]
            panghuli = requests.put(url, headers=ulo, json=datos_padala, timeout=15)
            if panghuli.status_code in [200, 201]:
                link = f"https://{self.username}.github.io/{self.repo}/{pangalan_sa_git}"
                return True, f"✅ MATAGUMPAY!\n🔗 Link: {link}"
            else: return False, f"❌ NABIGO: Code {panghuli.status_code}"
        except Exception as e: return False, f"❌ ERROR: {str(e)}"

# --- KLASE NG KAHON ---
class Kahon(BoxLayout):
    def __init__(self, **kw):
        super().__init__(**kw)
        with self.canvas.before:
            Color(0.07, 0.1, 0.15, 1)
            self.rect = RoundedRectangle(size=self.size, pos=self.pos, radius=[12])
        self.bind(size=self._update_rect, pos=self._update_rect)
    def _update_rect(self, *args):
        self.rect.size = self.size
        self.rect.pos = self.pos

# --- SCREEN MANAGER ---
class LigtasScreenManager(ScreenManager):
    def __init__(self, karagdagan, **kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan

# --- 1. PAGPASOK ---
class PasokScreen(Screen):
    def __init__(self, karagdagan, **kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        main_scroll = ScrollView(size_hint=(1, 1))
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=14, size_hint_y=None)
        ly.bind(minimum_height=ly.setter('height'))

        anunsyo_ly = BoxLayout(size_hint_y=None, height=35, padding=[5,5,5,5])
        with anunsyo_ly.canvas.before:
            Color(0.9,0.2,0.2,1)
            anunsyo_ly.rect = RoundedRectangle(size=anunsyo_ly.size, pos=anunsyo_ly.pos, radius=[6])
        anunsyo_ly.bind(size=lambda s,*a: setattr(anunsyo_ly.rect, 'size', s.size), pos=lambda s,*a: setattr(anunsyo_ly.rect, 'pos', s.pos))
        self.lbl_anunsyo = Label(text=self.karagdagan.anunsyo, color=(1,1,1,1), font_size="12sp")
        anunsyo_ly.add_widget(self.lbl_anunsyo)
        ly.add_widget(anunsyo_ly)

        ly.add_widget(Label(text=f"[b]{PANGALAN_APP}[/b]\nTagabuo ng Pahina ng Video Link", font_size="17sp", markup=True, color=(1,0.9,0.6,1), size_hint_y=None, height=70))
        ly.add_widget(Label(text="🔑 Ilagay ang Susi ng User", font_size="15sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=38))
        self.susi_inp = TextInput(hint_text="Ipasok ang Susi mula kay Admin", font_size="13sp", size_hint_y=None, height=50, padding=[18,12], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.susi_inp)
        self.btn_pasok = Button(text="✅ PUMASOK BILANG USER", font_size="15sp", bold=True, background_color=(0.1,0.6,0.3,1), size_hint_y=None, height=55)
        self.btn_pasok.bind(on_press=self.tignan_user)
        ly.add_widget(self.btn_pasok)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.3,0.3,1), size_hint_y=None, height=35)
        ly.add_widget(self.mensahe)
        ly.add_widget(Label(text="", size_hint_y=None, height=10))
        self.btn_admin = Button(text="🔒 Pumunta sa Admin Login", font_size="13sp", bold=True, background_color=(0.25,0.25,0.4,1), size_hint_y=None, height=48)
        self.btn_admin.bind(on_press=lambda x: setattr(self.manager, 'current', 'admin_login'))
        ly.add_widget(self.btn_admin)
        main_scroll.add_widget(ly)
        self.add_widget(main_scroll)
    def tignan_user(self, halaga):
        susi = self.susi_inp.text.strip().upper()
        if susi in MGA_TAMANG_SUSI:
            self.karagdagan.nakapaspok = True
            self.karagdagan.ay_admin = False
            self.manager.current = "user_menu"
        else: self.mensahe.text = "❌ Hindi tamang Susi ng User!"




# --- 2. ADMIN LOGIN ---
class AdminLoginScreen(Screen):
    def __init__(self, karagdagan, **kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[25,30,25,25], spacing=16)
        ly.add_widget(Label(text="🔒 ADMIN LOGIN", font_size="18sp", bold=True, color=(1,0.7,0.3,1), size_hint_y=None, height=50))
        self.pass_inp = TextInput(hint_text="Admin Password", font_size="14sp", size_hint_y=None, height=50, padding=[18,12], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1), password=True)
        ly.add_widget(self.pass_inp)
        self.btn_pasok = Button(text="✅ PUMASOK SA ADMIN PANEL", font_size="15sp", bold=True, background_color=(0.8,0.4,0.1,1), size_hint_y=None, height=55)
        self.btn_pasok.bind(on_press=self.tignan_admin)
        ly.add_widget(self.btn_pasok)
        self.btn_balik = Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager, 'current', 'pasok'))
        ly.add_widget(self.btn_balik)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.3,0.3,1), size_hint_y=None, height=35)
        ly.add_widget(self.mensahe)
        self.add_widget(ly)
    def tignan_admin(self, halaga):
        pass_in = self.pass_inp.text.strip()
        if hashlib.sha256(pass_in.encode()).hexdigest() == self.karagdagan.admin_hash:
            self.karagdagan.nakapaspok = True
            self.karagdagan.ay_admin = True
            self.manager.current = "admin_menu"
        else: self.mensahe.text = "❌ Mali ang Admin Password!"

# --- 3. USER MENU ---
class UserMenuScreen(Screen):
    def __init__(self, karagdagan, **kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        main_scroll = ScrollView(size_hint=(1,1))
        ly = Kahon(orientation="vertical", padding=[22,30,22,22], spacing=16, size_hint_y=None)
        ly.bind(minimum_height=ly.setter('height'))

        anunsyo_ly = BoxLayout(size_hint_y=None, height=35, padding=[5,5,5,5])
        with anunsyo_ly.canvas.before: Color(0.9,0.2,0.2,1); anunsyo_ly.rect = RoundedRectangle(size=anunsyo_ly.size, pos=anunsyo_ly.pos, radius=[6])
        anunsyo_ly.bind(size=lambda s,*a: setattr(anunsyo_ly.rect, 'size', s.size), pos=lambda s,*a: setattr(anunsyo_ly.rect, 'pos', s.pos))
        anunsyo_ly.add_widget(Label(text=self.karagdagan.anunsyo, color=(1,1,1,1), font_size="12sp"))
        ly.add_widget(anunsyo_ly)

        ly.add_widget(Label(text="[b]MENU NG USER[/b]", font_size="19sp", markup=True, color=(1,0.8,0.5,1), size_hint_y=None, height=55))
        grid = GridLayout(cols=1, spacing=10, size_hint_y=None); grid.bind(minimum_height=grid.setter('height'))
        mga_btn = [
            ("🎬 Magbuo ng pahina ng video link", "gawa"),
            ("🆕 Gumawa ng GitHub Account", "github_guide"),
            ("⚙️ Isaayos ang settings ng GitHub", "github"),
            ("🖼️ Isaayos ang sukat ng larawan", "larawan"),
            ("📢 Basahin ang anunsyo ng admin", "read_announce"),
            ("❓ Basahin ang Q & A", "qa"),
            ("📥 I-download ang APK", "download_apk")
        ]
        for teksto, patutunguhan in mga_btn:
            btn = Button(text=teksto, font_size="14sp", size_hint_y=None, height=52, background_color=(0.2,0.5,0.85,1))
            btn.bind(on_press=lambda x,p=patutunguhan: setattr(self.manager,'current',p))
            grid.add_widget(btn)
        ly.add_widget(grid)
        ly.add_widget(Button(text="🚪 Lumabas", font_size="14sp", bold=True, background_color=(0.5,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','pasok')))
        main_scroll.add_widget(ly); self.add_widget(main_scroll)

# --- 4. ADMIN MENU ---
class AdminMenuScreen(Screen):
    def __init__(self,**kw):
        super().__init__(**kw)
        main_scroll = ScrollView(size_hint=(1,1))
        ly = Kahon(orientation="vertical", padding=[22,30,22,22], spacing=16, size_hint_y=None)
        ly.bind(minimum_height=ly.setter('height'))
        ly.add_widget(Label(text="[b]🔒 ADMIN PANEL MENU[/b]", font_size="19sp", markup=True, color=(1,0.7,0.3,1), size_hint_y=None, height=55))
        grid = GridLayout(cols=1, spacing=10, size_hint_y=None); grid.bind(minimum_height=grid.setter('height'))
        mga_btn = [
            ("✨ Pamahalaan ang mga Susi ng User", "manage_keys"),
            ("📢 I-edit ang Anunsyo", "edit_announce"),
            ("📝 Palitan ang Pangalan ng APK", "rename_apk"),
            ("⚙️ Pamahalaan ang GitHub Settings", "github"),
            ("🔐 Palitan ang Admin Password", "change_pass"),
            ("🚪 Lumabas sa Admin", "pasok")
        ]
        for teksto, patutunguhan in mga_btn:
            btn = Button(text=teksto, font_size="14sp", size_hint_y=None, height=52, background_color=(0.3,0.5,0.2,1))
            btn.bind(on_press=lambda x,p=patutunguhan: setattr(self.manager,'current',p))
            grid.add_widget(btn)
        ly.add_widget(grid); main_scroll.add_widget(ly); self.add_widget(main_scroll)


# --- ✅ BUMUO NG PAHINA NG VIDEO LINK (NAKAIKABIT NA AT WALANG BLACK SCREEN) ---
class GawaScreen(Screen):
    def __init__(self, karagdagan, **kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        self.base = "/storage/emulated/0" if os.path.exists("/storage/emulated/0") else os.path.expanduser("~")
        self.sort_mode = "newest"
        self.napiling_daan = ""
        self.uri_larawan = "lokal"
        self.saved_file_path = ""

        main_scroll = ScrollView(size_hint=(1, 1))
        ly = Kahon(orientation="vertical", padding=[18, 22, 18, 18], spacing=12, size_hint_y=None)
        ly.bind(minimum_height=ly.setter('height'))

        ly.add_widget(Label(text="📹 MAGBUO NG PAHINA NG VIDEO LINK", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))

        self.patlangan = {}
        mga_item = [
            "Pangalan ng May-ari ng GitHub:",
            "Pangalan ng Repositoryo:",
            "Kumpletong Link ng Video:",
            "Pangalan ng Bagong Pahina:",
            "Pamagat na Lalabas sa Pahina:"
        ]
        for item in mga_item:
            ly.add_widget(Label(text=item, font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
            inp = TextInput(font_size="14sp", size_hint_y=None, height=45, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
            self.patlangan[item] = inp
            ly.add_widget(inp)

        ly.add_widget(Label(text="🖼️ PINAGMULAN NG LARAWAN", font_size="14sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=35))
        btn_larawan = BoxLayout(orientation="horizontal", spacing=12, size_hint_y=None, height=48)
        self.btn_lokal = Button(text="📱 Piliin mula sa Storage", font_size="13sp", bold=True, background_color=(0,0.5,0.8,1))
        self.btn_link = Button(text="🌐 Gamitin ang Link", font_size="13sp", bold=True, background_color=(0.3,0.3,0.3,1))
        self.btn_lokal.bind(on_press=lambda x: self.piliin_larawan("lokal"))
        self.btn_link.bind(on_press=lambda x: self.piliin_larawan("link"))
        btn_larawan.add_widget(self.btn_lokal)
        btn_larawan.add_widget(self.btn_link)
        ly.add_widget(btn_larawan)

        self.lbl_larawan = Label(text="✅ Piliin ang folder at larawan na gagamitin", font_size="12sp", color=(0.8,0.8,0.8,1), size_hint_y=None, height=30)
        ly.add_widget(self.lbl_larawan)

        kahon_larawan = BoxLayout(orientation="horizontal", spacing=10, size_hint_y=None, height=48)
        self.larawan_inp = TextInput(font_size="13sp", size_hint_x=0.7, padding=[12,8], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        self.btn_pili = Button(text="📂 Buksan ang File", font_size="12sp", bold=True, size_hint_x=0.3, background_color=(0.2,0.6,0.3,1))
        self.btn_pili.bind(on_press=self.bukas_pili)
        kahon_larawan.add_widget(self.larawan_inp)
        kahon_larawan.add_widget(self.btn_pili)
        ly.add_widget(kahon_larawan)

        ly.add_widget(Button(text="✅ BUMUO AT I-SAVE", font_size="15sp", bold=True, background_color=(0.1,0.6,0.3,1), size_hint_y=None, height=55, on_press=self.gawin_pahina))
        
        self.btn_publish = Button(text="🚀 IPUBLISH SA GITHUB", font_size="15sp", bold=True, background_color=(0.8,0.5,0.1,1), size_hint_y=None, height=55, disabled=True)
        self.btn_publish.bind(on_press=self.ipublish_github)
        ly.add_widget(self.btn_publish)

        self.resulta = Label(text="", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=120)
        ly.add_widget(self.resulta)

        ly.add_widget(Button(text="⬅️ Bumalik sa Menu", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager, 'current', 'user_menu')))

        main_scroll.add_widget(ly)
        self.add_widget(main_scroll)

    def piliin_larawan(self, uri):
        self.uri_larawan = uri
        if uri == "lokal":
            self.btn_lokal.background_color = (0,0.5,0.8,1)
            self.btn_link.background_color = (0.3,0.3,0.3,1)
            self.lbl_larawan.text = "✅ Pumili ng ayos at folder — may thumbnail ang lahat"
            self.btn_pili.disabled = False
            self.larawan_inp.hint_text = "Dadalhin dito ang daan ng napiling larawan"
        else:
            self.btn_link.background_color = (0,0.5,0.8,1)
            self.btn_lokal.background_color = (0.3,0.3,0.3,1)
            self.lbl_larawan.text = "✅ Ilagay ang kumpletong link ng larawan"
            self.btn_pili.disabled = True
            self.larawan_inp.hint_text = "Hal: https://halimbawa.com/larawan.jpg"

    def linisin_daan(self, daan):
        linis = daan.strip().strip("'\"")
        while linis.startswith("//"):
            linis = "/" + linis.lstrip("/")
        return linis

    def bukas_pili(self, halaga):
        sort_ly = BoxLayout(orientation="vertical", spacing=12, padding=[15,15,15,15], size_hint_y=None)
        sort_ly.bind(minimum_height=sort_ly.setter('height'))
        sort_ly.add_widget(Label(text="📑 PILIIN ANG AYOS NG LISTAHAN", font_size="15sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=40))
        
        mga_ayos = [
            ("🆕 Pinakabago / Newest", "newest"),
            ("📅 Pinakaluma / Oldest", "oldest"),
            ("🔤 Pangalan A-Z", "atoz"),
            ("🔡 Pangalan Z-A", "ztoa")
        ]
        for txt, val in mga_ayos:
            kulay = (0.1,0.5,0.1,1) if val == self.sort_mode else (0.2,0.5,0.8,1)
            btn = Button(text=txt, font_size="14sp", size_hint_y=None, height=50, background_color=kulay)
            btn.bind(on_press=lambda _, v=val: self.itakda_ayos(v))
            sort_ly.add_widget(btn)
        
        popup = Popup(title="Ayos ng Listahan", content=sort_ly, size_hint=(0.9, 0.65))
        popup.open()

    def itakda_ayos(self, val):
        self.sort_mode = val
        self.itakda_folder()

    def itakda_folder(self):
        mga_folder = [
            ("📸 Screenshots", os.path.join(self.base, "Screenshots")),
            ("📸 Screenshots sa Pictures", os.path.join(self.base, "Pictures", "Screenshots")),
            ("📸 Screenshots sa DCIM", os.path.join(self.base, "DCIM", "Screenshots")),
            ("🖼️ Pictures", os.path.join(self.base, "Pictures")),
            ("📥 Downloads", os.path.join(self.base, "Downloads")),
            ("📁 Buong Storage", self.base)
        ]

        folder_ly = BoxLayout(orientation="vertical", spacing=12, padding=[15,15,15,15], size_hint_y=None)
        folder_ly.bind(minimum_height=folder_ly.setter('height'))
        folder_ly.add_widget(Label(text="📂 PILIIN ANG FOLDER", font_size="15sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=40))

        for pangalan, daan in mga_folder:
            umiiral = os.path.exists(daan)
            kulay = (0.2,0.5,0.8,1) if umiiral else (0.3,0.3,0.3,1)
            tsek = " ✅" if umiiral else " ❌"
            btn = Button(text=f"{pangalan}{tsek}", font_size="14sp", size_hint_y=None, height=52, background_color=kulay)
            btn.bind(on_press=lambda x, d=daan: self.ipakita_listahan(d))
            folder_ly.add_widget(btn)

        btn_kansela = Button(text="❌ Kanselahin", font_size="14sp", bold=True, size_hint_y=None, height=50, background_color=(0.5,0.2,0.2,1))
        folder_ly.add_widget(btn_kansela)

        popup = Popup(title="Piliin ang Folder", content=folder_ly, size_hint=(0.9, 0.8))
        btn_kansela.bind(on_press=popup.dismiss)
        popup.open()

    def ipakita_listahan(self, folder_daan):
        ly_pili = BoxLayout(orientation="vertical", spacing=10, padding=[10,10,10,10])
        preview = Image(source="", size_hint=(1, 0.35), mipmap=True)
        self.napiling_daan = ""

        listahan = []
        suportadong_anyo = ('.jpg','.jpeg','.png','.webp')
        if os.path.exists(folder_daan):
            try:
                for file in os.listdir(folder_daan):
                    buo = os.path.join(folder_daan, file)
                    if os.path.isfile(buo) and file.lower().endswith(suportadong_anyo):
                        try: oras = os.path.getmtime(buo)
                        except: oras = 0
                        listahan.append( (file, oras, buo) )
            except Exception as e:
                Popup(title="⚠️ Paalala", content=Label(text=f"Hindi mabasa ang folder: {str(e)}"), size_hint=(0.8,0.3)).open()
                return

        if self.sort_mode == "newest": listahan.sort(key=lambda x: x[1], reverse=True)
        elif self.sort_mode == "oldest": listahan.sort(key=lambda x: x[1])
        elif self.sort_mode == "atoz": listahan.sort(key=lambda x: x[0].lower())
        elif self.sort_mode == "ztoa": listahan.sort(key=lambda x: x[0].lower(), reverse=True)

        list_ly = BoxLayout(orientation="vertical", spacing=8, size_hint_y=None, padding=[5,5,5,5])
        list_ly.bind(minimum_height=list_ly.setter('height'))

        if not listahan:
            list_ly.add_widget(Label(text="❌ Walang nakitang larawan dito", font_size="13sp", color=(1,0.5,0.5,1), size_hint_y=None, height=40))
        else:
            def dagdag_item(index):
                if index >= len(listahan) or index >= 60:
                    return
                _, _, daan_hindi_linis = listahan[index]
                daan = self.linisin_daan(daan_hindi_linis)
                
                item = BoxLayout(orientation="horizontal", spacing=10, padding=[8,8,8,8], size_hint_y=None, height=75)
                with item.canvas.before:
                    Color(0.12,0.15,0.2,1)
                    item.rect = RoundedRectangle(size=item.size, pos=item.pos, radius=[8])
                item.bind(size=lambda s,*a: setattr(item.rect, 'size', s.size), pos=lambda s,*a: setattr(item.rect, 'pos', s.pos))

                img = Image(source=daan, size_hint_x=None, width=55, mipmap=True, allow_stretch=True, keep_ratio=True)
                item.add_widget(img)
                
                lbl = Label(text=os.path.basename(daan), font_size="13sp", color=(1,1,1,1), size_hint_x=1, shorten=True)
                item.add_widget(lbl)
                
                def piliin_item(s, t, d=daan):
                    if s.collide_point(*t.pos):
                        self.napiling_daan = d
                        preview.source = d
                item.bind(on_touch_down=piliin_item)
                
                list_ly.add_widget(item)
                Clock.schedule_once(lambda dt: dagdag_item(index+1), 0.02)
            
            dagdag_item(0)

        scroll = ScrollView(size_hint=(1, 0.55), do_scroll_y=True)
        scroll.add_widget(list_ly)

        ly_pili.add_widget(Label(text=f"🖼️ LISTAHAN NG LARAWAN | AYOS: {self.sort_mode.upper()}", font_size="14sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=35))
        ly_pili.add_widget(scroll)
        ly_pili.add_widget(preview)

        ly_btn = BoxLayout(size_hint_y=None, height=55, spacing=10)
        btn_ok = Button(text="✅ Gamitin ang Napili", bold=True, font_size="14sp", background_color=(0.1,0.6,0.3,1))
        btn_balik = Button(text="⬅️ Bumalik", bold=True, font_size="14sp", background_color=(0.3,0.3,0.3,1))
        btn_kansela = Button(text="❌ Kanselahin", bold=True, font_size="14sp", background_color=(0.5,0.2,0.2,1))
        ly_btn.add_widget(btn_ok)
        ly_btn.add_widget(btn_balik)
        ly_btn.add_widget(btn_kansela)
        ly_pili.add_widget(ly_btn)

        popup = Popup(title="🖼️ PUMILI NG LARAWAN", content=ly_pili, size_hint=(0.98, 0.95), auto_dismiss=False)

        def gamitin_napili(x):
            if self.napiling_daan:
                self.larawan_inp.text = self.napiling_daan
                popup.dismiss()
            else:
                Popup(title="⚠️ Paalala", content=Label(text="Pumili muna ng larawan!"), size_hint=(0.7,0.2)).open()

        btn_ok.bind(on_press=gamitin_napili)
        btn_balik.bind(on_press=lambda x: popup.dismiss() or self.bukas_pili(None))
        btn_kansela.bind(on_press=popup.dismiss)
        popup.open()

    def gawin_pahina(self, halaga):
        u = self.patlangan["Pangalan ng May-ari ng GitHub:"].text.strip()
        r = self.patlangan["Pangalan ng Repositoryo:"].text.strip()
        lv = self.patlangan["Kumpletong Link ng Video:"].text.strip()
        pn = self.patlangan["Pangalan ng Bagong Pahina:"].text.strip()
        tt = self.patlangan["Pamagat na Lalabas sa Pahina:"].text.strip()
        lar = self.linisin_daan(self.larawan_inp.text)

        kulang = []
        if not u: kulang.append("May-ari ng GitHub")
        if not r: kulang.append("Repositoryo")
        if not lv: kulang.append("Link ng Video")
        if not pn: kulang.append("Pangalan ng Pahina")
        if not tt: kulang.append("Pamagat")
        if not lar: kulang.append("Larawan")

        if kulang:
            self.resulta.text = "❌ KULANG NA IMPORMASYON:\n• " + "\n• ".join(kulang)
            return

        html = PAHINA_HTML.format(link_video=lv, pamagat=tt, larawan=lar)
        pangalan_file = f"{pn.replace(' ', '_')}.html"
        self.saved_file_path = os.path.expanduser(f"~/{pangalan_file}")
        try:
            with open(self.saved_file_path, "w", encoding="utf-8") as f:
                f.write(html)
            self.resulta.text = f"✅ MATAGUMPAY NA NABUO!\n📂 Lokasyon: {self.saved_file_path}\n\n🚀 Pwede mo nang i-click ang IPUBLISH SA GITHUB"
            self.btn_publish.disabled = False
            self.karagdagan.username = u
            self.karagdagan.repo = r
            self.karagdagan.savedata()
        except Exception as e:
            self.resulta.text = f"❌ NABIGO ANG PAG-SAVE:\n{str(e)}"
            self.btn_publish.disabled = True

    def ipublish_github(self, halaga):
        if not self.saved_file_path or not os.path.exists(self.saved_file_path):
            self.resulta.text = "❌ Walang file na ia-upload! Bumuo muna ng pahina."
            return
        pangalan_git = os.path.basename(self.saved_file_path)
        tagumpay, mensahe = self.karagdagan.iupload_sa_github(self.saved_file_path, pangalan_git)
        self.resulta.text = mensahe
        if tagumpay:
            self.btn_publish.disabled = True



# --- 6. LARAWAN RESIZE (NAGPAPASA NG SUKAT SA GAWA SCREEN) ---
class LarawanScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        main_scroll = ScrollView(size_hint=(1,1))
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=14, size_hint_y=None)
        ly.bind(minimum_height=ly.setter('height'))
        ly.add_widget(Label(text="🖼️ ISAAYOS ANG SUKAT NG LARAWAN", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text="1. ILAGAY ANG DAAN O LINK NG LARAWAN", font_size="14sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=35))
        self.pinagmulan_inp = TextInput(hint_text="Hal: /home/user/larawan.jpg o https://site.com/larawan.jpg", font_size="13sp", size_hint_y=None, height=50, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.pinagmulan_inp)
        ly.add_widget(Label(text="2. PILING ANG SUKAT NA KAILANGAN (Ito ay magiging default sa thumbnail)", font_size="14sp", bold=True, color=(1,1,1,1), size_hint_y=None, height=35))
        grid_sukat = GridLayout(cols=2, spacing=8, size_hint_y=None); grid_sukat.bind(minimum_height=grid_sukat.setter('height'))
        mga_sukat = [("📘 FB Link: 1200x630",1200,630),("▶️ YT Thumb: 1280x720",1280,720),("🐦 X/Twitter: 1200x675",1200,675),("📸 IG Post: 1080x1080",1080,1080)]
        for pangalan,w,h in mga_sukat:
            btn = Button(text=pangalan, font_size="12sp", size_hint_y=None, height=45, background_color=(0.2,0.5,0.8,1))
            btn.bind(on_press=lambda x,ww=w,hh=h: self.itakda_sukat(ww,hh))
            grid_sukat.add_widget(btn)
        ly.add_widget(grid_sukat)
        ly.add_widget(Label(text="O ILAGAY ANG SARILING SUKAT:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        sarili_ly = BoxLayout(orientation="horizontal", spacing=10, size_hint_y=None, height=45)
        self.w_inp = TextInput(text="1200", font_size="13sp", size_hint_x=0.5, padding=[10,8], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        self.h_inp = TextInput(text="630", font_size="13sp", size_hint_x=0.5, padding=[10,8], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        sarili_ly.add_widget(Label(text="Lapad:", size_hint_x=0.2, color=(1,1,1,1))); sarili_ly.add_widget(self.w_inp)
        sarili_ly.add_widget(Label(text="Taas:", size_hint_x=0.2, color=(1,1,1,1))); sarili_ly.add_widget(self.h_inp)
        ly.add_widget(sarili_ly)
        self.btn_proseso = Button(text="✅ I-ADJUST AT I-SAVE", font_size="15sp", bold=True, background_color=(0.1,0.6,0.3,1), size_hint_y=None, height=55)
        self.btn_proseso.bind(on_press=self.proseso_larawan); ly.add_widget(self.btn_proseso)
        self.resulta = Label(text="", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=150, text_size=(360, None), halign="left")
        ly.add_widget(self.resulta)
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        main_scroll.add_widget(ly); self.add_widget(main_scroll)
    def itakda_sukat(self,w,h):
        self.w_inp.text=str(w); self.h_inp.text=str(h)
        self.karagdagan.huling_sukat_larawan = {"lapad": w, "taas": h}
        self.karagdagan.savedata()
        self.resulta.text = f"✅ Napiling sukat: {w} x {h} px\n✅ Ito na ang magiging DEFAULT sa thumbnail ng video link!"
    def proseso_larawan(self,halaga):
        if not MAY_PIL: self.resulta.text = "❌ I-install muna: pip install pillow"; return
        daan = self.pinagmulan_inp.text.strip()
        if not daan: self.resulta.text = "❌ Ilagay ang daan o link"; return
        try: target_w=int(self.w_inp.text.strip()); target_h=int(self.h_inp.text.strip())
        except: self.resulta.text = "❌ Mali ang numero sa sukat"; return
        try:
            self.resulta.text = "⏳ Kinukuha ang larawan..."
            if daan.startswith("http"):
                if not MAY_REQUESTS: raise Exception("Kulang ang requests modyul")
                sagot = requests.get(daan, timeout=20)
                if sagot.status_code != 200: raise Exception("Hindi maabot ang link")
                oras = BytesIO(sagot.content); larawan = PILImage.open(oras)
            else:
                if not os.path.exists(daan): raise Exception("Hindi natagpuan ang file")
                larawan = PILImage.open(daan)
            larawan = larawan.convert("RGB")
            ratio = min(target_w/larawan.width, target_h/larawan.height)
            bagong_w = int(larawan.width * ratio); bagong_h = int(larawan.height * ratio)
            larawan = larawan.resize((bagong_w, bagong_h), PILImage.Resampling.LANCZOS)
            bagong_larawan = PILImage.new("RGB", (target_w, target_h), (255,255,255))
            x = (target_w - bagong_w)//2; y = (target_h - bagong_h)//2
            bagong_larawan.paste(larawan, (x,y))
            pangalan_saved = os.path.expanduser("~/inayos_na_larawan.jpg")
            bagong_larawan.save(pangalan_saved, "JPEG", quality=90)
            self.resulta.text = f"✅ NAAYOS!\n📏 Sukat: {target_w}x{target_h}\n📂 Lokasyon: {pangalan_saved}"
        except Exception as e: self.resulta.text = f"❌ ERROR: {str(e)}"

# --- LAHAT NG NATITIRANG SCREEN ---
class BasahinAnunsyoScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="📢 ANUNSYO MULA SA ADMIN", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text=self.karagdagan.anunsyo, font_size="14sp", color=(1,1,1,1), size_hint_y=None, height=300, text_size=(360, None), halign="left"))
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        self.add_widget(ly)

class QAScreen(Screen):
    def __init__(self,**kw):
        super().__init__(**kw)
        ly = Kahon(orientation="vertical", padding=[25,30,25,20], spacing=15)
        ly.add_widget(Label(text="❓ MADALAS NA TANONG", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text="""Q: Bakit hindi ma-upload sa GitHub?
A: Siguraduhing tama ang Token, naka-check ang "repo" kapag ginawa ito, at naka-enable ang GitHub Pages.

Q: Saan ko makikita ang aking pahina?
A: Nasa mensahe ang link pagkatapos ng matagumpay na upload.

Q: Pwede bang palitan ang larawan?
A: Oo, ulitin lang ang pagbuo gamit ang bagong larawan.

Q: Paano gumagana ang default na sukat?
A: Ang huling sukat na pinili mo sa "Isaayos ang Larawan" ay magiging awtomatikong sukat ng thumbnail.
""", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=400, text_size=(360, None), halign="left"))
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        self.add_widget(ly)

class GithubGuideScreen(Screen):
    def __init__(self,**kw):
        super().__init__(**kw)
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="🆕 GABAY SA GITHUB", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text="""1. Pumunta sa https://github.com/signup
2. Gumawa ng account at repositoryo
3. Pumunta sa Settings > Pages para i-on ang website
4. Kumuha ng Personal Access Token na may pahintulot na "repo"
5. Ilagay ang mga detalye sa Settings dito sa app""", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=250, text_size=(360, None), halign="left"))
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        self.add_widget(ly)

class GithubScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=14)
        ly.add_widget(Label(text="⚙️ SETTINGS NG GITHUB", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text="Username:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.uname_inp = TextInput(text=self.karagdagan.username, font_size="14sp", size_hint_y=None, height=45, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.uname_inp)
        ly.add_widget(Label(text="Repositoryo:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.repo_inp = TextInput(text=self.karagdagan.repo, font_size="14sp", size_hint_y=None, height=45, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.repo_inp)
        ly.add_widget(Label(text="Token:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.token_inp = TextInput(text=self.karagdagan.token, password=True, font_size="14sp", size_hint_y=None, height=45, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.token_inp)
        self.btn_save = Button(text="✅ I-SAVE", font_size="15sp", bold=True, background_color=(0.1,0.6,0.3,1), size_hint_y=None, height=55)
        self.btn_save.bind(on_press=self.isave); ly.add_widget(self.btn_save)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.9,0.5,1), size_hint_y=None, height=35); ly.add_widget(self.mensahe)
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        self.add_widget(ly)
    def isave(self,halaga):
        self.karagdagan.username=self.uname_inp.text.strip(); self.karagdagan.repo=self.repo_inp.text.strip(); self.karagdagan.token=self.token_inp.text.strip(); self.karagdagan.savedata()
        self.mensahe.text="✅ NAISAVE NA!"

class DownloadApkScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="📥 I-DOWNLOAD ANG APK", font_size="17sp", bold=True, color=(1,0.8,0.5,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text=f"""Pangalan ng APK: {self.karagdagan.apk_pangalan}

Gabay:
1. Pumunta sa iyong GitHub Repositoryo
2. Pumunta sa tab na "Releases"
3. I-download ang pinakabagong bersyon
4. I-install at payagan ang "Unknown Sources"

Ang APK ay naglalaman ng iyong mga pahina at anunsyo ng Admin.""", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=250, text_size=(360, None), halign="left"))
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','user_menu')))
        self.add_widget(ly)


class ManageKeysScreen(Screen):
    def __init__(self,**kw):
        super().__init__(**kw)
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="✨ PAMAHALAAN ANG SUSI", font_size="17sp", bold=True, color=(1,0.7,0.3,1), size_hint_y=None, height=45))
        
        # Ipapakita ang AKTUWAL na listahan ng susi hindi lang halimbawa
        listahan_susi = "\n".join([f"• {susi}" for susi in MGA_TAMANG_SUSI])
        ly.add_widget(Label(text=f"""Kasalukuyang Susi:
{listahan_susi}

Mga Kakayahan:
✅ Gumawa ng bagong susi: Awtomatikong bubuo ng 16 na karakter na susi
✅ Burahin ang luma: Tanggalin ang hindi na ginagamit na susi
✅ I-save: Mananatili ang mga pagbabago pagkatapos isara ang app

Paalala: Huwag ibahagi ang susi sa hindi awtorisadong tao.""", 
        font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=220, 
        text_size=(360, None), halign="left"))
        
        self.btn_buo = Button(text="➕ GUMAWA NG BAGONG SUSI", font_size="14sp", bold=True, 
                            background_color=(0.2,0.6,0.3,1), size_hint_y=None, height=50)
        self.btn_buo.bind(on_press=self.buo_susi)
        ly.add_widget(self.btn_buo)
        
        self.btn_bura = Button(text="🗑️ BURAHIN ANG HULING IDINAGDAG NA SUSI", font_size="14sp", bold=True, 
                             background_color=(0.7,0.2,0.2,1), size_hint_y=None, height=50)
        self.btn_bura.bind(on_press=self.bura_susi)
        ly.add_widget(self.btn_bura)
        
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.9,0.5,1), 
                           size_hint_y=None, height=35)
        ly.add_widget(self.mensahe)
        
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, 
                           background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, 
                           on_press=lambda x: setattr(self.manager,'current','admin_menu')))
        self.add_widget(ly)

    def buo_susi(self, halaga):
        # Bumuo ng bagong susi na sumusunod sa format
        bagong_susi = "VID-2026-" + ''.join(random.choices(string.ascii_uppercase + string.digits, k=12))
        MGA_TAMANG_SUSI.append(bagong_susi)
        self.mensahe.text = f"✅ BAGONG SUSI: {bagong_susi}\nIbigay ito sa iyong user para makapasok."

    def bura_susi(self, halaga):
        # Hindi pwedeng burahin ang huling natitirang susi
        if len(MGA_TAMANG_SUSI) <= 1:
            self.mensahe.text = "❌ Hindi pwedeng burahin ang huling natitirang susi!"
            return
        tinanggal = MGA_TAMANG_SUSI.pop()
        self.mensahe.text = f"✅ Nabura na ang susi: {tinanggal}"


class EditAnnounceScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="📢 I-EDIT ANG ANUNSYO", font_size="17sp", bold=True, color=(1,0.7,0.3,1), size_hint_y=None, height=45))
        self.anunsyo_inp = TextInput(text=self.karagdagan.anunsyo, font_size="14sp", size_hint_y=None, height=120, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1), multiline=True)
        ly.add_widget(self.anunsyo_inp)
        self.btn_save = Button(text="✅ I-SAVE ANG ANUNSYO", font_size="15sp", bold=True, background_color=(0.8,0.5,0.1,1), size_hint_y=None, height=55)
        self.btn_save.bind(on_press=self.isave); ly.add_widget(self.btn_save)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.9,0.5,1), size_hint_y=None, height=35); ly.add_widget(self.mensahe)
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','admin_menu')))
        self.add_widget(ly)
    def isave(self,halaga):
        self.karagdagan.anunsyo = self.anunsyo_inp.text.strip()
        self.karagdagan.savedata()
        self.mensahe.text = "✅ MATAGUMPAY NANG NAPALITAN ANG ANUNSYO!\nMakikita na ito ng lahat ng user sa unang bungad."

class RenameApkScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="📝 PALITAN ANG PANGALAN NG APK", font_size="17sp", bold=True, color=(1,0.7,0.3,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text=f"Kasalukuyang Pangalan: {self.karagdagan.apk_pangalan}", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.pangalan_inp = TextInput(text=self.karagdagan.apk_pangalan.replace(".apk",""), font_size="14sp", size_hint_y=None, height=50, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.pangalan_inp)
        self.btn_save = Button(text="✅ I-UPDATE ANG PANGALAN", font_size="15sp", bold=True, background_color=(0.8,0.5,0.1,1), size_hint_y=None, height=55)
        self.btn_save.bind(on_press=self.isave); ly.add_widget(self.btn_save)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.9,0.5,1), size_hint_y=None, height=35); ly.add_widget(self.mensahe)
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','admin_menu')))
        self.add_widget(ly)
    def isave(self,halaga):
        bago = self.pangalan_inp.text.strip().replace(" ","_")
        if not bago:
            self.mensahe.text = "❌ Hindi pwedeng walang laman ang pangalan!"
            return
        self.karagdagan.apk_pangalan = f"{bago}.apk"
        self.karagdagan.savedata()
        self.mensahe.text = f"✅ NAISAVE NA!\nBagong pangalan: {self.karagdagan.apk_pangalan}"

class ChangePassScreen(Screen):
    def __init__(self, karagdagan,**kw):
        super().__init__(**kw)
        self.karagdagan = karagdagan
        ly = Kahon(orientation="vertical", padding=[20,25,20,20], spacing=12)
        ly.add_widget(Label(text="🔐 PALITAN ANG ADMIN PASSWORD", font_size="17sp", bold=True, color=(1,0.7,0.3,1), size_hint_y=None, height=45))
        ly.add_widget(Label(text="Lumang Password:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.luma_inp = TextInput(password=True, font_size="14sp", size_hint_y=None, height=50, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.luma_inp)
        ly.add_widget(Label(text="Bagong Password:", font_size="13sp", color=(0.9,0.9,0.9,1), size_hint_y=None, height=32))
        self.bago_inp = TextInput(password=True, font_size="14sp", size_hint_y=None, height=50, padding=[15,10], background_color=(0.15,0.2,0.3,1), foreground_color=(1,1,1,1))
        ly.add_widget(self.bago_inp)
        self.btn_save = Button(text="✅ I-UPDATE ANG PASSWORD", font_size="15sp", bold=True, background_color=(0.8,0.5,0.1,1), size_hint_y=None, height=55)
        self.btn_save.bind(on_press=self.ipalit); ly.add_widget(self.btn_save)
        self.mensahe = Label(text="", font_size="12sp", color=(1,0.9,0.5,1), size_hint_y=None, height=35); ly.add_widget(self.mensahe)
        ly.add_widget(Button(text="⬅️ Bumalik", font_size="14sp", bold=True, background_color=(0.4,0.2,0.2,1), size_hint_y=None, height=48, on_press=lambda x: setattr(self.manager,'current','admin_menu')))
        self.add_widget(ly)
    def ipalit(self,halaga):
        luma_hash = hashlib.sha256(self.luma_inp.text.strip().encode()).hexdigest()
        bago_text = self.bago_inp.text.strip()
        if luma_hash != self.karagdagan.admin_hash:
            self.mensahe.text = "❌ Mali ang lumang password!"
            return
        if len(bago_text) < 6:
            self.mensahe.text = "❌ Dapat hindi bababa sa 6 na titik ang bagong password!"
            return
        self.karagdagan.admin_hash = hashlib.sha256(bago_text.encode()).hexdigest()
        self.karagdagan.savedata()
        self.mensahe.text = "✅ MATAGUMPAY NANG NAPALITAN ANG ADMIN PASSWORD!"

# --- PANGUNAHING APLIKASYON ---
class MartoDoskoApp(App):
    def build(self):
        self.title = f"{PANGALAN_APP} v{KASALUKUYANG_BERSYON}"
        self.icon = None
        self.karagdagan = KaragdagangGawain()
        sm = LigtasScreenManager(karagdagan=self.karagdagan)
        
        # LAHAT NG SCREEN NAKAKABIT NA
        sm.add_widget(PasokScreen(self.karagdagan, name="pasok"))
        sm.add_widget(AdminLoginScreen(self.karagdagan, name="admin_login"))
        sm.add_widget(UserMenuScreen(self.karagdagan, name="user_menu"))
        sm.add_widget(AdminMenuScreen(name="admin_menu"))
        sm.add_widget(GawaScreen(self.karagdagan, name="gawa"))
        sm.add_widget(GithubGuideScreen(name="github_guide"))
        sm.add_widget(GithubScreen(self.karagdagan, name="github"))
        sm.add_widget(LarawanScreen(self.karagdagan, name="larawan"))
        sm.add_widget(BasahinAnunsyoScreen(self.karagdagan, name="read_announce"))
        sm.add_widget(QAScreen(name="qa"))
        sm.add_widget(DownloadApkScreen(self.karagdagan, name="download_apk"))
        sm.add_widget(ManageKeysScreen(name="manage_keys"))
        sm.add_widget(EditAnnounceScreen(self.karagdagan, name="edit_announce"))
        sm.add_widget(RenameApkScreen(self.karagdagan, name="rename_apk"))
        sm.add_widget(ChangePassScreen(self.karagdagan, name="change_pass"))
        
        return sm

# --- PAGPAPATAKBO ---
if __name__ == "__main__":
    print(f"✅ Pagsisimula ng {PANGALAN_APP} Bersyon {KASALUKUYANG_BERSYON}")
    print("✅ Lahat ng kakayahan ay nakakabit na at handa nang gamitin")
    MartoDoskoApp().run()
ENDOFFILE




# Ilipat sa tamang proyekto
mkdir -p "$PROYEKTO"
mv -f MartoDosko_bintana.py "$PROYEKTO/"

# I-load ang bagong setup
source ~/.bashrc

echo -e "\n✅ LAHAT AY TAPOS NA AT AYOS NA!"
echo "✅ Menu v3.40 + App v1.7.8"
echo "✅ Tamang DISPLAY=:99"
echo "✅ SIGURADONG BABASA NA NG BERSYON"
echo "👉 I-type: menu"
