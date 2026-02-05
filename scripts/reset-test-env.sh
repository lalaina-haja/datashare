#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# echo message formatting
source "$SCRIPT_DIR/message-format.sh"
TAG="${NC}[ ${TITLE}reset-test-env ${NC}]" # [reset-test-env]

echo ""
echo "═══════════════════════════════════════════════════"
echo -e "${TAG} 🧹 Resetting Test Environment"
echo "═══════════════════════════════════════════════════"
echo ""

# Reset Database
if [ -f "$SCRIPT_DIR/reset-database.sh" ]; then
    bash "$SCRIPT_DIR/reset-database.sh"
else
    echo -e "${TAG}⚠️  Warning: reset-database.sh not found"
fi

echo ""

# Reset S3
if [ -f "$SCRIPT_DIR/reset-s3.sh" ]; then
    bash "$SCRIPT_DIR/reset-s3.sh"
else
    echo -e "${TAG}⚠️  Warning: reset-s3.sh not found"
fi

echo ""
echo "═══════════════════════════════════════════════════"
echo -e "${TAG} ✔ Test environment reset complete"
echo "═══════════════════════════════════════════════════"
echo ""