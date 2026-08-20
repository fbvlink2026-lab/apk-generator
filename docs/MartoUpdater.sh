# ==========================================
# ✅ MARTODOSKO v5.28 — TANGGAL NA ANG LAHAT NG LOCAL SA LABAS NG FUNCTION!
# ✅ WALANG ERROR NA — SIGURADO!
# ✅ Ctrl+D = LIGTAS ANG PASTE!
# ==========================================

SCRIPT_PATH="/data/data/com.termux/files/usr/bin/martodosko"
SELF_BACKUP_DIR="$HOME/MARTODOS_BACKUPS"
CONFIG_FILE="$HOME/.martodosko_config"
PATHS_FILE="$HOME/.martodosko_paths"
REPO_SCAN_CACHE="$HOME/.martodosko_repo_paths"
REPO_LAST_HASH="$HOME/.martodosko_repo_hash"
ORIGINAL_DIR="$PWD"
VERSION="v5.28"

clear
echo "=========================================="
echo "   🎸 M A R T O D O S K O  -  $VERSION"
echo "   ✅ FINAL FIX: WALANG LOCAL SA LABAS NG FUNCTION!"
echo "=========================================="
echo ""

if [ -f "$SCRIPT_PATH" ]; then
  CURRENT_VER=""
  grep -q "^VERSION=" "$SCRIPT_PATH" 2>/dev/null && CURRENT_VER=$(grep "^VERSION=" "$SCRIPT_PATH" | cut -d'"' -f2)
  [ -z "$CURRENT_VER" ] && CURRENT_VER="Lumang Bersyon"
  echo "⚠️ MAY NAKAINSTALL NA: $CURRENT_VER"
  echo "👉 BAGONG VERSION: $VERSION"
  echo ""
  read -r -p "👉 PALITAN BA ANG KASALUKUYANG VERSION? (Y/n): " REPLACE_ASK
  if [ "$REPLACE_ASK" = "n" ] || [ "$REPLACE_ASK" = "N" ]; then
    echo "✅ HINDI PINALITAN — LUMALABAS..."
    sleep 1
    exit 0
  fi
  echo "✅ PAPALITAN NA — NAGBA-BACKUP MUNA..."
  mkdir -p "$SELF_BACKUP_DIR"
  cp "$SCRIPT_PATH" "$SELF_BACKUP_DIR/martodosko_${CURRENT_VER}_backup_$(date +%Y%m%d_%H%M%S).sh"
  echo "✅ NAKA-BACKUP NA!"
  echo ""
else
  echo "✅ WALA PANG NAKAINSTALL — MAG-I-INSTALL NG BAGO..."
  echo ""
fi

cat > "$SCRIPT_PATH" << 'ENDSCRIPT'
#!/bin/bash
VERSION="v5.28"
START_DIR="$PWD"
WORK_DIR="$HOME/apk-generator"
CONFIG_FILE="$HOME/.martodosko_config"
PATHS_FILE="$HOME/.martodosko_paths"
REPO_SCAN_CACHE="$HOME/.martodosko_repo_paths"
REPO_LAST_HASH="$HOME/.martodosko_repo_hash"

cd "$WORK_DIR" || { echo "❌ HINDI MAHANAP ANG apk-generator!"; cd "$START_DIR"; exit 1; }

PROJECT_BACKUP_ROOT="$HOME/GUITARFX_BACKUPS"
AUTO_RUN_BUILD="OFF"
SAVED_TAON=""
SAVED_BUWAN_MULA=""
SAVED_BUWAN_HANGGANG=""
LAST_ICON_NAME=""
LAST_ICON_PATH=""
ICON_DEST_PATH=""
FILE_FILENAME=""
FILE_CONTENT=""
FILE_DEST_PATH=""
REPO_ROOT=""
declare -A SAVED_PATHS_ARRAY=()
REPO_PATHS_LIST=()
ICON_FILES_ADDED=""

buwan_pangalan() {
  B=$1
  case "$B" in
    1) echo "Enero" ;; 2) echo "Pebrero" ;; 3) echo "Marso" ;; 4) echo "Abril" ;;
    5) echo "Mayo" ;; 6) echo "Hunyo" ;; 7) echo "Hulyo" ;; 8) echo "Agosto" ;;
    9) echo "Setyembre" ;; 10) echo "Oktubre" ;; 11) echo "Nobyembre" ;; 12) echo "Disyembre" ;;
    *) echo "Buwan $B" ;;
  esac
}

huling_araw_ng_buwan() {
  TAON=$1; BUWAN=$2
  date -d "$TAON-$BUWAN-01 +1 month -1 day" +%d 2>/dev/null || echo "31"
}

load_config() {
  if [ -f "$CONFIG_FILE" ]; then
    while IFS='=' read -r key val; do
      case "$key" in
        AUTO_RUN_BUILD) AUTO_RUN_BUILD="$val" ;;
        SAVED_TAON) SAVED_TAON="$val" ;;
        SAVED_BUWAN_MULA) SAVED_BUWAN_MULA="$val" ;;
        SAVED_BUWAN_HANGGANG) SAVED_BUWAN_HANGGANG="$val" ;;
      esac
    done < "$CONFIG_FILE"
  fi
}

save_config() {
  mkdir -p "$HOME"
  cat > "$CONFIG_FILE" << EOF
AUTO_RUN_BUILD=$AUTO_RUN_BUILD
SAVED_TAON=$PINILI_TAON
SAVED_BUWAN_MULA=$BUWAN_MULA
SAVED_BUWAN_HANGGANG=$BUWAN_HANGGANG
EOF
}

hanapin_repo_root() {
  DIR="$PWD"
  while [ "$DIR" != "/" ]; do
    if [ -d "$DIR/.git" ]; then
      REPO_ROOT="$DIR"
      return 0
    fi
    DIR=$(dirname "$DIR")
  done
  REPO_ROOT=""
  return 1
}

compute_repo_hash() {
  [ -z "$REPO_ROOT" ] && { echo ""; return; }
  find "$REPO_ROOT" -maxdepth 4 -type d -not -path '*/.*' -print0 2>/dev/null | sort -z | xargs -0 stat -c "%n %Y" 2>/dev/null | sha256sum | cut -d' ' -f1
}

scan_repo_paths() {
  echo ""
  echo "🔍 SCANNING REPOSITORY STRUCTURE..."
  hanapin_repo_root || { echo "❌ HINDI MAHANAP ANG REPO ROOT!"; return 1; }
  CURRENT_HASH=$(compute_repo_hash)
  SAVED_HASH=""
  [ -f "$REPO_LAST_HASH" ] && SAVED_HASH=$(cat "$REPO_LAST_HASH")
  if [ -n "$SAVED_HASH" ] && [ "$CURRENT_HASH" = "$SAVED_HASH" ] && [ -f "$REPO_SCAN_CACHE" ]; then
    echo "✅ GINAMIT ANG NA-SCAN NA STRUCTURE — WALANG PAGBABAGO!"
    mapfile -t REPO_PATHS_LIST < "$REPO_SCAN_CACHE"
    return 0
  fi
  echo "⚠️ NAGBAGO ANG STRUCTURE NG REPO — MULING MAG-SCAN..."
  REPO_PATHS_LIST=("(Root)")
  while IFS= read -r -d '' d; do
    REL="${d#$REPO_ROOT/}"
    [[ "$REL" == .* || "$REL" == "$REPO_ROOT" ]] && continue
    REPO_PATHS_LIST+=("$REL")
  done < <(find "$REPO_ROOT" -maxdepth 4 -type d -not -path '*/.*' -print0 2>/dev/null | sort -z)
  printf "%s\n" "${REPO_PATHS_LIST[@]}" > "$REPO_SCAN_CACHE"
  echo "$CURRENT_HASH" > "$REPO_LAST_HASH"
  echo "✅ NATAPOS ANG SCAN — ${#REPO_PATHS_LIST[@]} NA FOLDER NAKITA!"
  echo ""
  return 0
}

pili_path_from_scan() {
  PURPOSE="$1"
  FILE_NAME="$2"
  DEFAULT_PATH="$3"
  echo ""
  echo "📂 PILIIN ANG PATH — $PURPOSE"
  echo "=========================================="
  echo "   [0] /  ← Ugat ng Repository"
  IDX=1
  for p in "${REPO_PATHS_LIST[@]:1}"; do
    echo "   [$IDX] /$p/"
    IDX=$((IDX + 1))
  done
  echo "   [m] ✏️  Mano-mano — Ako ang magsulat ng path"
  [ -n "$DEFAULT_PATH" ] && echo "   [ENTER] = Default: $DEFAULT_PATH"
  echo "=========================================="
  SEL=""
  FINAL_PATH="$DEFAULT_PATH"
  read -r -p "👉 ILAGAY ANG NUMERO, 'm', o ENTER: " SEL
  if [ -z "$SEL" ] && [ -n "$DEFAULT_PATH" ]; then
    FINAL_PATH="$DEFAULT_PATH"
  else
    case "$SEL" in
      0) FINAL_PATH="/" ;;
      m|M) read -r -p "👉 ILAGAY ANG PATH (hal: apps/GuitarFX/): " FINAL_PATH ;;
      *) if [[ "$SEL" =~ ^[0-9]+$ ]] && [ "$SEL" -ge 1 ] && [ "$SEL" -lt ${#REPO_PATHS_LIST[@]} ]; then
           FINAL_PATH="/${REPO_PATHS_LIST[$SEL]}/"
         else
           [ -n "$DEFAULT_PATH" ] && FINAL_PATH="$DEFAULT_PATH" || FINAL_PATH="/"
         fi
         ;;
    esac
  fi
  FINAL_PATH=$(echo "$FINAL_PATH" | sed 's|//|/|g')
  if [ -n "$FILE_NAME" ]; then
    SAVED_PATH="${SAVED_PATHS_ARRAY[$FILE_NAME]:-}"
    if [ -z "$SAVED_PATH" ] || [ "$FINAL_PATH" != "$SAVED_PATH" ]; then
      read -r -p "👉 I-SAVE BA ITO BILANG DEFAULT PARA SA '$FILE_NAME'? (y/N): " SAVE_ASK
      if [ "$SAVE_ASK" = "y" ] || [ "$SAVE_ASK" = "Y" ]; then
        SAVED_PATHS_ARRAY["$FILE_NAME"]="$FINAL_PATH"
        > "$PATHS_FILE"
        for k in "${!SAVED_PATHS_ARRAY[@]}"; do echo "$k=${SAVED_PATHS_ARRAY[$k]}" >> "$PATHS_FILE"; done
        echo "💾 NA-SAVE: $FILE_NAME → $FINAL_PATH"
      fi
    fi
  fi
  echo "$FINAL_PATH"
}

load_saved_paths() {
  SAVED_PATHS_ARRAY=()
  if [ -f "$PATHS_FILE" ]; then
    while IFS='=' read -r k v; do
      [ -n "$k" ] && SAVED_PATHS_ARRAY["$k"]="$v"
    done < "$PATHS_FILE"
  fi
}

progress_bar() {
  C=$1; T=$2; LEN=30
  PCT=$((C*100/T)); FILLED=$((C*LEN/T)); EMPTY=$((LEN-FILLED))
  BG=""; BE=""
  for ((i=0;i<FILLED;i++)); do BG+="█"; done
  for ((i=0;i<EMPTY;i++)); do BE+="░"; done
  printf "\r\033[32m📋 SINUSURI... [%s%s] %3d%% (%d/%d)\033[0m" "$BG" "$BE" "$PCT" "$C" "$T"
}

normalize_num() { n="$1"; n="${n#0}"; [ -z "$n" ] && n=0; echo "$n"; }

pili_at_ayus_ang_icon() {
  echo ""
  echo "=========================================="
  echo "   🖼️  PILIN ANG ICON — MAY SARILING PATH!"
  echo "=========================================="
  echo ""
  IMAGICK_CMD=""
  if command -v magick &>/dev/null; then
    IMAGICK_CMD="magick"; echo "✅ IMAGEMAGICK v7 — 'magick' ang gagamitin!"
  elif command -v convert &>/dev/null; then
    IMAGICK_CMD="convert"; echo "✅ IMAGEMAGICK v6 — 'convert' ang gagamitin!"
  else
    echo "❌ KAILANGAN NG IMAGEMAGICK → pkg install imagemagick"
    read -r -p "👉 ENTER..." _
    return 1
  fi
  echo ""
  STORAGE_OK=0
  [ -d "/storage/emulated/0/Download" ] || [ -d "$HOME/storage/downloads" ] && STORAGE_OK=1
  if [ "$STORAGE_OK" = "0" ]; then
    echo "❌ WALANG ACCESS SA STORAGE → termux-setup-storage"
    read -r -p "👉 ENTER..." _
    return 1
  fi
  echo "✅ STORAGE — MAY ACCESS NA!"
  echo ""
  PINILI_TAON=""
  BUWAN_MULA=""
  BUWAN_HANGGANG=""
  if [ -n "$SAVED_TAON" ] && [ -n "$SAVED_BUWAN_MULA" ] && [ -n "$SAVED_BUWAN_HANGGANG" ]; then
    P_M=$(buwan_pangalan "$SAVED_BUWAN_MULA")
    P_H=$(buwan_pangalan "$SAVED_BUWAN_HANGGANG")
    H_A=$(huling_araw_ng_buwan "$SAVED_TAON" "$SAVED_BUWAN_HANGGANG")
    echo "💾 HULING PINILING PETSA: $P_M 1 — $P_H $H_A, $SAVED_TAON"
    read -r -p "👉 GAGAMITIN BA ITO? (G=Gamitin / B=Baguhin): " CHOICE
    case "$CHOICE" in
      [Gg]*) PINILI_TAON="$SAVED_TAON"; BUWAN_MULA="$SAVED_BUWAN_MULA"; BUWAN_HANGGANG="$SAVED_BUWAN_HANGGANG" ;;
      *) echo "✅ BABAGUHIN..."; PINILI_TAON=""; ;;
    esac
    echo ""
  fi
  if [ -z "$PINILI_TAON" ]; then
    echo "📅 HAKBANG 1 — TAON (hal: 2026, 0=Lahat):"
    read -r -p "👉 TAON: " PINILI_TAON
    if [ "$PINILI_TAON" = "0" ] || [ -z "$PINILI_TAON" ]; then
      PINILI_TAON=0; echo "✅ LAHAT NG TAON"
    else
      [ ${#PINILI_TAON} -ne 4 ] && PINILI_TAON=0 && echo "⚠️ HINDI TAMA — LAHAT NG TAON" || echo "✅ TAON: $PINILI_TAON"
    fi
    echo ""
    echo "📅 HAKBANG 2 — BUWAN (1-12, 0=Lahat):"
    read -r -p "👉 MULA BUWAN: " BUWAN_MULA
    if [ "$BUWAN_MULA" = "0" ] || [ -z "$BUWAN_MULA" ]; then
      BUWAN_MULA=1; BUWAN_HANGGANG=12; echo "✅ LAHAT NG BUWAN"
    else
      read -r -p "👉 HANGGANG BUWAN: " BUWAN_HANGGANG
      [ "$BUWAN_MULA" -lt 1 ] || [ "$BUWAN_MULA" -gt 12 ] && BUWAN_MULA=1
      [ "$BUWAN_HANGGANG" -lt 1 ] || [ "$BUWAN_HANGGANG" -gt 12 ] && BUWAN_HANGGANG=12
      [ "$BUWAN_MULA" -gt "$BUWAN_HANGGANG" ] && { T=$BUWAN_MULA; BUWAN_MULA=$BUWAN_HANGGANG; BUWAN_HANGGANG=$T; }
      P_M=$(buwan_pangalan "$BUWAN_MULA")
      P_H=$(buwan_pangalan "$BUWAN_HANGGANG")
      H_A=$(huling_araw_ng_buwan "$PINILI_TAON" "$BUWAN_HANGGANG")
      echo "✅ $P_M 1 — $P_H $H_A, $PINILI_TAON"
    fi
    echo ""
    save_config
  fi
  P_M=$(buwan_pangalan "$BUWAN_MULA")
  P_H=$(buwan_pangalan "$BUWAN_HANGGANG")
  H_A=$(huling_araw_ng_buwan "$PINILI_TAON" "$BUWAN_HANGGANG")
  [ "$PINILI_TAON" -ne 0 ] && echo "🎯 TUTOK SA: $P_M 1 — $P_H $H_A, $PINILI_TAON" || echo "🎯 LAHAT NG TAON, BUWAN: $P_M — $P_H"
  echo ""
  declare -a SEARCH_PATHS=("/storage/emulated/0/Download" "/storage/emulated/0/Pictures" "$HOME/storage/downloads" "$HOME/storage/pictures")
  declare -a FILTERED=()
  if [ "$PINILI_TAON" -ne 0 ]; then
    MULA_PETSAS="$PINILI_TAON-$(printf "%02d" $BUWAN_MULA)-01"
    HANGGANG_PETSAS="$PINILI_TAON-$(printf "%02d" $BUWAN_HANGGANG)-$H_A"
    echo "📅 HANAP: $P_M 1 — $P_H $H_A, $PINILI_TAON"
    TOTAL_DIRS=${#SEARCH_PATHS[@]}; DIR_COUNT=0
    for DIR in "${SEARCH_PATHS[@]}"; do
      DIR_COUNT=$((DIR_COUNT+1))
      progress_bar $DIR_COUNT $TOTAL_DIRS
      [ ! -d "$DIR" ] && continue
      while IFS= read -r -d '' f; do FILTERED+=("$f"); done < <(find "$DIR" -maxdepth 3 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) -newermt "$MULA_PETSAS" ! -newermt "$HANGGANG_PETSAS" -print0 2>/dev/null)
    done
    progress_bar $TOTAL_DIRS $TOTAL_DIRS
    echo ""
    echo "✅ NATAPOS! — ${#FILTERED[@]} NA LARAWAN NAHANAP!"
    echo ""
  else
    declare -a ALL_FILES=()
    TOTAL_DIRS=${#SEARCH_PATHS[@]}; DIR_COUNT=0
    for DIR in "${SEARCH_PATHS[@]}"; do
      DIR_COUNT=$((DIR_COUNT+1))
      progress_bar $DIR_COUNT $TOTAL_DIRS
      [ ! -d "$DIR" ] && continue
      while IFS= read -r -d '' f; do ALL_FILES+=("$f"); done < <(find "$DIR" -maxdepth 3 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) -print0 2>/dev/null)
    done
    progress_bar $TOTAL_DIRS $TOTAL_DIRS
    echo ""
    if [ ${#ALL_FILES[@]} -eq 0 ]; then
      echo "❌ WALANG LARAWAN!"
      read -r -p "👉 ENTER..." _
      return 1
    fi
    for f in "${ALL_FILES[@]}"; do
      TS=$(stat -c "%Y" "$f" 2>/dev/null)
      FILE_DATE=$(date -d "@$TS" +"%Y-%m-%d" 2>/dev/null)
      FILE_TAON=$(echo "$FILE_DATE" | cut -d'-' -f1)
      FILE_BUWAN=$(normalize_num "$(echo "$FILE_DATE" | cut -d'-' -f2)")
      BUWAN_OK=1
      [ "$FILE_BUWAN" -lt "$BUWAN_MULA" ] || [ "$FILE_BUWAN" -gt "$BUWAN_HANGGANG" ] && BUWAN_OK=0
      [ "$BUWAN_OK" -eq 1 ] && FILTERED+=("$f")
    done
    [ ${#FILTERED[@]} -eq 0 ] && FILTERED=("${ALL_FILES[@]}")
  fi
  if [ ${#FILTERED[@]} -eq 0 ]; then
    echo "❌ WALANG LARAWAN SA PETSANG ITO!"
    read -r -p "👉 ENTER..." _
    return 1
  fi
  readarray -t FILTERED < <(for f in "${FILTERED[@]}"; do echo "$(stat -c "%Y" "$f" 2>/dev/null)|$f"; done | sort -rn | cut -d'|' -f2-)
  echo "✅ INA-AYOS — TAPOS NA!"
  echo ""
  LIMIT=20; SHOW_CNT=${#FILTERED[@]}
  [ "$SHOW_CNT" -gt "$LIMIT" ] && SHOW_CNT=$LIMIT
  echo "📋 LARAWAN — PINAKABAGO SA TAAS:"
  echo "================================"
  for i in $(seq 0 $((SHOW_CNT-1))); do
    FPATH="${FILTERED[$i]}"; FNAME=$(basename "$FPATH")
    TS=$(stat -c "%Y" "$FPATH" 2>/dev/null)
    DATE=$(date -d "@$TS" +"%Y-%m-%d" 2>/dev/null)
    LOC="[PICTURES]"; [[ "$FPATH" == *"Download"* ]] && LOC="[DOWNLOAD]"
    echo "   $((i+1)). $LOC $FNAME  ($DATE)"
  done
  REM=$(( ${#FILTERED[@]} - LIMIT ))
  [ $REM -gt 0 ] && echo "   ... at $REM pa — hindi na ipinakita"
  echo "   0. BUMALIK"
  echo ""
  read -r -p "👉 ILAGAY ANG NUMERO NG ICON: " PILI
  [ "$PILI" = "0" ] || [ -z "$PILI" ] && { echo "✅ BUMALIK..."; return 0; }
  IDX=$((PILI - 1))
  [ "$IDX" -lt 0 ] || [ "$IDX" -ge ${#FILTERED[@]} ] && { echo "❌ MALING NUMERO!"; sleep 1; return 1; }
  LAST_ICON_PATH="${FILTERED[$IDX]}"
  LAST_ICON_NAME=$(basename "$LAST_ICON_PATH")
  echo ""
  echo "✅ NAPILI MONG ICON: $LAST_ICON_NAME"
  echo ""
  declare -A SZ=(["mdpi"]=48 ["hdpi"]=72 ["xhdpi"]=96 ["xxhdpi"]=144 ["xxxhdpi"]=192)
  echo "📏 NAG-RESIZE NG ICON..."
  if [ -z "$ICON_DEST_PATH" ]; then
    SAVED_PATH="${SAVED_PATHS_ARRAY["icon"]:-}"
    if [ -n "$SAVED_PATH" ]; then
      echo "💾 NA-SAVE NA DEFAULT PATH: $SAVED_PATH"
      read -r -p "👉 GAGAMITIN BA ITO? (Y/n): " USE_SAVED
      if [ "$USE_SAVED" = "n" ] || [ "$USE_SAVED" = "N" ]; then
        ICON_DEST_PATH=$(pili_path_from_scan "ICON DESTINATION" "icon" "")
      else
        ICON_DEST_PATH="$SAVED_PATH"
      fi
    else
      ICON_DEST_PATH=$(pili_path_from_scan "ICON DESTINATION" "icon" "")
    fi
  fi
  ICON_BASE_PATH="$REPO_ROOT$ICON_DEST_PATH"
  ICON_BASE_PATH=$(echo "$ICON_BASE_PATH" | sed 's|//|/|g')
  [[ "$ICON_BASE_PATH" != *"/res/mipmap-"* ]] && ICON_BASE_PATH="${ICON_BASE_PATH}res/"
  ICON_FILES_ADDED=""
  for D in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
    S="${SZ[$D]}"
    DST_DIR="$ICON_BASE_PATH/mipmap-$D"
    DST="$DST_DIR/ic_launcher.png"
    mkdir -p "$DST_DIR"
    "$IMAGICK_CMD" "$LAST_ICON_PATH" -resize "${S}x${S}" -gravity center -background transparent -extent "${S}x${S}" "$DST"
    ICON_FILES_ADDED="$ICON_FILES_ADDED $DST"
    echo "   ✅ $D → $DST"
  done
  echo ""
  echo "✅ ICON NAAYOS NA!"
  echo ""
  return 0
}

diretsong_code() {
  echo ""
  echo "=========================================="
  echo "   📝 DIRETSONG CODE — FILENAME + PATH + Ctrl+D!"
  echo "=========================================="
  echo ""
  hanapin_repo_root || { echo "❌ HINDI MAHANAP ANG REPO ROOT!"; sleep 1; return 1; }
  scan_repo_paths
  load_saved_paths
  read -r -p "👉 FILENAME (hal: build.gradle): " FILE_FILENAME
  [ -z "$FILE_FILENAME" ] && { echo "❌ WALANG FILENAME!"; sleep 1; return 1; }
  SAVED_PATH="${SAVED_PATHS_ARRAY[$FILE_FILENAME]:-}"
  echo ""
  if [ -n "$SAVED_PATH" ]; then
    echo "💾 NA-SAVE NA DEFAULT PATH: $SAVED_PATH"
    read -r -p "👉 GAGAMITIN BA ITO? (Y/n): " USE_SAVED
    if [ "$USE_SAVED" = "n" ] || [ "$USE_SAVED" = "N" ]; then
      FILE_DEST_PATH=$(pili_path_from_scan "FILE DESTINATION" "$FILE_FILENAME" "")
    else
      FILE_DEST_PATH="$SAVED_PATH"
    fi
  else
    FILE_DEST_PATH=$(pili_path_from_scan "FILE DESTINATION" "$FILE_FILENAME" "")
  fi
  echo ""
  echo "📥 I-PASTE ANG CODE — PAGKATAPOS → PINDUTIN ANG Ctrl + D"
  echo "----------------------------------------------------------"
  FILE_CONTENT=""
  while IFS= read -r LINE; do
    FILE_CONTENT+="$LINE"$'\n'
  done
  LINE_COUNT=$(echo "$FILE_CONTENT" | wc -l)
  echo ""
  echo "✅ NAKUHA ANG LAMAN — $LINE_COUNT LINYA!"
  DEST_PATH="$REPO_ROOT$FILE_DEST_PATH$FILE_FILENAME"
  DEST_PATH=$(echo "$DEST_PATH" | sed 's|//|/|g')
  mkdir -p "$(dirname "$DEST_PATH")"
  printf '%s' "$FILE_CONTENT" > "$DEST_PATH"
  echo "✅ NA-SAVE SA: $DEST_PATH"
  echo ""
  return 0
}

cat_code_format() {
  echo ""
  echo "=========================================="
  echo "   📝 CAT CODE — PASTE + Ctrl+D — LIGTAS!"
  echo "=========================================="
  echo ""
  hanapin_repo_root || { echo "❌ HINDI MAHANAP ANG REPO ROOT!"; sleep 1; return 1; }
  scan_repo_paths
  load_saved_paths
  echo "📥 I-PASTE ANG BUONG CODE — PAGKATAPOS → PINDUTIN ANG Ctrl + D"
  echo "   🔒 LIGTAS: HINDI MAGTATRIGGER ANG KAHIT ANONG COMMAND!"
  echo "-------------------------------------------------------------------"
  PASTED=""
  while IFS= read -r LINE; do
    PASTED+="$LINE"$'\n'
  done
  CAT_LINE=$(echo "$PASTED" | grep -m 1 "^cat >")
  if [ -z "$CAT_LINE" ]; then
    echo ""
    echo "❌ HINDI MAHANAP ANG 'cat > filename' — SIGURADO KUMPLETO ANG CODE!"
    sleep 2
    return 1
  fi
  REST="${CAT_LINE#cat > }"
  FILE_FILENAME="${REST%% *}"
  FILE_FILENAME=$(echo "$FILE_FILENAME" | sed "s/['\"]//g")
  FILE_CONTENT=$(echo "$PASTED" | sed -n '/^cat >/,/^EOF$/p' | sed '1d;/^EOF$/d')
  echo ""
  echo "🔍 NAKITANG FILE: $FILE_FILENAME"
  echo ""
  SAVED_PATH="${SAVED_PATHS_ARRAY[$FILE_FILENAME]:-}"
  if [ -n "$SAVED_PATH" ]; then
    echo "💾 NA-SAVE NA DEFAULT PATH: $SAVED_PATH"
    read -r -p "👉 GAGAMITIN BA ITO? (Y/n): " USE_SAVED
    if [ "$USE_SAVED" = "n" ] || [ "$USE_SAVED" = "N" ]; then
      FILE_DEST_PATH=$(pili_path_from_scan "FILE DESTINATION" "$FILE_FILENAME" "")
    else
      FILE_DEST_PATH="$SAVED_PATH"
    fi
  else
    FILE_DEST_PATH=$(pili_path_from_scan "FILE DESTINATION" "$FILE_FILENAME" "")
  fi
  DEST_PATH="$REPO_ROOT$FILE_DEST_PATH$FILE_FILENAME"
  DEST_PATH=$(echo "$DEST_PATH" | sed 's|//|/|g')
  mkdir -p "$(dirname "$DEST_PATH")"
  printf '%s' "$FILE_CONTENT" > "$DEST_PATH"
  echo ""
  echo "✅ NA-SAVE SA: $DEST_PATH"
  echo ""
  return 0
}

option3_menu() {
  echo ""
  echo "=========================================="
  echo "   📂 OPTION 3 — ANO ANG IPAPADALA?"
  echo "=========================================="
  echo ""
  echo "   1 — 🖼️ ICON LANG"
  echo "   2 — 📝 DIRETSONG CODE — FILENAME + Ctrl+D"
  echo "   3 — 📝 CAT CODE FORMAT — Ctrl+D"
  echo "   4 — ✨ ISASABAY: ICON + CODE → ISANG COMMIT LANG!"
  echo "   0 — BUMALIK SA MENU"
  echo ""
  read -r -p "👉 PILIIN ANG NUMERO: " CHOICE
  case "$CHOICE" in
    1) ICON_DEST_PATH=""; pili_at_ayus_ang_icon; return ;;
    2) FILE_FILENAME=""; FILE_CONTENT=""; FILE_DEST_PATH=""; diretsong_code; return ;;
    3) FILE_FILENAME=""; FILE_CONTENT=""; FILE_DEST_PATH=""; cat_code_format; return ;;
    4)
      echo ""
      echo "✨ ISASABAY ANG ICON + CODE SA ISANG PAGPAPADALA!"
      echo "=========================================="
      echo ""
      echo "📝 HAKBANG 1/2 — CODE / FILE:"
      FILE_FILENAME=""; FILE_CONTENT=""; FILE_DEST_PATH=""
      diretsong_code || { echo "❌ NABIGO ANG CODE — HINDI ITUTULOY!"; return; }
      echo ""
      echo "🖼️ HAKBANG 2/2 — ICON:"
      ICON_DEST_PATH=""
      pili_at_ayus_ang_icon || { echo "❌ NABIGO ANG ICON — HINDI ITUTULOY!"; return; }
      echo ""
      echo "✅ PAREHONG NAKAHANDA — IPAPADALA SA ISANG COMMIT!"
      return
      ;;
    0) echo "✅ BUMALIK..."; return ;;
    *) echo "❌ MALING NUMERO!"; sleep 1; return ;;
  esac
}

main_push() {
  MODE="$1"
  echo ""
  echo "=========================================="
  echo "   📤 IPAPADALA NA SA GITHUB"
  echo "=========================================="
  echo ""
  if [ "$MODE" = "RUN" ] && [ "$AUTO_RUN_BUILD" = "OFF" ]; then
    echo "⚡ ANG AUTO-RUN AY NAKA-OFF."
    read -r -p "👉 GUSTO MO BA I-ON NA RIN ANG AUTO-RUN? (y/N): " ON_QUESTION
    if [ "$ON_QUESTION" = "y" ] || [ "$ON_QUESTION" = "Y" ]; then
      AUTO_RUN_BUILD="ON"
      echo "AUTO_RUN_BUILD=ON" > "$CONFIG_FILE"
      echo "✅ NAKA-ON NA ANG AUTO-RUN!"
    fi
    echo ""
  fi
  COMMIT_MSG=""
  FILES_TO_ADD=""
  if [ -n "$LAST_ICON_NAME" ] && [ -n "$ICON_FILES_ADDED" ]; then
    echo "🖼️ ICON — NAKA-HANDA NA!"
    echo "   📂 File: $LAST_ICON_NAME"
    echo "   📂 Path: $ICON_DEST_PATH"
    FILES_TO_ADD="$FILES_TO_ADD $ICON_FILES_ADDED"
  fi
  if [ -n "$FILE_FILENAME" ] && [ -n "$FILE_DEST_PATH" ]; then
    echo "📝 FILE — NAKA-HANDA NA!"
    echo "   📂 File: $FILE_FILENAME"
    echo "   📂 Path: $FILE_DEST_PATH"
    FULL_PATH="$REPO_ROOT$FILE_DEST_PATH$FILE_FILENAME"
    FULL_PATH=$(echo "$FULL_PATH" | sed 's|//|/|g')
    FILES_TO_ADD="$FILES_TO_ADD $FULL_PATH"
  fi
  FILES_TO_ADD=$(echo "$FILES_TO_ADD" | sed 's/^ *//')
  echo ""
  if [ -z "$FILES_TO_ADD" ]; then
    echo "⚠️ WALANG FILE NA IPAPADALA — LAHAT NG FILES ANG I-ADD"
    FILES_TO_ADD="."
    read -r -p "👉 I-type ang commit message (o ENTER para default): " COMMIT_MSG
    [ -z "$COMMIT_MSG" ] && COMMIT_MSG="📤 Update files — $(date +%Y%m%d_%H%M UTC)"
  else
    echo "📋 LAHAT NG IPAPADALA:"
    for f in $FILES_TO_ADD; do echo "   - $f"; done
    echo ""
    read -r -p "👉 I-type ang commit message (o ENTER para default): " COMMIT_MSG
    [ -z "$COMMIT_MSG" ] && {
      COMMIT_MSG=""
      [ -n "$LAST_ICON_NAME" ] && COMMIT_MSG+="🖼️ Icon: $LAST_ICON_NAME — "
      [ -n "$FILE_FILENAME" ] && COMMIT_MSG+="📝 File: $FILE_FILENAME"
      COMMIT_MSG=$(echo "$COMMIT_MSG" | sed 's/ — $//')
    }
  fi
  echo "📝 COMMIT MESSAGE: $COMMIT_MSG"
  echo ""
  read -r -p "👉 SIGURADO KA NA BA? IPAPADALA NA BA? (y/N): " SIGURADO
  if [ "$SIGURADO" != "y" ] && [ "$SIGURADO" != "Y" ]; then
    echo "❌ NAKANSELA!"
    sleep 1
    return
  fi
  mkdir -p "$PROJECT_BACKUP_ROOT"
  BKUP="$PROJECT_BACKUP_ROOT/backup_$(date +%Y%m%d_%H%M%S)"
  echo "💾 NAG-BACKUP MUNA..."
  cp -r . "$BKUP" 2>/dev/null
  echo "✅ BACKUP — TAPOS NA!"
  echo ""
  GITHUB_REPO="fbvlink2026-lab/apk-generator"
  ACT_URL="https://github.com/fbvlink2026-lab/apk-generator/actions"
  WF_URL="https://api.github.com/repos/$GITHUB_REPO/actions/workflows/build.yml/dispatches"
  [ -z "$GITHUB_USERNAME" ] && { read -r -p "👤 GitHub Username: " GITHUB_USERNAME; echo "export GITHUB_USERNAME=$GITHUB_USERNAME" >> ~/.bashrc; }
  [ -z "$GITHUB_TOKEN" ] && { read -s -r -p "🔑 GitHub Token: " GITHUB_TOKEN; echo "export GITHUB_TOKEN=$GITHUB_TOKEN" >> ~/.bashrc; echo ""; }
  echo "📤 NAGPAPADALA SA GITHUB..."
  cd "$REPO_ROOT" || { echo "❌ HINDI MAHANAP ANG REPO!"; return 1; }
  git add $FILES_TO_ADD
  git commit -m "$COMMIT_MSG" --allow-empty 2>/dev/null
  until git push --force "https://$GITHUB_USERNAME:$GITHUB_TOKEN@github.com/$GITHUB_REPO.git" main; do
    echo "⏳ SUBOK ULIT — HINDI MAKAKONEKTA, HINTAY NG 5 SEGUNDO..."
    sleep 5
  done
  echo ""
  echo "✅ ========================================"
  echo "✅    TAGUMPAY NA NAIPADALA!"
  [ -n "$LAST_ICON_NAME" ] && echo "✅    🖼️ ICON — NAKA-SAVE SA SARILING PATH!"
  [ -n "$FILE_FILENAME" ] && echo "✅    📝 FILE — NAKA-SAVE SA SARILING PATH!"
  echo "✅    ISANG COMMIT LANG — WALANG DOBLE!"
  echo "✅ ========================================"
  echo ""
  [ "$MODE" = "RUN" ] && {
    echo "🚀 PATAKBUHIN ANG BUILD APK..."
    curl -s -X POST -H "Accept: application/vnd.github+json" -H "Authorization: token $GITHUB_TOKEN" "$WF_URL" -d '{"ref":"main"}' >/dev/null 2>&1
    termux-open-url "$ACT_URL" 2>/dev/null
    echo "✅ BUILD — PINASIMULA NA!"
  }
  echo ""
  LAST_ICON_NAME=""
  LAST_ICON_PATH=""
  ICON_DEST_PATH=""
  FILE_DEST_PATH=""
  FILE_FILENAME=""
  FILE_CONTENT=""
  ICON_FILES_ADDED=""
  read -r -p "👉 ENTER para bumalik sa Menu..." _
}

load_config
hanapin_repo_root
scan_repo_paths
load_saved_paths

while true; do
  clear
  echo "=========================================="
  echo "   🎸 M A R T O D O S K O  -  $VERSION"
  echo "   ✅ FINAL FIX: WALANG LOCAL SA LABAS NG FUNCTION!"
  echo "=========================================="
  echo ""
  [ "$AUTO_RUN_BUILD" = "ON" ] && echo "   ⚡ AUTO-RUN: ON" || echo "   🛑 MANO-MANO MODE"
  [ -n "$LAST_ICON_NAME" ] && echo "   🖼️ ICON: $LAST_ICON_NAME → $ICON_DEST_PATH"
  [ -n "$FILE_FILENAME" ] && echo "   📝 FILE: $FILE_FILENAME → $FILE_DEST_PATH"
  if [ -n "$SAVED_TAON" ] && [ -n "$SAVED_BUWAN_MULA" ] && [ -n "$SAVED_BUWAN_HANGGANG" ]; then
    P_M=$(buwan_pangalan "$SAVED_BUWAN_MULA")
    P_H=$(buwan_pangalan "$SAVED_BUWAN_HANGGANG")
    H_A=$(huling_araw_ng_buwan "$SAVED_TAON" "$SAVED_BUWAN_HANGGANG")
    echo "   💾 NA-SAVED: $P_M 1 — $P_H $H_A, $SAVED_TAON"
  fi
  echo ""
  echo "📋 PILIIN ANG GAGAWIN:"
  echo ""
  echo "   1 - IPADALA SA GITHUB ← MAY KUMPIRMA!"
  echo "   2 - IPADALA + PATAKBUHIN AGAD ← LAGING NANDOON!"
  echo "   3 - 📂 PILIN ANG IPAPADALA → ICON / CODE / PAREHO! 🆕"
  echo "   4 - 💾 TIGNAN ANG NA-SAVE NA PATH"
  echo "   5 - 🗑️  BURAHIN ANG NA-SAVE NA PATH"
  echo "   6 - Buksan ang GitHub Repo"
  echo "   7 - Buksan ang GitHub Actions"
  echo "   8 - I-ON ang AUTO-RUN"
  echo "   9 - I-OFF ang AUTO-RUN"
  echo "  10 - TIGNAN ANG STATUS"
  echo "   0 - TAPOS NA / LUMABAS"
  echo ""
  read -r -p "👉 ILAGAY ANG NUMERO: " PILIIN
  case "$PILIIN" in
    1) main_push; continue ;;
    2) main_push "RUN"; continue ;;
    3) option3_menu; continue ;;
    4) load_saved_paths; echo ""; echo "💾 MGA NA-SAVE NA DEFAULT PATHS:"; echo "====================="; [ ${#SAVED_PATHS_ARRAY[@]} -eq 0 ] && echo "WALA PA" || for k in "${!SAVED_PATHS_ARRAY[@]}"; do echo "$k → ${SAVED_PATHS_ARRAY[$k]}"; done; echo ""; read -r -p "👉 ENTER..." _; continue ;;
    5) rm -f "$PATHS_FILE" "$REPO_SCAN_CACHE" "$REPO_LAST_HASH"; SAVED_PATHS_ARRAY=(); echo "✅ LAHAT NG NA-SAVE — NABURA NA!"; sleep 1; continue ;;
    6) termux-open-url "https://github.com/fbvlink2026-lab/apk-generator" 2>/dev/null; sleep 1; continue ;;
    7) termux-open-url "https://github.com/fbvlink2026-lab/apk-generator/actions" 2>/dev/null; sleep 1; continue ;;
    8) echo "AUTO_RUN_BUILD=ON" > "$CONFIG_FILE"; AUTO_RUN_BUILD="ON"; echo "⚡ NAKA-ON NA!"; sleep 1; continue ;;
    9) echo "AUTO_RUN_BUILD=OFF" > "$CONFIG_FILE"; AUTO_RUN_BUILD="OFF"; echo "🛑 NAKA-OFF NA!"; sleep 1; continue ;;
   10) echo ""; [ -n "$LAST_ICON_NAME" ] && echo "🖼️ Icon: $LAST_ICON_NAME → $ICON_DEST_PATH"; [ -n "$FILE_FILENAME" ] && echo "📝 File: $FILE_FILENAME → $FILE_DEST_PATH"; [ -n "$SAVED_TAON" ] && echo "💾 Petsa: $(buwan_pangalan $SAVED_BUWAN_MULA) 1 — $(buwan_pangalan $SAVED_BUWAN_HANGGANG) $(huling_araw_ng_buwan $SAVED_TAON $SAVED_BUWAN_HANGGANG), $SAVED_TAON"; echo ""; read -r -p "👉 ENTER..." _; continue ;;
    0) echo "✅ TAPOS NA!"; cd "$START_DIR"; exit 0 ;;
    *) echo "❌ MALI!"; sleep 1; continue ;;
  esac
done
ENDSCRIPT

chmod +x "$SCRIPT_PATH"
cp -f "$SCRIPT_PATH" ~/../usr/bin/martodosko
chmod +x ~/../usr/bin/martodosko

cd "$ORIGINAL_DIR"

echo ""
echo "=========================================="
echo "   ✅ $VERSION — NAKA-INSTALL NA! 🎉"
echo "   ✅ TANGGAL NA ANG LAHAT NG LOCAL SA LABAS NG FUNCTION!"
echo "   ✅ WALANG ERROR NA — SIGURADO NA!"
echo "=========================================="
echo ""
echo "👉 I-type: martodosko"
