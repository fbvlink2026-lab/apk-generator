# =====================================================
# ✅ MARTOPUSH v5.77 — OPTION 1.2: MARAMI FILE MULA SA CAT CODE!
# ✅ BUONG AYOS — WALANG SYNTAX ERROR!
# =====================================================

SCRIPT_INSTALL_PATH="/data/data/com.termux/files/usr/bin/martopush"
CONFIG_FILE="$HOME/.martopush_config"
PATHS_FILE="$HOME/.martopush_paths"

# --------------------------
# 🛡️ KUMPIRMA BAGO PALITAN
# --------------------------
if [ -f "$SCRIPT_INSTALL_PATH" ]; then
  clear
  echo "╔══════════════════════════════════════════════════════════════╗"
  echo "║          🎸  M A R T O P U S H   —   I N S T A L L E R        ║"
  echo "╠══════════════════════════════════════════════════════════════╣"
  echo "║ ⚠️  MAY NAKAINSTALL NA — VERSION: $(grep '^VERSION=' "$SCRIPT_INSTALL_PATH" 2>/dev/null | cut -d'"' -f2 || echo "luma")"
  echo "╠══════════════════════════════════════════════════════════════╣"
  echo "║ 👉 PALITAN BA ANG KASALUKUYANG VERSION? (y/n): \c"
  read PALITAN_BA
  if [[ "$PALITAN_BA" != "y" ]]; then
    echo "💤 Hindi pinalitan — mananatili ang lumang bersyon."
    echo "👉 I-type: martopush"
    exit 0
  fi
  mkdir -p "$HOME/MARTODOS_BACKUPS"
  BACKUP_FILE="$HOME/MARTODOS_BACKUPS/martopush_backup_$(date +%Y%m%d_%H%M%S).sh"
  cp "$SCRIPT_INSTALL_PATH" "$BACKUP_FILE"
  echo "✅ NAKA-BACKUP NA: $BACKUP_FILE"
fi

# --------------------------
# ✅ BUONG SCRIPT
# --------------------------
cat > "$SCRIPT_INSTALL_PATH" << 'ENDSCRIPT'
#!/bin/bash
# ==========================================
# 🎸 M A R T O P U S H  —  v5.77
# ✅ OPTION 1.2: MARAMI FILE — MULA SA ISANG PASTE!
# ✅ BAWAT FILE MAY SARILING PATH → IPAPADALA SA TAMANG LUGAR!
# ✅ AWTO ITABI MUNA BAGO MAG-PULL — WALANG ERROR!
# ✅ BUONG AYOS — WALANG SYNTAX ERROR!
# ==========================================

VERSION="v5.77"
REPO_DIR="$HOME/apk-generator"
GITHUB_BASE="https://github.com/fbvlink2026-lab/apk-generator"
GITHUB_WEB="https://fbvlink2026-lab.github.io/apk-generator"

cd "$REPO_DIR" 2>/dev/null || { echo "❌ HINDI MAHANAP ANG REPO SA: $REPO_DIR"; exit 1; }

# 🎨 KULAY
B=$'\e[1;34m'; G=$'\e[1;32m'; Y=$'\e[1;33m'; R=$'\e[1;31m'; W=$'\e[1;37m'; N=$'\e[0m'

CONFIG_FILE="$HOME/.martopush_config"
PATHS_FILE="$HOME/.martopush_paths"
LAST_DEST_PATH=""
declare -A SAVED_PATHS_ARRAY=()
STASH_NAME="martopush-temp-$(date +%Y%m%d-%H%M%S)"
GUMAMIT_STASH=0

# ==========================================
# 🛡️ SURIIN KUNG MAY INTERNET
# ==========================================
MAY_INTERNET() {
  curl -s --connect-timeout 5 https://github.com > /dev/null 2>&1
  return $?
}

HINTAY_SA_INTERNET() {
  echo ""
  echo -e "${Y}📡 WALANG INTERNET O NAWALA ANG KONEKSYON!${N}"
  echo -e "${W}⏳ HIHINTAYIN ANG PAGBALIK NG KONEKSYON...${N}"
  echo -e "${W}👉 PWEDE MONG ITIGIL — PINDUTIN: Ctrl+C${N}"
  echo ""
  SEGUNDOS=0
  while true; do
    if MAY_INTERNET; then
      echo ""
      echo -e "${G}✅ BALIK NA ANG INTERNET — ITUTULOY NA! 🚀${N}"
      echo ""
      return 0
    fi
    printf "\r${Y}   ⏳ HINTOY: %02d segundo...${N}" "$SEGUNDOS"
    sleep 1
    SEGUNDOS=$((SEGUNDOS + 1))
    if [ $((SEGUNDOS % 30)) -eq 0 ] && [ $SEGUNDOS -gt 0 ]; then
      echo ""
      echo -e "${Y}📡 HINDI PA RIN BALIK ANG INTERNET — SURIIN ANG KONEKSYON...${N}"
    fi
  done
}

# ==========================================
# 🛡️ AWTO ITABI ANG PAGBABAGO BAGO MAG-PULL
# ==========================================
AWTO_ITABI_PAGBABAGO() {
  if git status --porcelain 2>/dev/null | grep -q .; then
    echo -e "${Y}📦 MAY PAGBABAGO — ITATABI MUNA BAGO KUMUHA MULA SA GITHUB...${N}"
    git stash push -m "$STASH_NAME" > /dev/null 2>&1
    GUMAMIT_STASH=1
    echo -e "${G}✅ ITINABI NA — IBABALIK PAGKATAPUS${N}"
  else
    GUMAMIT_STASH=0
  fi
  return 0
}

AWTO_BALIK_PAGBABAGO() {
  if [ $GUMAMIT_STASH -eq 1 ]; then
    echo ""
    echo -e "${Y}📦 IBINABALIK ANG MGA PAGBABAGO...${N}"
    if git stash list | grep -q "$STASH_NAME"; then
      git stash pop > /dev/null 2>&1
      echo -e "${G}✅ IBALIK NA ANG LAHAT NG PAGBABAGO!${N}"
    else
      echo -e "${Y}⚠️ Hindi na matagpuan ang itinabing pagbabago — suriin gamit: git stash list${N}"
    fi
  fi
  GUMAMIT_STASH=0
}

# ==========================================
# 📤 PUSH — MAY AUTO-RESUME
# ==========================================
PUSH_SA_GITHUB_AWTOMATIKO() {
  local MENSAHE="$1" DEST_PATH="$2"
  while true; do
    MAY_INTERNET || HINTAY_SA_INTERNET
    echo -e "${W}📤 IPINAPADALA NA SA GITHUB...${N}"
    git push origin main
    local KODI=$?
    if [ $KODI -eq 0 ]; then
      PROSESO_AFTER_PUSH "$DEST_PATH"
      return 0
    elif [ $KODI -eq 128 ] || [ $KODI -eq 56 ] || [ $KODI -eq 7 ]; then
      echo -e "${R}⚠️ NAPUTOL ANG KONEKSYON — SUSUBOK ULIT...${N}"
      sleep 3
      continue
    else
      echo -e "${R}❌ HINDI NAPADALA — suriin ang mensahe sa itaas${N}"
      return 1
    fi
  done
}

# ==========================================
# 🧹 AWTO LINIS
# ==========================================
AWTO_LINIS_BAGO_LAHAT() {
  [ -d "processed-icons" ] && rm -rf processed-icons && echo -e "${Y}🗑️ BINURA: processed-icons/${N}"
  [ -d "$HOME/.martopush_resized" ] && rm -rf "$HOME/.martopush_resized"
  find . -name "*.tmp" -delete 2>/dev/null
  find . -name "*~" -delete 2>/dev/null
}

# ==========================================
# 📋 ILILISTA + PAPIPILIIN
# ==========================================
PUMILI_NG_IPAPADALA() {
  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📋 MGA FILE NA HINDI PA NAIPAPADALA — PUMILI KA!${N}"
  echo -e "${B}╠══════════════════════════════════════════════════════════════╣${N}"
  echo ""

  mapfile -t LAHAT_NABAGO < <(git status --porcelain 2>/dev/null | grep -v '^$')
  if [ ${#LAHAT_NABAGO[@]} -eq 0 ]; then
    echo -e "${Y}ℹ️ WALANG FILE NA IPAPADALA — lahat ay naka-padala na${N}"
    return 1
  fi

  echo -e "${W}   [0]${N} ✅ LAHAT NG NABAGO — ipadala lahat"
  echo ""
  declare -a FILE_PATH=()
  IDX=1
  for LINE in "${LAHAT_NABAGO[@]}"; do
    STAT="${LINE:0:2}"
    FPATH="${LINE:2}"
    FPATH=$(echo "$FPATH" | sed 's/^ *//')
    FILE_PATH[$IDX]="$FPATH"
    case "${STAT:0:1}" in
      M) STAT_DISP="${G}BINAGO${N}" ;;
      A) STAT_DISP="${G}BAGO${N}" ;;
      D) STAT_DISP="${R}BINURA${N}" ;;
      \?) STAT_DISP="${Y}BAGO${N}" ;;
      *) STAT_DISP="${W}$STAT${N}" ;;
    esac
    printf "   ${G}[%d]${N} %-10s %s\n" "$IDX" "$(echo -e "$STAT_DISP")" "$FPATH"
    IDX=$((IDX+1))
  done
  echo ""
  echo -e "${Y}💡 I-type numero o maraming numero (hal: 1 3 5) o 0 para LAHAT${N}"
  read -rp "👉 ANO ANG IPAPADALA? " PILI

  declare -a PILI_FILES=()
  if [[ "$PILI" == "0" ]]; then
    for i in "${!FILE_PATH[@]}"; do
      [ -n "${FILE_PATH[$i]}" ] && PILI_FILES+=("${FILE_PATH[$i]}")
    done
  else
    for N in $PILI; do
      if [ -n "${FILE_PATH[$N]}" ]; then
        PILI_FILES+=("${FILE_PATH[$N]}")
      fi
    done
  fi

  if [ ${#PILI_FILES[@]} -eq 0 ]; then
    echo -e "${Y}ℹ️ WALANG PINILI — WALANG IPAPADALA${N}"
    return 1
  fi

  echo ""
  echo -e "${B}✅ MGA FILE NA IPAPADALA:${N}"
  for F in "${PILI_FILES[@]}"; do echo -e "   → $F"; done
  echo ""
  read -rp "✅ TAMA BA ITO? (y/n): " KUMPIRMA
  [[ "$KUMPIRMA" != "y" ]] && { echo -e "${Y}ℹ️ HINDI ITINULOY${N}"; return 1; }

  SELECTED_FILES=("${PILI_FILES[@]}")
  return 0
}

# ==========================================
# 📥 PULL MULA SA GITHUB — AWTO AYUSIN!
# ==========================================
AWTO_PULL_BAGO_PUSH() {
  echo ""
  echo -e "${W}🔄 KINUKUHA MUNA ANG MGA BAGONG PAGBABAGO MULA SA GITHUB...${N}"

  MAY_INTERNET || HINTAY_SA_INTERNET
  AWTO_ITABI_PAGBABAGO

  git pull origin main --rebase 2>&1
  local KODI=$?

  AWTO_BALIK_PAGBABAGO

  if [ $KODI -eq 0 ]; then
    echo -e "${G}✅ KUMPLETO — WALANG KONFLIKT${N}"
    return 0
  elif [ $KODI -eq 1 ]; then
    echo -e "${R}⚠️ MAY KONFLIKT SA PAGBABAGO!${N}"
    echo -e "${Y}💡 Ayusin ang mga file na may '<<<<<<<', pagkatapos i-type:${N}"
    echo -e "   git add . && git rebase --continue"
    return 1
  else
    echo -e "${R}❌ HINDI MAKAKUHA — suriin ang internet${N}"
    return 1
  fi
}

SURIIN_GIT_CONFIG() {
  git config user.name &>/dev/null && git config user.email &>/dev/null && return 0
  echo -e "${Y}⚠️ HINDI NAKA-SET ANG PANGALAN AT EMAIL SA GIT${N}"
  echo -e "${W}👉 I-ILAGAY ANG ITO:${N}"
  echo -e "   git config --global user.name \"Iyong Pangalan\""
  echo -e "   git config --global user.email \"iyong@email.com\""
  return 1
}

# ==========================================
# 📊 PROGRESS BAR AT IBA PANG GAMIT
# ==========================================
progress_bar_search() {
  local CURRENT=$1 TOTAL=$2
  local PC=$(( CURRENT * 100 / TOTAL ))
  local FILL=$(( PC / 5 ))
  local BAR=""
  for ((i=0; i<FILL; i++)); do BAR+="#"; done
  for ((i=FILL; i<20; i++)); do BAR+="-"; done
  printf "\r\033[2K${W}🔍 NAGHAHANAP... [${BAR}] %3d%%${N}" "$PC"
  [ $CURRENT -eq $TOTAL ] && printf "\n"
}

progress_bar_resize_done() {
  local NAME="$1"
  printf "   ${W}%-8s [${G}####################${N}--------------------${W}] 100%%${N}\n" "$NAME"
}

buwan_pangalan() {
  case "$1" in
    1) echo "Enero" ;; 2) echo "Pebrero" ;; 3) echo "Marso" ;; 4) echo "Abril" ;;
    5) echo "Mayo" ;; 6) echo "Hunyo" ;; 7) echo "Hulyo" ;; 8) echo "Agosto" ;;
    9) echo "Setyembre" ;; 10) echo "Oktubre" ;; 11) echo "Nobyembre" ;; 12) echo "Disyembre" ;;
  esac
}

huling_araw_ng_buwan() {
  date -d "$1-$2-01 +1 month -1 day" +%d 2>/dev/null || echo "31"
}

load_config() {
  [ -f "$CONFIG_FILE" ] && while IFS='=' read -r key val; do
    [ "$key" = "LAST_DEST_PATH" ] && LAST_DEST_PATH="$val"
  done < "$CONFIG_FILE"
  [ -f "$PATHS_FILE" ] && while IFS='=' read -r key val; do
    SAVED_PATHS_ARRAY["$key"]="$val"
  done < "$PATHS_FILE"
}

save_config_paths() {
  > "$PATHS_FILE"
  for k in "${!SAVED_PATHS_ARRAY[@]}"; do echo "$k=${SAVED_PATHS_ARRAY[$k]}" >> "$PATHS_FILE"; done
  grep -v "^LAST_DEST_PATH=" "$CONFIG_FILE" > "${CONFIG_FILE}.tmp" 2>/dev/null && mv "${CONFIG_FILE}.tmp" "$CONFIG_FILE"
  echo "LAST_DEST_PATH=$LAST_DEST_PATH" >> "$CONFIG_FILE"
}

hanapin_repo_root() {
  DIR="$PWD"
  while [ "$DIR" != "/" ]; do
    [ -d "$DIR/.git" ] && { echo "$DIR"; return 0; }
    DIR=$(dirname "$DIR")
  done
  echo "$PWD"
}

IPAKITA_HEADER() {
  clear
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W}                🎸  M A R T O P U S H${N}                                ${B}║${N}"
  echo -e "${B}║${N}              GitHub Updater & File Deployer                 ${B}║${N}"
  echo -e "${B}╠══════════════════════════════════════════════════════════════╣${N}"
  echo -e "${B}║${G}  VERSYON: $VERSION${N}${B}║${N}"
  [ -n "$LAST_DEST_PATH" ] && echo -e "${B}║${W}  DEFAULT PATH: $LAST_DEST_PATH${N}${B}║${N}"
  echo -e "${B}╠══════════════════════════════════════════════════════════════╣${N}"
  echo -e "${B}║${Y}        © Created & Developed by MartoDosko  • 2026${N}${B}║${N}"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
  echo ""
}

PUMILI_NG_PATH() {
  PURPOSE="$1"; FILE_KEY="$2"
  REPO_ROOT=$(hanapin_repo_root)
  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📂 LAHAT NG PATH — $PURPOSE${N}"
  echo -e "${B}╠══════════════════════════════════════════════════════════════╣${N}"
  echo ""
  declare -a PATH_LIST=(); local IDX=1
  echo -e "${G}   [0]${N} /  ← Ugat ng Repository"; PATH_LIST[0]="/"
  echo ""; echo -e "${W}   🔍 SINUSURI ANG MGA FOLDER...${N}"; echo ""
  while IFS= read -r -d '' d; do
    REL="${d#$REPO_ROOT/}"; REL="${REL#/}"
    [[ "$REL" == .* || "$REL" == "" || "$REL" == ".git"* ]] && continue
    PATH_LIST[$IDX]="/$REL/"
    printf "   ${G}[%d]${N} /%s/\n" "$IDX" "$REL"
    IDX=$((IDX+1))
  done < <(find "$REPO_ROOT" -maxdepth 4 -type d -print0 2>/dev/null | sort -z)
  echo ""; echo -e "${G}   [m]${N} ✏️ Sariling isulat ang path"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
  while true; do
    read -rp "👉 ILAGAY ANG NUMERO: " SEL
    case "$SEL" in
      0) FINAL_PATH="/"; break ;;
      m|M) read -rp "👉 ILAGAY ANG PATH: " FINAL_PATH; FINAL_PATH="/${FINAL_PATH#/}/"; break ;;
      *) if [[ "$SEL" =~ ^[0-9]+$ ]] && [ "$SEL" -ge 1 ] && [ "$SEL" -lt $IDX ]; then
           FINAL_PATH="${PATH_LIST[$SEL]}"; break
         else echo -e "${R}❌ Maling numero${N}"; fi ;;
    esac
  done
  FINAL_PATH=$(echo "$FINAL_PATH" | sed 's|//|/|g')
  SAVED_PATHS_ARRAY["$FILE_KEY"]="$FINAL_PATH"; LAST_DEST_PATH="$FINAL_PATH"
  save_config_paths
  echo -e "${G}✅ NAPILI: $FINAL_PATH${N}"; echo ""
}

PROSESO_AFTER_PUSH() {
  DEST_PATH="$1"
  DEST_PATH_CLEAN="${DEST_PATH#/}"; DEST_PATH_CLEAN="${DEST_PATH_CLEAN%/}"
  DEST_URL="$GITHUB_BASE/tree/main/$DEST_PATH_CLEAN"
  ACTIONS_URL="$GITHUB_BASE/actions"
  echo ""
  echo -e "${G}✅ IPINADALA NA! 🎉${N}"
  echo -e "${W}📂 NAPUNTA SA: $DEST_PATH${N}"
  echo ""
  echo "👉 BUKSAN SA: "
  echo "   [1] 📂 GitHub — ang mismong lugar"
  echo "   [2] 🚀 Actions Page"
  echo "   [3] 🌐 Website"
  echo "   [0] ❌ Huwag buksan"
  read -rp "👉 PILII: " PILI
  case "$PILI" in
    1) termux-open-url "$DEST_URL" 2>/dev/null || echo "👉 Pumunta ka: $DEST_URL" ;;
    2) termux-open-url "$ACTIONS_URL" 2>/dev/null || echo "👉 Pumunta ka: $ACTIONS_URL" ;;
    3) termux-open-url "$GITHUB_WEB" 2>/dev/null || echo "👉 Pumunta ka: $GITHUB_WEB" ;;
  esac
}

PUSH_SA_GITHUB() {
  local MENSAHE="$1" DEST_PATH="$2"

  AWTO_LINIS_BAGO_LAHAT
  SURIIN_GIT_CONFIG || return 1

  declare -a SELECTED_FILES=()
  if ! PUMILI_NG_IPAPADALA; then echo ""; return 0; fi

  AWTO_PULL_BAGO_PUSH || return 1

  git add -- "${SELECTED_FILES[@]}"

  if [ -z "$(git diff --cached --name-only)" ]; then
    echo -e "${Y}ℹ️ WALANG IPAPADALA — walang napiling file${N}"
    return 0
  fi

  git commit -m "$MENSAHE"
  [ $? -ne 0 ] && { echo -e "${Y}ℹ️ Walang bagong ipapadala${N}"; return 0; }

  PUSH_SA_GITHUB_AWTOMATIKO "$MENSAHE" "$DEST_PATH"
}

# ==========================================
# 🆕 OPTION 1.2 — MARAMI FILE MULA SA ISANG CAT CODE!
# ==========================================
PROSESO_MARAMI_FILE() {
  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📄 MARAMI FILE — I-PASTE ANG BUONG CAT CODE${N}"
  echo -e "${B}║${W}    Ctrl+D kapag tapos na ang pag-paste${N}"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
  echo ""

  TMP_PASTE=$(mktemp)
  cat > "$TMP_PASTE"

  [ ! -s "$TMP_PASTE" ] && { echo -e "${Y}ℹ️ Walang ipinaste${N}"; rm -f "$TMP_PASTE"; return 0; }

  echo ""
  echo -e "${W}🔨 HINIHIWALAY ANG BAWAT FILE...${N}"

  REPO_ROOT=$(hanapin_repo_root)
  FILE_COUNT=0
  declare -a CREATED_FILES=()

  # Basahin at hatiin ang bawat cat > ... << EOF ... EOF
  while IFS= read -r LINE; do
    if [[ "$LINE" =~ ^cat[[:space:]]*\>[[:space:]]*([^[:space:]]+)[[:space:]]*\<\< ]]; then
      FILEPATH="${BASH_REMATCH[1]}"
      FILEPATH=$(echo "$FILEPATH" | sed 's/^["'\'']//;s/["'\'']$//')
      FILE_COUNT=$((FILE_COUNT+1))
      DEST_FULL="$REPO_ROOT/$FILEPATH"
      DEST_DIR=$(dirname "$DEST_FULL")
      mkdir -p "$DEST_DIR"
      > "$DEST_FULL"
      CREATED_FILES+=("$FILEPATH")
      CURRENT_FILE="$DEST_FULL"
      IN_FILE=1
      echo -e "${G}   ✅ [$FILE_COUNT] $FILEPATH${N}"
    elif [[ "$LINE" =~ ^EOF$ ]] && [ "$IN_FILE" = "1" ]; then
      IN_FILE=0
      CURRENT_FILE=""
    elif [ "$IN_FILE" = "1" ] && [ -n "$CURRENT_FILE" ]; then
      echo "$LINE" >> "$CURRENT_FILE"
    fi
  done < "$TMP_PASTE"
  rm -f "$TMP_PASTE"

  if [ $FILE_COUNT -eq 0 ]; then
    echo -e "${R}❌ WALANG FILE NA NAKUHA — suriin ang format${N}"
    echo -e "${Y}💡 Dapat ganito ang bawat bahagi:${N}"
    echo -e "${W}   cat > folder/filename.ext << EOF${N}"
    echo -e "${W}   ...nilalaman ng file...${N}"
    echo -e "${W}   EOF${N}"
    return 1
  fi

  echo ""
  echo -e "${G}✅ LAHAT NG $FILE_COUNT FILE — NAI-SAVE SA TAMANG LUGAR!${N}"
  echo ""
  read -rp "👉 IPADALA NA BA LAHAT SA GITHUB? (y/n): " PUSH_NOW
  if [[ "$PUSH_NOW" != "y" ]]; then
    echo -e "${Y}ℹ️ Hindi ipinadala — naka-save na sa repo${N}"
    return 0
  fi

  AWTO_LINIS_BAGO_LAHAT
  SURIIN_GIT_CONFIG || return 1
  AWTO_PULL_BAGO_PUSH || return 1

  git add .

  read -rp "👉 MENSAHE SA GITHUB: " MSG
  [ -z "$MSG" ] && MSG="Maraming file — $(date +%F)"

  git commit -m "$MSG"
  if [ $? -eq 0 ]; then
    PUSH_SA_GITHUB_AWTOMATIKO "$MSG" "/"
  else
    echo -e "${Y}ℹ️ Walang bagong ipapadala${N}"
  fi
}

# ==========================================
# 📄 OPTION 1.1 — ISANG FILE LANG (DATI)
# ==========================================
PROSESO_ISANG_FILE() {
  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📄 IPIPASTE ANG CODE — Ctrl+D kapag tapos${N}"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
  TMP=$(mktemp)
  cat > "$TMP"
  [ ! -s "$TMP" ] && { echo -e "${Y}ℹ️ Walang ipinaste${N}"; rm -f "$TMP"; sleep 1; return 0; }
  FIRST_LINE=$(head -n1 "$TMP")
  TARGET_FILE=""
  if echo "$FIRST_LINE" | grep -qE '^cat[[:space:]]+>'; then
    TARGET_FILE=$(echo "$FIRST_LINE" | sed -E 's/^cat[[:space:]]+>[[:space:]]*//; s/[[:space:]]+<<.*$//')
    TARGET_FILE=$(basename "$TARGET_FILE")
    DELIM=$(echo "$FIRST_LINE" | sed -E 's/^.*<<[[:space:]]*//; s/^['\''"]//; s/['\''"]$//')
    [ -z "$DELIM" ] && DELIM="EOF"
    tail -n +2 "$TMP" | sed "/^$DELIM\$/,\$d" | sed "/^[[:space:]]*$DELIM[[:space:]]*$/d" > "$TARGET_FILE"
  else
    read -rp "👉 PANGALAN NG FILE: " TARGET_FILE
    mv "$TMP" "$TARGET_FILE"
  fi
  rm -f "$TMP"
  echo -e "${G}✅ NAISAVE: $TARGET_FILE${N}"
  read -rp "👉 IPADALA NA BA AGAD? (y/n): " PUSH_NOW
  if [[ "$PUSH_NOW" == "y" ]]; then
    PUMILI_NG_PATH "DESTINASYON" "push"
    DEST_PATH="$FINAL_PATH"
    REPO_ROOT=$(hanapin_repo_root)
    FINAL_DEST="$REPO_ROOT/$DEST_PATH"
    FINAL_DEST=$(echo "$FINAL_DEST" | sed 's|//|/|g')
    mkdir -p "$FINAL_DEST"
    mv "$TARGET_FILE" "$FINAL_DEST/"
    echo -e "${G}✅ INILIPAT SA: $FINAL_DEST${N}"
    read -rp "👉 MENSAHE: " MSG
    [ -z "$MSG" ] && MSG="Ipinadala: $TARGET_FILE"
    PUSH_SA_GITHUB "$MSG" "$DEST_PATH"
  fi
  sleep 1
}

proseso_icon() {
  CURRENT_PATH="${SAVED_PATHS_ARRAY[icon]:-}"
  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📂 DESTINASYON NG ICON${N}"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"

  if [ -z "$CURRENT_PATH" ]; then
    echo -e "${Y}💡 Wala pang naisave na default path${N}"
    PUMILI_NG_PATH "ICON DESTINASYON" "icon"
    FINAL_ICON_PATH="$FINAL_PATH"
  else
    echo -e "${W}KASALUKUYANG DEFAULT: $CURRENT_PATH${N}"
    read -rp "👉 GAMITIN BA ITO? (y/n): " GAMITIN_DEFAULT
    [[ "$GAMITIN_DEFAULT" != "y" ]] && PUMILI_NG_PATH "ICON DESTINASYON" "icon" || FINAL_ICON_PATH="$CURRENT_PATH"
  fi

  echo ""
  echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
  echo -e "${B}║${W} 📅 PILII ANG PETSA NG LARAWAN${N}"
  echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"

  PINILI_TAON=""
  BUWAN_MULA=""
  BUWAN_HANGGANG=""
  SAVED_TAON="" SAVED_BUWAN_MULA="" SAVED_BUWAN_HANGGANG=""
  if [ -f "$CONFIG_FILE" ]; then
    while IFS='=' read -r k v; do
      [ "$k" = "SAVED_TAON" ] && SAVED_TAON="$v"
      [ "$k" = "SAVED_BUWAN_MULA" ] && SAVED_BUWAN_MULA="$v"
      [ "$k" = "SAVED_BUWAN_HANGGANG" ] && SAVED_BUWAN_HANGGANG="$v"
    done < "$CONFIG_FILE"
  fi

  if [ -n "$SAVED_TAON" ]; then
    P_M=$(buwan_pangalan "$SAVED_BUWAN_MULA")
    P_H=$(buwan_pangalan "$SAVED_BUWAN_HANGGANG")
    H_A=$(huling_araw_ng_buwan "$SAVED_TAON" "$SAVED_BUWAN_HANGGANG")
    echo -e "${W}💾 HULING GINAMIT: $P_M 1 — $P_H $H_A, $SAVED_TAON${N}"
    read -rp "👉 GAMITIN ULIT? (y/n): " CHOICE
    [[ "$CHOICE" == "y" ]] && { PINILI_TAON="$SAVED_TAON"; BUWAN_MULA="$SAVED_BUWAN_MULA"; BUWAN_HANGGANG="$SAVED_BUWAN_HANGGANG"; }
  fi

  if [ -z "$PINILI_TAON" ]; then
    read -rp "👉 TAON (hal: 2026, 0=LAHAT): " PINILI_TAON
    if [ "$PINILI_TAON" = "0" ] || [ -z "$PINILI_TAON" ]; then
      PINILI_TAON=0; BUWAN_MULA=1; BUWAN_HANGGANG=12
    else
      read -rp "👉 BUWAN MULA (1-12): " BUWAN_MULA
      read -rp "👉 BUWAN HANGGANG (1-12): " BUWAN_HANGGANG
      [ "$BUWAN_MULA" -lt 1 ] || [ "$BUWAN_MULA" -gt 12 ] && BUWAN_MULA=1
      [ "$BUWAN_HANGGANG" -lt 1 ] || [ "$BUWAN_HANGGANG" -gt 12 ] && BUWAN_HANGGANG=12
      [ "$BUWAN_MULA" -gt "$BUWAN_HANGGANG" ] && { T=$BUWAN_MULA; BUWAN_MULA=$BUWAN_HANGGANG; BUWAN_HANGGANG=$T; }
    fi
    grep -v "^SAVED_" "$CONFIG_FILE" > "${CONFIG_FILE}.tmp" 2>/dev/null && mv "${CONFIG_FILE}.tmp" "$CONFIG_FILE"
    echo "SAVED_TAON=$PINILI_TAON
SAVED_BUWAN_MULA=$BUWAN_MULA
SAVED_BUWAN_HANGGANG=$BUWAN_HANGGANG" >> "$CONFIG_FILE"
  fi

  P_M=$(buwan_pangalan "$BUWAN_MULA")
  P_H=$(buwan_pangalan "$BUWAN_HANGGANG")
  H_A=$(huling_araw_ng_buwan "$PINILI_TAON" "$BUWAN_HANGGANG")
  echo -e "${G}🎯 HANAP: $P_M 1 — $P_H $H_A, $PINILI_TAON${N}"
  echo ""

  IMAGICK_CMD=""
  command -v magick &>/dev/null && IMAGICK_CMD="magick" || IMAGICK_CMD="convert"
  command -v "$IMAGICK_CMD" &>/dev/null || { echo -e "${R}❌ I-type muna: pkg install imagemagick${N}"; sleep 1; return 1; }

  declare -a SEARCH_PATHS=()
  [ -d "$HOME/storage" ] && { SEARCH_PATHS+=("$HOME/storage/Downloads"); SEARCH_PATHS+=("$HOME/storage/Pictures"); SEARCH_PATHS+=("$HOME/storage/DCIM"); }
  [ -d "/sdcard" ] && { SEARCH_PATHS+=("/sdcard/Download"); SEARCH_PATHS+=("/sdcard/Pictures"); SEARCH_PATHS+=("/sdcard/DCIM"); SEARCH_PATHS+=("/sdcard/Documents"); }

  echo -e "${W}📂 HINAHANAP SA:${N}"
  for p in "${SEARCH_PATHS[@]}"; do [ -d "$p" ] && echo -e "${G}   ✅ $p${N}" || echo -e "${Y}   ⚠️  $p — WALA${N}"; done
  echo ""

  declare -a FILTERED=()
  TOTAL_FOLDERS=${#SEARCH_PATHS[@]}
  DONE_FOLDERS=0

  if [ "$PINILI_TAON" -ne 0 ]; then
    MULA_PETSAS="$PINILI_TAON-$(printf "%02d" $BUWAN_MULA)-01"
    HANGGANG_PETSAS="$PINILI_TAON-$(printf "%02d" $BUWAN_HANGGANG)-$H_A"
    for DIR in "${SEARCH_PATHS[@]}"; do
      DONE_FOLDERS=$((DONE_FOLDERS + 1))
      progress_bar_search $DONE_FOLDERS $TOTAL_FOLDERS
      [ ! -d "$DIR" ] && continue
      while IFS= read -r -d '' f; do
        FILTERED+=("$f")
      done < <(find "$DIR" -maxdepth 3 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) -newermt "$MULA_PETSAS" ! -newermt "$HANGGANG_PETSAS" -print0 2>/dev/null)
    done
  else
    for DIR in "${SEARCH_PATHS[@]}"; do
      DONE_FOLDERS=$((DONE_FOLDERS + 1))
      progress_bar_search $DONE_FOLDERS $TOTAL_FOLDERS
      [ ! -d "$DIR" ] && continue
      while IFS= read -r -d '' f; do
        FILTERED+=("$f")
      done < <(find "$DIR" -maxdepth 3 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) -print0 2>/dev/null)
    done
  fi

  if [ ${#FILTERED[@]} -eq 0 ]; then
    echo -e "\n${R}❌ WALANG LARAWAN NAHANAP${N}"
    echo -e "${Y}💡 Piliin TAON=0 para makita LAHAT${N}"
    sleep 1; return 1
  fi

  readarray -t FILTERED < <(for f in "${FILTERED[@]}"; do echo "$(stat -c "%Y" "$f" 2>/dev/null)||$f"; done | sort -rn | cut -d'|' -f3-)

  echo -e "\n${W}📋 MGA LARAWAN NA NAHANAP (${#FILTERED[@]}):${N}"
  for i in 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19; do
    [ -z "${FILTERED[$i]}" ] && break
    echo "   ${G}[$((i+1))]${N} $(basename "${FILTERED[$i]}")"
  done
  echo ""

  read -rp "👉 ILAGAY ANG NUMERO: " PILI; IDX=$((PILI-1))
  SELECTED_ICON="${FILTERED[$IDX]}"
  echo -e "${G}✅ NAPILI: $(basename "$SELECTED_ICON")${N}"

  declare -A SZ=(["mdpi"]=48 ["hdpi"]=72 ["xhdpi"]=96 ["xxhdpi"]=144 ["xxxhdpi"]=192)
  RESIZE_BASE_DIR="$HOME/.martopush_resized"
  rm -rf "$RESIZE_BASE_DIR"
  mkdir -p "$RESIZE_BASE_DIR/res"
  echo ""
  echo -e "${W}📏 INA-AYOS ANG MGA SUKAT...${N}"

  for D in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
    S="${SZ[$D]}"
    DST_DIR="$RESIZE_BASE_DIR/res/mipmap-$D"
    mkdir -p "$DST_DIR"
    "$IMAGICK_CMD" "$SELECTED_ICON" -resize "${S}x${S}" -gravity center -background transparent -extent "${S}x${S}" "$DST_DIR/ic_launcher.png" 2>/dev/null
    progress_bar_resize_done "$D"
  done
  echo -e "${G}✅ LAHAT NG SUKAT — TAPOS NA!${N}"

  REPO_ROOT=$(hanapin_repo_root)
  DEST_PATH="$FINAL_ICON_PATH"
  FINAL_DEST="$REPO_ROOT/$DEST_PATH/res/"
  FINAL_DEST=$(echo "$FINAL_DEST" | sed 's|//|/|g')
  mkdir -p "$FINAL_DEST"
  cp -r "$RESIZE_BASE_DIR/res/mipmap-"* "$FINAL_DEST/" 2>/dev/null
  echo -e "${G}✅ NAKA-KOPYA SA: $FINAL_DEST${N}"

  read -rp "👉 IPADALA NA BA SA GITHUB? (y/n): " GO
  if [[ "$GO" == "y" ]]; then
    read -rp "👉 MENSAHE: " MSG
    [ -z "$MSG" ] && MSG="🖼️ ICON — $P_M $PINILI_TAON"
    PUSH_SA_GITHUB "$MSG" "$DEST_PATH"
  fi
}

# =====================================================
# 📋 PANGUNAHING MENU
# =====================================================
load_config
while true; do
  IPAKITA_HEADER
  echo -e "${B}   ┌──────────────────────────────────────────────┐${N}"
  echo -e "${B}   │${G}   1.${W} 📄 IPIPASTE ANG CODE → I-SAVE AGAD        ${B}│${N}"
  echo -e "${B}   │${G}      ├── 1.1${W} Isang file lang                    ${B}│${N}"
  echo -e "${B}   │${G}      └── 1.2${W} MARAMI FILE — may sariling path 🆕  ${B}│${N}"
  echo -e "${B}   │${G}   2.${W} 🖼️ PILIN AT I-PROSESO ANG APK ICON       ${B}│${N}"
  echo -e "${B}   │${G}   3.${W} 📤 IPADALA SA GITHUB — PUMILI NG FILE      ${B}│${N}"
  echo -e "${B}   │${G}   4.${W} 📂 TIGNAN ANG LAHAT NG FOLDER             ${B}│${N}"
  echo -e "${B}   │${G}   5.${W} 📂 PALITAN ANG DEFAULT DESTINASYON PATH    ${B}│${N}"
  echo -e "${B}   │${G}   0.${W} ❌ TAPOS NA / LUMABAS                    ${B}│${N}"
  echo -e "${B}   └──────────────────────────────────────────────┘${N}"
  echo ""
  read -rp "👉 ILAGAY ANG NUMERO: " PILI_MENU

  case "$PILI_MENU" in
    0) echo -e "\n${G}✅ TAPOS NA!${N}"; exit 0 ;;

    1)
      echo ""
      echo -e "${B}   ┌──────────────────────────────────────────────┐${N}"
      echo -e "${B}   │${W}        📄 PILIIN ANG URI NG PAGPAPADALA       ${B}│${N}"
      echo -e "${B}   ├──────────────────────────────────────────────┤${N}"
      echo -e "${B}   │${G}   1.1${W} 📄 Isang file lang — katulad ng dati     ${B}│${N}"
      echo -e "${B}   │${G}   1.2${W} 📁 MARAMI FILE — bawat isa may sariling   ${B}│${N}"
      echo -e "${B}   │${W}       path → ipadala nang hiwalay sa GitHub 🆕    ${B}│${N}"
      echo -e "${B}   │${G}   0.${W} ↩️ BUMALIK SA MENU                       ${B}│${N}"
      echo -e "${B}   └──────────────────────────────────────────────┘${N}"
      echo ""
      read -rp "👉 PILII: " SUB_PILI
      case "$SUB_PILI" in
        1.1|1) PROSESO_ISANG_FILE; echo ""; read -rp "👉 ENTER..." ;;
        1.2|2) PROSESO_MARAMI_FILE; echo ""; read -rp "👉 ENTER..." ;;
        0) ;;
        *) echo -e "${R}❌ MALING NUMERO!${N}"; sleep 1 ;;
      esac
      ;;

    2) proseso_icon; echo ""; read -rp "👉 ENTER..." ;;

    5)
      echo ""
      echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
      echo -e "${B}║${W} 📂 PALITAN ANG DEFAULT DESTINASYON PATH${N}"
      echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
      PUMILI_NG_PATH "BAGONG DEFAULT PATH" "icon"
      SAVED_PATHS_ARRAY["icon"]="$FINAL_PATH"
      save_config_paths
      echo -e "${G}✅ BAGONG DEFAULT PATH: $FINAL_PATH${N}"
      read -rp "👉 ENTER..." ;;

    3)
      echo ""
      echo -e "${B}╔══════════════════════════════════════════════════════════════╗${N}"
      echo -e "${B}║${W} 📤 IPADALA SA GITHUB — PUMILI NG FILE${N}"
      echo -e "${B}╚══════════════════════════════════════════════════════════════╝${N}"
      PUMILI_NG_PATH "DESTINASYON" "push"
      read -rp "👉 MENSAHE: " MSG
      [ -z "$MSG" ] && MSG="Pag-update $(date +%F)"
      PUSH_SA_GITHUB "$MSG" "$FINAL_PATH"
      ;;

    4) echo ""; echo -e "${B}📂 LAHAT NG FOLDER SA REPOSITORY:${N}"; find . -type d -not -path './.git/*' | sed 's|^\./||' | sort; echo ""; read -rp "👉 ENTER..." ;;

    *) echo -e "${R}❌ MALING NUMERO!${N}"; sleep 1 ;;
  esac
done
ENDSCRIPT

chmod +x "$SCRIPT_INSTALL_PATH"

clear
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                    ✅  NAKA-INSTALL NA!  🎉                    ║"
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║   🎸 MARTOPUSH v5.77 — BUONG AYOS — WALANG SYNTAX ERROR!       ║"
echo "║   ✅ OPTION 1.1 — Isang file lang                              ║"
echo "║   ✅ OPTION 1.2 — MARAMI FILE — bawat isa may sariling path    ║"
echo "║   ✅ AWTO ITABI — WALANG ERROR NA 'cannot pull with rebase'    ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "👉 I-type: martopush"
echo ""
exec martopush

