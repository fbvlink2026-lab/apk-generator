cat > /data/data/com.termux/files/usr/bin/martopush << 'ENDSCRIPT'
#!/bin/bash
# ==========================================
# ✅ MARTOPUSH v4.0 — BUONG PROSESO NG APK ICON!
# 🖼️ PILII LARAWAN → I-RESIZE SA TAMANG SUKAT → HANDANG IPADALA!
# 📂 MAGHANAP SA ANDROID FOLDERS — Pictures, Downloads, atbp.
# 📤 Pagpadala → Tanong kung saan pupunta → Diretso sa tamang lugar!
# 📄 Sa loob na ng menu ang pag-paste + linisin ang cat code
# 🛡️ Ligtas — hindi mabubura ang ipapadala!
# ==========================================

clear
VERSION="v4.0 — APK Icon Processor + Clean Cat Code"
echo "=========================================="
echo "   📤 M A R T O P U S H  —  $VERSION"
echo "=========================================="
echo ""

REPO_DIR="$HOME/apk-generator"
GITHUB_BASE="https://github.com/fbvlink2026-lab/apk-generator"
GITHUB_WEB="https://fbvlink2026-lab.github.io/apk-generator"
cd "$REPO_DIR" 2>/dev/null || {
  echo "❌ HINDI MAHANAP: $REPO_DIR"
  exit 1
}
echo "✅ Nasa Repository: $PWD"
echo ""

LAST_DEST_PATH=""
LAST_ICON_PATH=""

# 📂 MGA FOLDER SA ANDROID KUNG SAAN HAHANAPIN ANG LARAWAN
ANDROID_FOLDERS=(
  "$HOME/Pictures"
  "$HOME/Downloads"
  "$HOME/DCIM/Camera"
  "$HOME/DCIM"
  "$HOME/Pictures/Screenshots"
  "$HOME"
  "/sdcard/Pictures"
  "/sdcard/Download"
)

# ✅ TAMANG SUKAT NG APK ICON — STANDARD
ICON_SIZE_MDPI="48x48"
ICON_SIZE_HDPI="72x72"
ICON_SIZE_XHDPI="96x96"
ICON_SIZE_XXHDPI="144x144"
ICON_SIZE_XXXHDPI="192x192"
ICON_OUTPUT_NAME="ic_launcher.png"

# ==========================================
# 📋 MENU
# ==========================================
while true; do
  clear
  echo "=========================================="
  echo "   📤 M A I N   M E N U"
  echo "=========================================="
  echo ""
  if [ -n "$LAST_DEST_PATH" ]; then
    echo "   💾 HULING LUGAR NA PINADALA: $LAST_DEST_PATH"
  fi
  if [ -n "$LAST_ICON_PATH" ]; then
    echo "   🖼️ HULING NAPROSESONG ICON: $LAST_ICON_PATH"
  fi
  [ -n "$LAST_DEST_PATH" ] || [ -n "$LAST_ICON_PATH" ] && echo ""
  echo "   📋 ANO ANG GUSTO MONG GAWIN?"
  echo ""
  echo "   ┌──────────────────────────────────────────────┐"
  echo "   │  1. 📄 IPIPASTE ANG CODE → I-SAVE AGAD         │"
  echo "   │  2. 🖼️ PILII AT I-PROSESO ANG APK ICON         │"
  echo "   │  3. 📤 IPADALA ANG FILE NA NASA TERMUX         │"
  echo "   │  4. 📂 TIGNAN ANG LAHAT NG FOLDER              │"
  echo "   │  5. 📄 TIGNAN ANG MGA NABAGONG FILE            │"
  echo "   │  0. ❌ TAPOS NA / LUMABAS                      │"
  echo "   └──────────────────────────────────────────────┘"
  echo ""
  read -rp "👉 ILAGAY ANG NUMERO: " PILI_MENU

  case "$PILI_MENU" in
    0)
      echo -e "\n✅ TAPOS NA!"
      exit 0
      ;;

    # =====================================================
    # 🖼️ OPSYON 2 — PILII AT I-PROSESO ANG APK ICON!
    # =====================================================
    2)
      echo -e "\n=========================================="
      echo "   🖼️ PILII AT I-PROSESO ANG APK ICON"
      echo "=========================================="
      echo ""

      # ✅ SURIIN KUNG MAY IMAGEMAGICK
      if ! command -v magick &>/dev/null && ! command -v convert &>/dev/null; then
        echo "❌ HINDI MAHANAP ANG IMAGEMAGICK!"
        echo "👉 I-type: pkg install imagemagick"
        echo "👉 Pagkatapos — subukan ulit."
        read -rp "👉 ENTER para bumalik..."
        continue
      fi
      MAGICK_CMD=$(command -v magick || echo "convert")
      echo "✅ ImageMagick — NAKA-INSTALL NA! ($MAGICK_CMD)"
      echo ""

      # ✅ ILISTA ANG MGA FOLDER KUNG SAAN HAHANAPIN
      echo "📂 MGA FOLDER KUNG SAAN HAHANAPIN ANG LARAWAN:"
      FOUND_FOLDERS=()
      IDX=1
      for FOLDER in "${ANDROID_FOLDERS[@]}"; do
        if [ -d "$FOLDER" ]; then
          FOUND_FOLDERS+=("$FOLDER")
          echo "   [$IDX] 📁 $FOLDER"
          ((IDX++))
        fi
      done
      echo "   [0] 🔍 LAHAT NG FOLDER — HANAPIN LAHAT NG LARAWAN"
      echo ""
      read -rp "👉 PILII ANG FOLDER: " PILI_FOLDER

      SELECTED_FOLDER=""
      if [[ "$PILI_FOLDER" == "0" ]]; then
        SELECTED_FOLDER="ALL"
      else
        IDX=$((PILI_FOLDER-1))
        if [ -n "${FOUND_FOLDERS[$IDX]}" ]; then
          SELECTED_FOLDER="${FOUND_FOLDERS[$IDX]}"
          echo "✅ FOLDER: $SELECTED_FOLDER"
        else
          echo "❌ MALING NUMERO"
          sleep 1
          continue
        fi
      fi
      echo ""

      # ✅ HANAPIN ANG MGA LARAWAN
      echo "🔍 HINAHANAP ANG MGA LARAWAN..."
      FOUND_IMAGES=()
      if [[ "$SELECTED_FOLDER" == "ALL" ]]; then
        for FOLDER in "${ANDROID_FOLDERS[@]}"; do
          [ -d "$FOLDER" ] && FOUND_IMAGES+=($(find "$FOLDER" -maxdepth 2 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.webp" \) 2>/dev/null | head -n 50))
        done
      else
        while IFS= read -r -d '' IMG; do
          FOUND_IMAGES+=("$IMG")
        done < <(find "$SELECTED_FOLDER" -maxdepth 2 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.webp" \) -print0 2>/dev/null)
      fi

      if [ ${#FOUND_IMAGES[@]} -eq 0 ]; then
        echo "❌ WALANG LARAWAN NA NAHANAP"
        sleep 1.5
        continue
      fi

      # ✅ ILISTA ANG MGA LARAWAN — HANGGANG 20 LANG MUNA
      echo ""
      echo "🖼️ MGA NAHANAP NA LARAWAN:"
      DISPLAY_LIMIT=20
      TOTAL_SHOWN=$(( ${#FOUND_IMAGES[@]} < DISPLAY_LIMIT ? ${#FOUND_IMAGES[@]} : DISPLAY_LIMIT ))
      for ((i=0; i<TOTAL_SHOWN; i++)); do
        FNAME=$(basename "${FOUND_IMAGES[$i]}")
        echo "   [$((i+1))] 🖼️ $FNAME"
      done
      if [ ${#FOUND_IMAGES[@]} -gt $DISPLAY_LIMIT ]; then
        echo "   ... at $(( ${#FOUND_IMAGES[@]} - DISPLAY_LIMIT )) pa — i-type ang numero ng susunod na pahina"
      fi
      echo ""
      read -rp "👉 PILII ANG LARAWAN (NUMERO): " PILI_IMG

      IDX=$((PILI_IMG-1))
      if [ -z "${FOUND_IMAGES[$IDX]}" ]; then
        echo "❌ WALANG LARAWAN SA NUMERONG IYAN"
        sleep 1.5
        continue
      fi

      SELECTED_IMAGE="${FOUND_IMAGES[$IDX]}"
      echo "✅ NAPILI: $(basename "$SELECTED_IMAGE")"
      echo ""

      # ✅ I-PROSESO GAMIT ANG IMAGEMAGICK — TAMANG SUKAT NG APK ICON!
      echo "=========================================="
      echo "   📐 PAGPAPROSESO NG ICON"
      echo "=========================================="
      echo ""

      PROCESSED_DIR="$REPO_DIR/processed-icons"
      mkdir -p "$PROCESSED_DIR"
      OUTPUT_ICON="$PROCESSED_DIR/$ICON_OUTPUT_NAME"

      echo "📐 SUKAT: $ICON_SIZE_XXXHDPI — TAMANG SUKAT NG APK ICON"
      echo "🔄 PINAPROSESO..."

      $MAGICK_CMD "$SELECTED_IMAGE" -resize "$ICON_SIZE_XXXHDPI" -gravity center -background none -extent "$ICON_SIZE_XXXHDPI" "$OUTPUT_ICON" 2>/dev/null

      if [ $? -eq 0 ] && [ -f "$OUTPUT_ICON" ]; then
        echo "✅ TAPOS NA! NAISAVE BILANG: $ICON_OUTPUT_NAME"
        LAST_ICON_PATH="$OUTPUT_ICON"
        echo ""
        identify "$OUTPUT_ICON" | awk '{print "📐 SUKAT NG RESULTA: " $3}'
      else
        echo "❌ NABIGO ANG PAGPAPROSESO NG ICON"
        sleep 1.5
        continue
      fi
      echo ""

      read -rp "✅ HANDANG HAN — IPADALA NA BA AGAD? (y/n): " IPADALA_ICON
      [[ "$IPADALA_ICON" != "y" && "$IPADALA_ICON" != "Y" ]] && {
        echo "✅ NAKA-SAVE NA — ipadala mamaya."
        sleep 1
        continue
      }
      ;;

    # =====================================================
    # 📄 OPSYON 1 — IPIPASTE ANG CODE SA LOOB NG MENU
    # =====================================================
    1)
      echo -e "\n=========================================="
      echo "   📄 IPIPASTE ANG CODE DITO"
      echo "=========================================="
      echo -e "\n👉 I-paste ang code dito. Pagkatapos: Ctrl+D"
      echo "------------------------------------------"
      TEMP_INPUT=$(mktemp)
      cat > "$TEMP_INPUT"
      echo "------------------------------------------"

      if [ ! -s "$TEMP_INPUT" ]; then
        echo "ℹ️ Walang ipinaste."
        rm -f "$TEMP_INPUT"
        sleep 1
        continue
      fi

      FIRST_LINE=$(head -n1 "$TEMP_INPUT")
      TARGET_FILE=""
      DELIM=""

      if echo "$FIRST_LINE" | grep -qE '^cat[[:space:]]+>'; then
        echo "🧹 NAKITA: cat > file — LILINISIN BUO!"
        TARGET_FILE=$(echo "$FIRST_LINE" | sed -E 's/^cat[[:space:]]+>[[:space:]]*//; s/[[:space:]]+<<.*$//')
        TARGET_FILE=$(basename "$TARGET_FILE")
        DELIM=$(echo "$FIRST_LINE" | sed -E 's/^.*<<[[:space:]]*//; s/^['\''"]//; s/['\''"]$//')
        [ -z "$DELIM" ] && DELIM="EOF"
        echo "📄 File: $TARGET_FILE   🔍 Delimiter: [$DELIM]"

        TEMP_CLEAN=$(mktemp)
        tail -n +2 "$TEMP_INPUT" > "$TEMP_CLEAN"
        sed -i "/^${DELIM}\$/,\$d" "$TEMP_CLEAN"
        sed -i "/^[[:space:]]*${DELIM}[[:space:]]*$/d" "$TEMP_CLEAN"
        mv "$TEMP_CLEAN" "$TEMP_INPUT"
      else
        read -rp "👉 PANGALAN NG FILE: " TARGET_FILE
        [ -z "$TARGET_FILE" ] && { rm -f "$TEMP_INPUT"; continue; }
      fi

      mv "$TEMP_INPUT" "$TARGET_FILE"
      echo -e "\n✅ 💾 NAISAVE: $TARGET_FILE"
      echo "------------------------------------------"
      echo "📄 HULING BAHAGI NG FILE:"
      tail -n 3 "$TARGET_FILE"
      echo "------------------------------------------"

      read -rp "👉 IPADALA NA BA AGAD? (y/n): " AGAD
      [[ "$AGAD" != "y" && "$AGAD" != "Y" ]] && {
        echo "✅ Handa na — ipadala mamaya."
        sleep 1
        continue
      }
      ;;

    3) ;;

    4)
      echo -e "\n=========================================="
      echo "   📂 LAHAT NG FOLDER"
      echo "=========================================="
      echo ""
      find . -type d -not -path './.git/*' | sed 's|^\./||' | sort
      echo ""
      read -rp "👉 ENTER para bumalik..."
      continue
      ;;

    5)
      echo -e "\n=========================================="
      echo "   📄 MGA NABAGONG FILE"
      echo "=========================================="
      git status --short
      echo ""
      read -rp "👉 ENTER para bumalik..."
      continue
      ;;

    *)
      echo -e "\n❌ MALING NUMERO"
      sleep 1
      continue
      ;;
  esac

  # ==========================================
  # 📤 KARANIWANG PROSESO NG PAGPAPADALA
  # ==========================================
  echo -e "\n=========================================="
  echo "   📤 PAGPAPADALA SA GITHUB"
  echo "=========================================="

  echo "📥 KUKUNIN ANG PINAKABAGO MULA SA GITHUB..."
  git pull origin main --rebase 2>/dev/null

  mapfile -t ALL_FILES < <(git status --porcelain | sed 's/^...//' | grep -v '^$' | sort)
  if [ ${#ALL_FILES[@]} -eq 0 ]; then
    echo "ℹ️ Walang ipapadala."
    sleep 1
    continue
  fi

  echo -e "\n📋 PILIIN ANG FILE:"
  echo "   [0] ✅ LAHAT NG FILE"
  for i in "${!ALL_FILES[@]}"; do
    echo "   [$((i+1))] 📄 ${ALL_FILES[$i]}"
  done
  echo ""
  read -rp "👉 ILAGAY ANG NUMERO: " PILI_FILES

  SELECTED_FILES=()
  if [[ "$PILI_FILES" == "0" ]]; then
    SELECTED_FILES=("${ALL_FILES[@]}")
  else
    for NUM in $PILI_FILES; do
      IDX=$((NUM-1))
      [ -n "${ALL_FILES[$IDX]}" ] && SELECTED_FILES+=("${ALL_FILES[$IDX]}")
    done
  fi

  if [ ${#SELECTED_FILES[@]} -eq 0 ]; then
    echo "❌ Walang napili."
    sleep 1
    continue
  fi

  # 📂 Piliin ang destinasyon
  echo -e "\n=========================================="
  echo "   📂 SAAN ILALAGAY SA GITHUB?"
  echo "=========================================="
  echo ""

  declare -A FOLDER_MAP
  mapfile -t FOLDERS < <(find . -type d -not -path './.git/*' | sed 's|^\./||' | sort)
  echo "   [0] 🏠 UGAT / ROOT"
  FOLDER_MAP[0]=""
  NUM=1
  for F in "${FOLDERS[@]}"; do
    if [ -n "$F" ]; then
      echo "   [$NUM] 📁 $F/"
      FOLDER_MAP[$NUM]="$F/"
      NUM=$((NUM+1))
    fi
  done
  echo "   [$NUM] ✏️  SARILING PATH"
  echo ""
  read -rp "👉 ILAGAY ANG NUMERO: " PILI_NUM

  DEST_PATH=""
  if [ "$PILI_NUM" -lt "$NUM" ]; then
    DEST_PATH="${FOLDER_MAP[$PILI_NUM]}"
    echo "✅ DESTINASYON: ${DEST_PATH:-🏠 Ugat}"
  else
    read -rp "👉 ILAGAY ANG PATH: " DEST_PATH
    DEST_PATH="${DEST_PATH%/}/"
    echo "✅ DESTINASYON: $DEST_PATH"
  fi

  LAST_DEST_PATH="$DEST_PATH"

  # 📂 Ilipat sa tamang lugar
  FINAL_FILES=()
  for FILE in "${SELECTED_FILES[@]}"; do
    [ ! -f "$FILE" ] && continue
    FILENAME=$(basename "$FILE")
    DEST_FILE="$DEST_PATH$FILENAME"
    mkdir -p "$(dirname "$DEST_FILE")" 2>/dev/null
    if [ "$FILE" != "$DEST_FILE" ]; then
      mv "$FILE" "$DEST_FILE"
    fi
    FINAL_FILES+=("$DEST_FILE")
  done

  # 📋 Buod
  echo -e "\n=========================================="
  echo "   📋 BUOD NG IPAPADALA"
  echo "=========================================="
  echo "   📂 LUGAR: $DEST_PATH"
  for f in "${FINAL_FILES[@]}"; do echo "   📄 $f"; done
  echo ""

  read -rp "✅ IPAPADALA NA BA? (y/n): " SIGURADO
  if [[ "$SIGURADO" != "y" && "$SIGURADO" != "Y" ]]; then
    echo "ℹ️ Hindi ipinadala."
    sleep 1.5
    continue
  fi

  read -rp "👉 MENSAHE NG PAGBABAGO: " MENSAHE
  [ -z "$MENSAHE" ] && MENSAHE="📂 Pag-update — $(date +'%Y-%m-%d %H:%M')"

  git add .
  git commit -m "$MENSAHE"
  git push

  if [ $? -eq 0 ]; then
    echo -e "\n=========================================="
    echo "   ✅✅✅ TAGUMPAY! IPINADALA NA! 🎉"
    echo "=========================================="
    echo ""

    DEST_URL="$GITHUB_BASE/tree/main/$DEST_PATH"
    echo "📋 SAAN MO GUSTONG PUMUNTA NGAYON?"
    echo ""
    echo "   [1] 📂 Buksan sa GITHUB — sa lugar na pinadala"
    echo "   [2] 🚀 Buksan ang ACTIONS PAGE"
    echo "   [3] 🌐 Buksan ang WEBSITE"
    echo "   [0] ❌ Wala — Bumalik sa Menu"
    echo ""
    read -rp "👉 ILAGAY ANG NUMERO: " SAAN_PUPUNTA

    case "$SAAN_PUPUNTA" in
      1)
        echo -e "\n📂 BUKAS SA GITHUB — DIRETSO SA LUGAR NA PINADALA:"
        echo "$DEST_URL"
        command -v termux-open-url &>/dev/null && termux-open-url "$DEST_URL"
        ;;
      2)
        echo -e "\n🚀 BUKAS ANG ACTIONS PAGE:"
        echo "$GITHUB_BASE/actions"
        command -v termux-open-url &>/dev/null && termux-open-url "$GITHUB_BASE/actions"
        ;;
      3)
        echo -e "\n🌐 BUKAS ANG WEBSITE:"
        echo "$GITHUB_WEB"
        command -v termux-open-url &>/dev/null && termux-open-url "$GITHUB_WEB"
        ;;
      *)
        echo -e "\n✅ Bumalik sa Menu..."
        ;;
    esac
    echo ""
    read -rp "👉 ENTER para bumalik sa Menu..."
  else
    echo -e "\n❌ NABIGO ANG PAGPAPADALA — Tingnan ang mensahe sa itaas."
    sleep 2
  fi
done
ENDSCRIPT
chmod +x /data/data/com.termux/files/usr/bin/martopush
