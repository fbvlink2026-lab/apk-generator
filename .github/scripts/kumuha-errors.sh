#!/bin/bash
set -e

OWNER="${GITHUB_REPOSITORY_OWNER}"
REPO="${GITHUB_REPOSITORY#*/}"
RUN_ID="${GITHUB_RUN_ID}"
BRANCH="${GITHUB_HEAD_REF:-${GITHUB_REF#refs/heads/}}"

echo "🔍 Tinitingnan ang Run ID: $RUN_ID..."

RUN_INFO=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
  "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID")

CONCLUSION=$(echo "$RUN_INFO" | grep '"conclusion"' | head -1 | cut -d'"' -f4)
CREATED=$(echo "$RUN_INFO" | grep '"created_at"' | head -1 | cut -d'"' -f4)
DATE_PART=$(echo "$CREATED" | cut -d'T' -f1)
TIME_PART=$(echo "$CREATED" | cut -d'T' -f2 | cut -d'Z' -f1)

if [ "$CONCLUSION" != "failure" ]; then
  echo "✅ Walang error — hindi babaguhin ang errors.md"
  exit 0
fi

echo "❌ Nabigo ang pagbuo — kinukuha ang mga linyang may error..."

JOBS=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
  "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID/jobs?per_page=100")

FAILED_IDS=$(echo "$JOBS" | grep '"conclusion": "failure"' -B50 | grep '"id"' | cut -d'"' -f4)

ERROR_LINES=""

for JOB_ID in $FAILED_IDS; do
  JOB_NAME=$(echo "$JOBS" | grep -A30 "\"id\": $JOB_ID" | grep '"name"' | head -1 | cut -d'"' -f4)
  echo "🔍 Nabigong trabaho: $JOB_NAME"

  LOG=$(curl -sL -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO/actions/jobs/$JOB_ID/logs")

  FOUND=$(echo "$LOG" | grep -iE '^.*(error|failed|fatal|cannot|unable|exception|missing|permission|denied):' | head -30)
  
  if [ -n "$FOUND" ]; then
    ERROR_LINES+="
---

## ❌ Nabigo — $DATE_PART $TIME_PART UTC

**Trabaho:** $JOB_NAME
**Run ID:** $RUN_ID

### Mga linyang may Error:
\`\`\`
$FOUND
\`\`\`
"
  fi
done

if [ -z "$ERROR_LINES" ]; then
  ERROR_LINES="
---

## ❌ Nabigo — $DATE_PART $TIME_PART UTC

**Run ID:** $RUN_ID
> Hindi mahanap ang tiyak na linyang may error — tingnan ang buong log sa GitHub Actions.
"
fi

NEW_CONTENT="# Talaan ng mga Error

$ERROR_LINES

---
📋 Gabay at Listahan ng Paggawa
Created by MartoDosko © Copyright 2026
"

# ✅ ANG errors.md MO AY NASA UGAT — HINDI SA docs/
echo "$NEW_CONTENT" > errors.md

echo "✅ Nailagay na ang detalye sa errors.md"

git config --global user.name "github-actions[bot]"
git config --global user.email "github-actions[bot]@users.noreply.github.com"

git add errors.md
git commit -m "🔄 Auto-update: naitala ang error mula Run $RUN_ID" || true
git push "https://$GITHUB_ACTOR:$GITHUB_TOKEN@github.com/$OWNER/$REPO.git" HEAD:$BRANCH || true

echo "✅ Tapos na ang script"
