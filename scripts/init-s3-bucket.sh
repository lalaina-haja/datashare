#!/bin/bash
set -e

# Message formatting
TITLE='\033[1;92m' 
NC='\033[0m'        
TAG="${NC}[ ${TITLE}init-s3-bucket ${NC}]" 

# Wait for ANY S3 response (lazy loads service)
sleep 5

# ========================================
# UNIVERSAL ENV VAR LOADING (local/CI/prod)
# ========================================
read_env_var() {
  local var_name=$1
  local default=$2
  
  # 1. Try direct env var (CI/prod/K8s)
  if [ ! -z "${!var_name}" ]; then
    echo "${!var_name}"
    return
  fi
  
  # 2. Try .env file (local dev)
  if [ -f "./.env" ] || [ -f "/tmp/config/.env" ]; then
    local env_file="./.env"
    [ -f "/tmp/config/.env" ] && env_file="/tmp/config/.env"
    local val=$(grep "^${var_name}=" "$env_file" | cut -d '=' -f2- | tr -d '"' | head -n1)
    if [ ! -z "$val" ]; then
      echo "$val"
      return
    fi
  fi
  
  # 3. Use default or fail
  if [ ! -z "$default" ]; then
    echo "$default"
  else
    echo -e "${TAG} ✘ Required $var_name not found (env var or .env)" >&2
    exit 1
  fi
}

# CORS file
CORS_CONFIG="./cors.json"
[ -f "/tmp/config/cors.json" ] && CORS_CONFIG="/tmp/config/cors.json"

# Read values
BUCKET_NAME=$(read_env_var "AWS_S3_BUCKET")
FRONTEND_URL=$(read_env_var "APP_HOST"):$(read_env_var "WEB_PORT")

echo -e "${TAG} 📦 Bucket: $BUCKET_NAME | 🌐 $FRONTEND_URL"

# ========================================
# DYNAMIC CORS CONFIG
# ========================================
cp "$CORS_CONFIG" /tmp/cors-dynamic.json
sed -i "s|frontend-url|$FRONTEND_URL|g" /tmp/cors-dynamic.json

echo -e "${TAG} Creating bucket: $BUCKET_NAME..."


# Create bucket + CORS
BUCKET_OUTPUT=$(awslocal s3 mb "s3://$BUCKET_NAME")
echo -e "${TAG} S3 Create: $BUCKET_OUTPUT"

LIST_OUTPUT=$(awslocal s3 ls 2>&1)
echo -e "${TAG} S3-List: $LIST_OUTPUT"

CORS_OUTPUT=$(awslocal s3api put-bucket-cors \
  --bucket "$BUCKET_NAME" \
  --cors-configuration file:///tmp/cors-dynamic.json)
echo -e "${TAG} S3 CORS: $CORS_OUTPUT"

echo -e "${TAG} ✔ Bucket '$BUCKET_NAME' configured for '$FRONTEND_URL'"