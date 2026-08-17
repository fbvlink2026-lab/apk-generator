#!/bin/bash
OWNER="${GITHUB_REPOSITORY_OWNER}"
REPO="${GITHUB_REPOSITORY#*/}"
RUN_ID="${WORKFLOW_RUN_ID:-$GITHUB_RUN_ID}"
echo "🔍 Checking Run: $RUN_ID"
[ -z "$GITHUB_TOKEN" ] && echo "⚠️ No token" && exit 0
! command -v jq &>/dev/null && (sudo apt-get update -qq && sudo apt-get install -y -qq jq >/dev/null 2>&1)
CONCLUSION=$(curl -s -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID" | jq -r .conclusion)
[ "$CONCLUSION" != "failure" ] && echo "✅ No error" && exit 0
echo "❌ Failed — writing to errors.md"
DATE=$(date -u +"%Y-%m-%d %H:%M UTC")
NEW="---\n## ❌ Build Failed — $DATE\n**Run ID:** $RUN_ID\n> Check Actions log for details.\n"
HEADER="# Talaan ng mga Error\n\n📋 Listahan ng mga pagkakamali habang pagbuo ng aplikasyon.\n"
FOOTER="\n---\nCreated by MartoDosko © Copyright 2026"
echo -e "$HEADER$NEW$FOOTER" > errors.md
echo "✅ Updated errors.md"
