#!/usr/bin/env bash
set -euo pipefail

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}bump-version ${NC}]" # [bump-version]

# VERSION file & pom.xml
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
VERSION_FILE="$ROOT_DIR/VERSION"
PKG_JSON="$ROOT_DIR/datashare-api/pom.xml"

VERSION="$(cat "$VERSION_FILE")"
sed -i.bak "s|<revision>.*</revision>|<revision>$VERSION</revision>|g" datashare-api/pom.xml && rm datashare-api/pom.xml.bak

echo -e "${TAG} ✔ datashare-api version set to ${KEYWORD}$VERSION${NC}"