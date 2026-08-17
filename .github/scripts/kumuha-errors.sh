#!/bin/bash
set -e

OWNER="${GITHUB_REPOSITORY_OWNER}"
REPO="${GITHUB_REPOSITORY#*/}"
RUN_ID="${WORKFLOW_RUN_ID:-$GITHUB_RUN_ID}"
TOKEN="${GITHUB_TOKEN}"

echo "🔍 Nagsisimula ang detalyadong pag-check — Run ID: $RUN_ID"

# ✅ KAILANGAN NG jq — i-install kung wala
if ! command -v jq &>/dev/null; then
  echo "📦 Nag-i-install ng jq..."
  sudo apt-get update -qq && sudo apt-get install -y -qq jq >/dev/null 2>&1
fi

# ✅ KUNIN ANG DETALYE NG RUN
API_URL="https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID"
CONCLUSION=$(curl -s -H "Authorization: token $TOKEN" "$API_URL" | jq -r .conclusion)

if [ "$CONCLUSION" != "failure" ]; then
  echo "✅ Walang nakitang pagkabigo — tapos na."
  exit 0
fi

echo "❌ NAKITANG PAGKABIGO — kinukuha ang mga detalye..."

# ✅ KUNIN ANG LAHAT NG JOBS — alin ang nabigo?
JOBS_URL="$API_URL/jobs"
JOBS_JSON=$(curl -s -H "Authorization: token $TOKEN" "$JOBS_URL")
FAILED_JOB_IDS=$(echo "$JOBS_JSON" | jq -r '.jobs[] | select(.conclusion=="failure") | .id')

if [ -z "$FAILED_JOB_IDS" ]; then
  ERROR_DETAILS="> Hindi makuha ang tiyak na detalye — tingnan ang Actions page.\n"
else
  ERROR_DETAILS=""
  for JOB_ID in $FAILED_JOB_IDS; do
    JOB_NAME=$(echo "$JOBS_JSON" | jq -r --arg JID "$JOB_ID" '.jobs[] | select(.id==($JID|tonumber)) | .name')
    JOB_URL="https://github.com/$OWNER/$REPO/actions/runs/$RUN_ID/job/$JOB_ID"
    echo "🔍 Sinusuri ang Job: $JOB_NAME — $JOB_URL"
    
    # ✅ KUNIN ANG LOGS NG NABIGONG JOB
    LOGS_URL="$API_URL/jobs/$JOB_ID/logs"
    LOGS_FILE=$(mktemp)
    curl -s -L -H "Authorization: token $TOKEN" -o "$LOGS_FILE" "$LOGS_URL"
    
    # ✅ HANAPIN ANG MGA TIYAK NA LINYANG MAY ERROR — maraming pattern!
    ERROR_LINES=$(grep -n -i -E 'error:|Error: |ERROR:|FAILED|FAILURE|exception:|Exception:|BUILD FAILED|cannot find|unresolved|invalid|returned non-zero|exit code [1-9]|Compilation failed|Execution failed|command failed|not found|permission denied' "$LOGS_FILE" | head -30)
    
    if [ -n "$ERROR_LINES" ]; then
      ERROR_DETAILS+="\n### 🔴 Nabigong Job: $JOB_NAME\n"
      ERROR_DETAILS+="> 🔗 [$JOB_URL]($JOB_URL)\n"
      ERROR_DETAILS+="\`\`\`\n$ERROR_LINES\n\`\`\`\n"
    else
      # Kung walang nakitang salitang error — kunin ang huling 50 linya
      LAST_LINES=$(tail -50 "$LOGS_FILE")
      ERROR_DETAILS+="\n### 🔴 Nabigong Job: $JOB_NAME — Huling 50 linya ng log:\n"
      ERROR_DETAILS+="> 🔗 [$JOB_URL]($JOB_URL)\n"
      ERROR_DETAILS+="\`\`\`\n$LAST_LINES\n\`\`\`\n"
    fi
    
    rm -f "$LOGS_FILE"
  done
fi

# ✅ BUUIN ANG BAGONG ENTRY — DAGDAG LANG SA UNA!
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")
RUN_URL="https://github.com/$OWNER/$REPO/actions/runs/$RUN_ID"
NEW_ENTRY="---\n\n## ❌ Build Nabigo — $DATE\n\n**🔗 Workflow Run:** [$RUN_ID]($RUN_URL)\n\n$ERROR_DETAILS\n"

# ✅ PAGSAMAHIN — DAGDAG SA UNA, HINDI BUBURAHIN ANG LUMANG TALAAN!
if [ -f errors.md ]; then
  # Alisin ang lumang header para hindi dumami
  OLD_CONTENT=$(cat errors.md | sed '/^# Talaan ng mga Error$/d' | sed '/^---$/!b' | sed '1,/^---$/d')
  # Kung walang laman pagkatapos linisin — walang laman na lang
  if [ -z "$(echo "$OLD_CONTENT" | tr -d '[:space:]')" ]; then
    FINAL="# Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n\n$NEW_ENTRY"
  else
    FINAL="# Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n\n$NEW_ENTRY$OLD_CONTENT"
  fi
else
  FINAL="# Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n\n$NEW_ENTRY"
fi

echo -e "$FINAL" > errors.md
echo "✅ Na-update ang errors.md — may detalyadong linya na ng error!"
