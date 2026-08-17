#!/data/data/com.termux/files/usr/bin/bash

OWNER="fbvlink2026-lab"
REPO="apk-generator"
PER_PAGE="10"

echo "🔍 Tinitingnan ang mga nabigong pagbuo..."

# ✅ KUKUHA NG LISTA NG MGA NABIGONG RUN
RUNS=$(curl -s "https://api.github.com/repos/$OWNER/$REPO/actions/runs?status=failure&per_page=$PER_PAGE")

if [ -z "$RUNS" ] || [ "$RUNS" = "[]" ]; then
  echo "✅ WALANG NABIGONG PAGBUO!"
  echo "# Talaan ng mga Error

✅ WALANG NABIGONG PAGBUO — Lahat ay matagumpay na naipagawa!

---
📋 Gabay at Listahan ng Paggawa
Created by MartoDosko © Copyright 2026" > docs/errors.md
  exit 0
fi

OUTPUT="# Talaan ng mga Error

"

# ✅ BAWAT RUN — KUKUHA NG DETALYE AT LOG
echo "$RUNS" | grep '"id"' | cut -d'"' -f4 | while read -r RUN_ID; do
  [ -z "$RUN_ID" ] && continue

  # ✅ KUKUHA NG IMPORMASYON TUNGKOL SA RUN
  RUN_INFO=$(curl -s "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID")
  CREATED=$(echo "$RUN_INFO" | grep '"created_at"' | head -1 | cut -d'"' -f4)
  DATE_PART=$(echo "$CREATED" | cut -d'T' -f1)
  TIME_PART=$(echo "$CREATED" | cut -d'T' -f2 | cut -d'Z' -f1)

  echo "❌ Pinoproseso: $DATE_PART $TIME_PART UTC"

  OUTPUT+="---

## ❌ Nabigo — $DATE_PART $TIME_PART UTC

**Run ID:** $RUN_ID

"

  # ✅ KUKUHA NG MGA JOBS — TIGNAN ALIN ANG NABIGO
  JOBS=$(curl -s "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID/jobs?per_page=100")
  FAILED_JOB_IDS=$(echo "$JOBS" | grep '"conclusion": "failure"' -B50 | grep '"id"' | cut -d'"' -f4)

  for JOB_ID in $FAILED_JOB_IDS; do
    # ✅ KUKUHA NG LOG NG NABIGONG JOB
    LOG=$(curl -sL "https://api.github.com/repos/$OWNER/$REPO/actions/jobs/$JOB_ID/logs")

    if [ -n "$LOG" ]; then
      # ✅ SALAIN — MGA LAMANG LINYANG MAY ERROR O BULLET NA PULANG MARKA
      ERR_LINES=$(echo "$LOG" | grep -iE '^.*(error|failed|fatal|cannot|unable|exception):' | head -20)
      if [ -n "$ERR_LINES" ]; then
        OUTPUT+="### Detalye ng Error:
\`\`\`
$ERR_LINES
\`\`\`
"
      else
        OUTPUT+="### Detalye ng Error:
> Hindi makita ang tiyak na linyang may error — tingnan ang buong log sa GitHub.
"
      fi
    fi
  done

done

OUTPUT+="
---
📋 Gabay at Listahan ng Paggawa
Created by MartoDosko © Copyright 2026"

# ✅ ISUSULAT SA docs/errors.md
echo "$OUTPUT" > docs/errors.md

echo ""
echo "✅ TAPOS NA! NAILAGAY NA ANG DETALYE NG ERROR SA docs/errors.md"
