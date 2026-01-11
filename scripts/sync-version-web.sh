#!/usr/bin/env bash
set -euo pipefail

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

echo "✔ datashare-web version set to $VERSION"
