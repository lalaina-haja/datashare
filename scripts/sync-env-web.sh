#!/usr/bin/env bash

set -euo pipefail

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}sync-env-web ${NC}]" # [sync-env-web]

# .env file & environment.dev.ts
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
ENV_LOCAL_FILE="$ROOT_DIR/datashare-web/src/environments/environment.dev.ts"

if [[ ! -f "$ENV_FILE" ]]; then
  echo -e "${TAG} x Env file file missing : ${KEYWORD}$ENV_FILE${NC}"
  echo ""
  exit 1
fi

if [[ ! -f "$ENV_LOCAL_FILE" ]]; then
  echo -e "${TAG} x environment.dev.ts file missing : ${KEYWORD}$ENV_LOCAL_FILE${NC}"
  echo ""
  exit 1
fi

# Read ports from .env
WEB_PORT=$(grep '^WEB_PORT=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
API_PORT=$(grep '^API_PORT=' "$ENV_FILE" | cut -d'=' -f2 | xargs)

WEB_PORT=${WEB_PORT:-4200}
API_PORT=${API_PORT:-8080}

echo -e "${TAG} Updating environment.dev.ts"
echo -e "${TAG}   WEB_PORT=${KEYWORD}$WEB_PORT${NC}"
echo -e "${TAG}   API_PORT=${KEYWORD}$API_PORT${NC}"

# Replace apiUrl and baseUrl in environment.local.ts
sed -E \
  -e "s|apiUrl: *'[^']*'|apiUrl: 'http://localhost:${API_PORT}'|" \
  -e "s|baseUrl: *'[^']*'|baseUrl: 'http://localhost:${WEB_PORT}'|" \
  "$ENV_LOCAL_FILE" > "$ENV_LOCAL_FILE.tmp" \
  && mv "$ENV_LOCAL_FILE.tmp" "$ENV_LOCAL_FILE"

echo -e "${TAG} ✔ environment.dev.ts updated"
echo ""
