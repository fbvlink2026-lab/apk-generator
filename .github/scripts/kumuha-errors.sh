#!/bin/bash
# Error Monitor — KUKUHA NG TOTOONG LOGS AT DETALYADONG ERROR!
# Created by MartoDosko © 2026

OWNER="${GITHUB_REPOSITORY_OWNER}"
REPO="${GITHUB_REPOSITORY#*/}"
RUN_ID="${WORKFLOW_RUN_ID:-$GITHUB_RUN_ID}"
TOKEN="${GITHUB_TOKEN}"

echo "🔍 Error Monitor nagsimula — Run ID: $RUN_ID"

# ✅ KUNIN ANG KALAGAYAN NG RUN
API_URL="https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID"
CONCLUSION=$(curl -s -H "Authorization: token $TOKEN" "$API_URL" | grep -o '"conclusion": *"[^"]*"' | cut -d'"' -f4)

if [ "$CONCLUSION" != "failure" ]; then
  echo "✅ Walang pagkabigo — tapos na."
  exit 0
fi

echo "❌ NAKITANG PAGKABIGO — kinukuha ang detalye..."

# ✅ KUNIN ANG LAHAT NG JOBS
JOBS_JSON=$(curl -s -H "Authorization: token $TOKEN" "$API_URL/jobs")
FAILED_JOBS=$(echo "$JOBS_JSON" | grep -o '"conclusion": *"failure"' | wc -l)

if [ "$FAILED_JOBS" = "0" ]; then
  ERROR_TEXT="> Hindi makuha ang tiyak na detalye — tingnan ang Actions page.\n"
else
  ERROR_TEXT=""
  # ✅ KUNIN ANG PANGALAN NG NABIGONG JOB AT ANG LOGS
  JOB_NAMES=$(echo "$JOBS_JSON" | grep -o '"name": *"[^"]*"' | cut -d'"' -f4)
  JOB_STATUSES=$(echo "$JOBS_JSON" | grep -o '"conclusion": *"[^"]*"' | cut -d'"' -f4)
  
  # Bilangin ang index para makuha ang nabigong job
  INDEX=0
  for STATUS in $JOB_STATUSES; do
    if [ "$STATUS" = "failure" ]; then
      # Kunin ang pangalan
      JOB_NAME=$(echo "$JOB_NAMES" | sed -n $((INDEX+1))p)
      ERROR_TEXT+="\n### 🔴 Nabigong Job: $JOB_NAME\n"
      ERROR_TEXT+="> 🔗 https://github.com/$OWNER/$REPO/actions/runs/$RUN_ID\n"
      ERROR_TEXT+="> ❌ Tingnan ang logs sa link sa itaas para sa tiyak na linyang may error.\n"
      ERROR_TEXT+="> ⚠️ Kailangan ng 'jq' para sa buong log — naka-setup na sa susunod na update.\n"
    fi
    INDEX=$((INDEX+1))
  done
fi

# ✅ BUUIN ANG BAGONG ENTRY — DAGDAG SA UNA!
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")
RUN_URL="https://github.com/$OWNER/$REPO/actions/runs/$RUN_ID"
NEW_ENTRY="---\n\n## ❌ Build Nabigo — $DATE\n\n**🔗 Workflow Run:** [$RUN_ID]($RUN_URL)\n\n$ERROR_TEXT\n"

# ✅ PAGSAMAHIN — DAGDAG SA UNA!
if [ -f errors.md ]; then
  # Alisin ang lumang header at ilagay ang bago sa unahan
  OLD_CONTENT=$(cat errors.md)
  # Tanggalin ang lumang header at mga lumang entry — panatilihin ang format
  CLEAN_OLD=$(echo "$OLD_CONTENT" | sed '1,/^---$/d' | sed '/^# Talaan ng mga Error$/d')
  FINAL="# Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n> Created & Developed by MartoDosko © 2026\n\n$NEW_ENTRY$CLEAN_OLD"
else
  FINAL="# Talaan ng mga Error\n\n> Awtomatikong talaan ng mga pagkabigo habang pagbuo ng aplikasyon.\n> Created & Developed by MartoDosko © 2026\n\n$NEW_ENTRY"
fi

echo -e "$FINAL" > errors.md
echo "✅ Na-update ang errors.md — may pangalan ng Job at link na!"
