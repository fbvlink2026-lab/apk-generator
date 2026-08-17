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
    echo "🔍 Sinusuri ang Job: $JOB_NAME"
    
    # ✅ KUNIN ANG LOGS NG NABIGONG JOB
    LOGS_URL="$API_URL/jobs/$JOB_ID/logs"
    LOGS_FILE=$(mktemp)
    curl -s -L -H "Authorization: token $TOKEN" -o "$LOGS_FILE" "$LOGS_URL"
    
    # ✅ HANAPIN ANG MGA TIYAK NA LINYANG MAY ERROR
    ERROR_LINES=$(grep -n -i -E 'error:|Error:|ERROR:|FAILED|FAILURE|exception:|Exception:|BUILD FAILED|cannot find|unresolved|invalid|returned non-zero|exit code [1-9]|Compilation failed|Execution failed' "$LOGS_FILE" | head -30)
    
    if [ -n "$ERROR_LINES" ]; then
      ERROR_DETAILS+="\n### 🔴 Job: $JOB_NAME\n"
      ERROR_DETAILS+="\`\`\`\n$ERROR_LINES\n\`\`\`\n"
    else
      # Kung walang nakitang salitang error — kunin ang huling 50 linya
      LAST_LINES=$(tail -50 "$LOGS_FILE")
      ERROR_DETAILS+="\n### 🔴 Job: $JOB_NAME — Huling 50 linya ng log:\n"
      ERROR_DETAILS+="\`\`\`\n$LAST_LINES\n\`\`\`\n"
    fi
    
    rm -f "$LOGS_FILE"
  done
fi

# ✅ BUUIN ANG BAGONG ERRORS.MD — DAGDAG SA UNA
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")
HEADER="# 📋 Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n\n"
FOOTER="\n---\nCreated & Developed by MartoDosko © Copyright 2026"
NEW_ENTRY="---\n\n## ❌ Build Nabigo — $DATE\n\n**Workflow Run ID:** [$RUN_ID](https://github.com/$OWNER/$REPO/actions/runs/$RUN_ID)\n\n$ERROR_DETAILS\n"

# ✅ DAGDAG LANG — HINDI BUBURAHIN ANG LUMANG TALAAN
if [ -f errors.md ]; then
  # Alisin ang lumang header para hindi dumami
  OLD_CONTENT=$(cat errors.md | sed '/^# 📋 Talaan ng mga Error$/d' | sed '/^---$/d' | sed '/^Created & Developed by MartoDosko/d' | sed '/^Copyright 2026/d')
  # Tanggalin ang sobrang bagong linya sa simula
  OLD_CONTENT=$(echo "$OLD_CONTENT" | sed '/^[[:space:]]*$/s/^$//' | sed '/./,$!d')
  FINAL="$HEADER$NEW_ENTRY$OLD_CONTENT"
else
  FINAL="$HEADER$NEW_ENTRY"
fi

echo -e "$FINAL$FOOTER" > errors.md
echo "✅ Na-update ang errors.md — may detalyadong linya na ng error!"
