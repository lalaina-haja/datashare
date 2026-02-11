#!/usr/bin/env bash

set -euo pipefail

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}sync-env-web ${NC}]" # [sync-env-web]

# .env & angular environment
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
ENV_LOCAL_FILE="$ROOT_DIR/datashare-web/config/env/environment.ts"
ENV_LOCAL_DEV_FILE="$ROOT_DIR/datashare-web/config/env/environment.dev.ts"

# Check for required files
if [[ ! -f "$ENV_FILE" ]]; then
  echo -e "${TAG} ✘ Env file file missing : ${KEYWORD}$ENV_FILE${NC}"
  echo ""
  exit 1
fi

if [[ ! -f "$ENV_LOCAL_FILE" ]]; then
  echo -e "${TAG} ✘ environment.ts file missing : ${KEYWORD}$ENV_LOCAL_FILE${NC}"
  echo ""
  exit 1
fi

# Read variables from .env 
API_PORT=$(grep '^API_PORT=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
WEB_PORT=$(grep '^WEB_PORT=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
APP_HOST=$(grep '^APP_HOST=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
APP_NAME=$(grep '^APP_NAME=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
TEST_EMAIL=$(grep '^TEST_EMAIL=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
TEST_PASS=$(grep '^TEST_PASS=' "$ENV_FILE" | cut -d'=' -f2 | xargs)
BUCKET_NAME=$(grep '^AWS_S3_BUCKET=' "$ENV_FILE" | cut -d'=' -f2 | xargs)

# Defaults & overrides
API_PORT=${API_PORT:-8080}
WEB_PORT=${WEB_PORT:-4200}
APP_HOST=${APP_HOST:-http://localhost}
APP_NAME=${APP_NAME:-DataShare}
API_URL=${APP_HOST}:${API_PORT}
BASE_URL=${APP_HOST}:${WEB_PORT}
TEST_EMAIL=${CYPRESS_TEST_EMAIL:-${TEST_EMAIL}}
TEST_PASS=${CYPRESS_TEST_PASS:-${TEST_PASS}}
BUCKET_NAME=${BUCKET_NAME:-datashare-bucket}

echo -e "${TAG} Updating environment.ts"
echo -e "${TAG}   APP_NAME=${KEYWORD}$APP_NAME${NC}"
echo -e "${TAG}   API_PORT=${KEYWORD}$API_PORT${NC}"
echo -e "${TAG}   WEB_PORT=${KEYWORD}$WEB_PORT${NC}"
echo -e "${TAG}   API_URL=${KEYWORD}$API_URL${NC}"
echo -e "${TAG}   BASE_URL=${KEYWORD}$BASE_URL${NC}"
echo -e "${TAG}   TEST_EMAIL=${KEYWORD}$TEST_EMAIL${NC}"
echo -e "${TAG}   TEST_PASS=${KEYWORD}$TEST_PASS${NC}"
echo -e "${TAG}   BUCKET_NAME=${KEYWORD}$BUCKET_NAME${NC}"

# Replace values in environment.ts
sed -E \
  -e "s|appName:.*|appName: \"${APP_NAME}\",|" \
  -e "s|apiPort:.*|apiPort: ${API_PORT},|" \
  -e "s|webPort:.*|webPort: ${WEB_PORT},|" \
  -e "s|apiUrl:.*|apiUrl: \`${APP_HOST}:${API_PORT}\`,|" \
  -e "s|baseUrl:.*|baseUrl: \`${APP_HOST}:${WEB_PORT}\`,|" \
  -e "s|testEmail:.*|testEmail: \"${TEST_EMAIL}\",|" \
  -e "s|testPass:.*|testPass: \"${TEST_PASS}\",|" \
  -e "s|bucketName:.*|bucketName: \"${BUCKET_NAME}\",|" \
  "$ENV_LOCAL_FILE" > "$ENV_LOCAL_FILE.tmp" \
  && mv "$ENV_LOCAL_FILE.tmp" "$ENV_LOCAL_FILE"

# Duplication environment.ts → environment.dev.ts
cp -f "$ENV_LOCAL_FILE" "$ENV_LOCAL_DEV_FILE"

echo -e "${TAG} ✔ environment.ts updated successfully."
echo ""
