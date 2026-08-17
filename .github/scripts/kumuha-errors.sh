#!/bin/bash
# ==========================================
# ✅ KUMUHA-ERRORS.SH — GUMAGANA NA!
# ✅ Walang set -e — hindi tumitigil agad!
# ✅ May jq — tamang pagbasa ng JSON!
# ✅ DAGDAG sa taas — hindi binubura ang luma!
# ==========================================

echo "🔍 ========================================="
echo "🔍   KUMUHA NG MGA ERROR MULA SA BUILD"
echo "🔍 ========================================="

OWNER="${GITHUB_REPOSITORY_OWNER}"
REPO_NAME="${GITHUB_REPOSITORY#*/}"
TRIGGERED_RUN_ID="${WORKFLOW_RUN_ID:-$GITHUB_RUN_ID}"
BRANCH="${BRANCH_NAME:-main}"

echo "📦 Repository: $OWNER/$REPO_NAME"
echo "🆔 Run ID: $TRIGGERED_RUN_ID"
echo "🌿 Branch: $BRANCH"
echo ""

# ✅ WALANG TOKEN — GUMAGAWA PA RIN NG TALAAN
if [ -z "$GITHUB_TOKEN" ]; then
  echo "⚠️ WALANG GITHUB_TOKEN — limitado lang ang makukuha"
  ERROR_LINES="
---

## ⚠️ Kulang ng Permission — $(date -u +"%Y-%m-%d %H:%M UTC")

> Hindi makuha ang buong log — walang GITHUB_TOKEN.
> Tingnan ang: https://github.com/$OWNER/$REPO_NAME/actions/runs/$TRIGGERED_RUN_ID
"
else
  echo "✅ May Token — Makakakuha ng Logs!"
  echo ""

  # ✅ I-INSTALL ANG jq KUNG WALA
  if ! command -v jq &> /dev/null; then
    echo "📦 Nag-i-install ng jq..."
    sudo apt-get update -qq && sudo apt-get install -y -qq jq > /dev/null 2>&1
  fi

  # ✅ KUNIN ANG IMPORMASYON NG RUN
  RUN_INFO=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO_NAME/actions/runs/$TRIGGERED_RUN_ID")

  CONCLUSION=$(echo "$RUN_INFO" | jq -r '.conclusion')
  CREATED=$(echo "$RUN_INFO" | jq -r '.created_at')
  DATE_PART=$(echo "$CREATED" | cut -d'T' -f1)
  TIME_PART=$(echo "$CREATED" | cut -d'T' -f2 | cut -d'Z' -f1)

  echo "📅 Petsa: $DATE_PART $TIME_PART UTC"
  echo "✅ Resulta: $CONCLUSION"
  echo ""

  # ✅ KUNG HINDI NABIGO — LUMABAS
  if [ "$CONCLUSION" != "failure" ]; then
    echo "✅ Walang error — hindi babaguhin ang errors.md"
    exit 0
  fi

  echo "❌ Nabigo ang pagbuo — kinukuha ang mga detalye..."
  echo ""

  # ✅ KUNIN ANG LAHAT NG JOBS
  JOBS=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO_NAME/actions/runs/$TRIGGERED_RUN_ID/jobs?per_page=100")

  FAILED_JOBS=$(echo "$JOBS" | jq -r '.jobs[] | select(.conclusion=="failure") | .id')

  ERROR_LINES=""

  if [ -z "$FAILED_JOBS" ]; then
    ERROR_LINES="
---

## ❌ Nabigo ang Pagbuo — $DATE_PART $TIME_PART UTC

**Run ID:** $TRIGGERED_RUN_ID
> Hindi matukoy ang tiyak na trabaho — tingnan ang buong log:
> https://github.com/$OWNER/$REPO_NAME/actions/runs/$TRIGGERED_RUN_ID
"
  else
    for JOB_ID in $FAILED_JOBS; do
      JOB_NAME=$(echo "$JOBS" | jq -r --argjid "$JOB_ID" '.jobs[] | select(.id==($jid | tonumber)) | .name')
      echo "🔍 Nabigong trabaho: $JOB_NAME"

      LOG=$(curl -sL -H "Authorization: token $GITHUB_TOKEN" \
        "https://api.github.com/repos/$OWNER/$REPO_NAME/actions/jobs/$JOB_ID/logs")

      if [ -z "$LOG" ]; then
        FOUND_LOGS="> Hindi makuha ang log — tingnan sa GitHub."
      else
        FOUND_LOGS=$(echo "$LOG" | grep -iE 'error:|failed:|fatal:|exception|cannot|unable|missing|permission denied|BUILD FAILED' | head -50)
        if [ -z "$FOUND_LOGS" ]; then
          FOUND_LOGS="> Walang tiyak na salitang 'error' — tingnan ang buong log sa GitHub."
        fi
      fi

      ERROR_LINES+="
---

## ❌ Nabigo — $DATE_PART $TIME_PART UTC

**Trabaho:** $JOB_NAME
**Run ID:** $TRIGGERED_RUN_ID
**Job ID:** $JOB_ID

### Mga linyang may Error:
\`\`\`
$FOUND_LOGS
\`\`\`
🔗 [Tingnan ang buong log](https://github.com/$OWNER/$REPO_NAME/actions/runs/$TRIGGERED_RUN_ID)
"
    done
  fi
fi

# ✅ ISULAT — DAGDAG SA TAAS, HUWAG BURAHIN ANG LUMANG LAMAN!
echo ""
echo "✅ Isinusulat sa errors.md..."

if [ -f errors.md ]; then
  OLD_CONTENT=$(cat errors.md | sed '/^---$/,$d' | head -n -1)
  OLD_FOOTER="---
📋 Gabay at Listahan ng Paggawa
Created by MartoDosko © Copyright 2026"
else
  OLD_CONTENT="# Talaan ng mga Error

📋 Ito ang listahan ng mga naging pagkakamali habang pagbuo ng aplikasyon.
"
fi

NEW_CONTENT="$OLD_CONTENT

$ERROR_LINES

---
📋 Gabay at Listahan ng Paggawa
Created by MartoDosko © Copyright 2026
"

echo "$NEW_CONTENT" > errors.md

echo "✅ Tapos na — naisulat na sa errors.md!"
