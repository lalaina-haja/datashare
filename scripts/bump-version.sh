#!/usr/bin/env bash
# Usage: ./bump-version.sh <type>
# <type> = patch | minor | major

set -e

TYPE=$1 # major|minor|patch
VERSION=$(cat VERSION)

IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"

case "$TYPE" in
  major) MAJOR=$((MAJOR+1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR+1)); PATCH=0 ;;
  patch) PATCH=$((PATCH+1)) ;;
  *) echo "Usage: bump-version.sh {major|minor|patch}"; exit 1 ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
echo "$NEW_VERSION" > VERSION

# Sync version to datashare-web package.json
./scripts/sync-version-web.sh

# Commit the version bump
git commit -am "chore: bump version to $NEW_VERSION"
