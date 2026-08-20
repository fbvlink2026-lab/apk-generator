#!/bin/bash
# ==========================================
# ✅ MARTOPUSH v3.4 — INAAYOS ANG LAHAT NG QUOTE ERROR!
# 📤 Pagpadala → Tanong kung saan pupunta → Diretso sa tamang lugar!
# 📄 Sa loob na ng menu ang pag-paste
# 🛡️ Ligtas — hindi mabubura ang ipapadala!
# ==========================================

clear
VERSION="v3.4 — Walang quote error + Diretso sa tamang lugar"
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
    echo ""
  fi
  echo "   📋 ANO ANG GUSTO MONG GAWIN?"
  echo ""
  echo "   ┌──────────────────────────────────────────────┐"
  echo "   │  1. 📄 IPIPASTE ANG CODE → I-SAVE AGAD         │"
  echo "   │  2. 📤 IPADALA ANG FILE NA NASA TERMUX         │"
  echo "   │  3. 📂 TIGNAN ANG LAHAT NG FOLDER              │"
  echo "   │  4. 📄 TIGNAN ANG MGA NABAGONG FILE            │"
  echo "   │  0. ❌ TAPOS NA / LUMABAS                      │"
  echo "   └──────────────────────────────────────────────┘"
  echo ""
  read -rp "👉 ILAGAY ANG NUMERO: " PILI_MENU

  case "$PILI_MENU" in
    0)
      echo -e "\n✅ TAPOS NA!"
      exit 0
      ;;

    1)
      echo -e "\n=========================================="
      echo "   📄 IPIPASTE ANG CODE DITO"
      echo "=========================================="
      echo -e "\n👉 I-paste ang code dito. Pagkatapos: Ctrl+D"
      echo "------------------------------------------"
      TEMP_INPUT=$(mktemp)
      cat > "$TEMP_INPUT"
      echo "------------------------------------------"

      [ ! -s "$TEMP_INPUT" ] && {
        echo "ℹ️ Walang ipinaste."
        rm -f "$TEMP_INPUT"
        sleep 1
        continue
      }

      FIRST_LINE=$(head -n1 "$TEMP_INPUT")
      TARGET_FILE=""

      if echo "$FIRST_LINE" | grep -qE '^cat[[:space:]]+>'; then
        # Kunin ang pangalan ng file mula sa: cat > filename << ...
        TARGET_FILE=$(echo "$FIRST_LINE" | sed -E 's/^cat[[:space:]]+>[[:space:]]*//; s/[[:space:]]+<<.*$//')
        TARGET_FILE=$(basename "$TARGET_FILE")
        DELIM=$(echo "$FIRST_LINE" | sed -E 's/^.*<<[[:space:]]*//')
        [ -z "$DELIM" ] && DELIM="EOF"
        # Tanggalin ang unang linya at huling delimiter
        TEMP_CLEAN=$(mktemp)
        tail -n +2 "$TEMP_INPUT" > "$TEMP_CLEAN"
        # Tanggalin ang huling delimiter line
        sed -i "/^${DELIM}\$/d" "$TEMP_CLEAN"
        mv "$TEMP_CLEAN" "$TEMP_INPUT"
      else
        read -rp "👉 PANGALAN NG FILE: " TARGET_FILE
        [ -z "$TARGET_FILE" ] && {
          rm -f "$TEMP_INPUT"
          continue
        }
      fi

      mv "$TEMP_INPUT" "$TARGET_FILE"
      echo -e "\n✅ 💾 NAISAVE: $TARGET_FILE"
      read -rp "👉 IPADALA NA BA AGAD? (y/n): " AGAD
      [[ "$AGAD" != "y" && "$AGAD" != "Y" ]] && {
        echo "✅ Handa na — ipadala mamaya."
        sleep 1
        continue
      }
      ;;

    3)
      echo -e "\n=========================================="
      echo "   📂 LAHAT NG FOLDER"
      echo "=========================================="
      echo ""
      find . -type d -not -path './.git/*' | sed 's|^\./||' | sort
      echo ""
      read -rp "👉 ENTER para bumalik..."
      continue
      ;;

    4)
      echo -e "\n=========================================="
      echo "   📄 MGA NABAGONG FILE"
      echo "=========================================="
      git status --short
      echo ""
      read -rp "👉 ENTER para bumalik..."
      continue
      ;;

    2)
      ;;

    *)
      echo -e "\n❌ MALING NUMERO"
      sleep 1
      continue
      ;;
  esac

  # ==========================================
  # 📤 PAGPAPADALA
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
      if [ -n "${ALL_FILES[$IDX]}" ]; then
        SELECTED_FILES+=("${ALL_FILES[$IDX]}")
      fi
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
  for f in "${FINAL_FILES[@]}"; do
    echo "   📄 $f"
  done
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
