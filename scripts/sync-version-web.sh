#!/usr/bin/env bash
set -euo pipefail

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}bump-version ${NC}]" # [bump-version]

# VERSION file & package.json
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
VERSION_FILE="$ROOT_DIR/VERSION"
PKG_JSON="$ROOT_DIR/datashare-web/package.json"

VERSION="$(cat "$VERSION_FILE")"

node <<EOF
const fs = require('fs');

const pkg = JSON.parse(fs.readFileSync('$PKG_JSON', 'utf8'));
pkg.version = '$VERSION';

fs.writeFileSync(
  '$PKG_JSON',
  JSON.stringify(pkg, null, 2) + '\n'
);
EOF

echo -e "${TAG} ✔ datashare-web version set to ${KEYWORD}$VERSION${NC}"
