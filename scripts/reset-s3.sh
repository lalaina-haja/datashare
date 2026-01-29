#!/bin/bash
set -e

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}reset-s3 ${NC}]" # [reset-s3]

# Configuration
LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT:-http://localhost:4566}
BUCKET_NAME=${AWS_S3_BUCKET:-test-uploads}
AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-test}
AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-test}
AWS_DEFAULT_REGION=${AWS_REGION:-eu-west-3}

echo -e "${TAG}  Resetting LocalStack S3..."
echo -e "   Endpoint: ${KEYWORD}$LOCALSTACK_ENDPOINT${NC}"
echo -e "   Bucket: ${KEYWORD}$BUCKET_NAME${NC}"

# Check if LocalStack is accessible
if ! curl -s -o /dev/null -w "%{http_code}" $LOCALSTACK_ENDPOINT/health | grep -q "200"; then
    echo -e "${TAG} ERROR: LocalStack is not accessible at $LOCALSTACK_ENDPOINT"
    echo ""
    exit 1
fi

# Export AWS credentials
export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
export AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION

# Delete all bucket objects
echo -e "${TAG} Deleting all objects..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 rm s3://$BUCKET_NAME --recursive 2>/dev/null || true

# Delete the bucket
echo -e "${TAG} Deleting bucket..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 rb s3://$BUCKET_NAME --force 2>/dev/null || true

# Create the bucket
echo -e "${TAG} Creating bucket..."
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 mb s3://$BUCKET_NAME

if [ $? -eq 0 ]; then
    echo -e "${TAG} ✔ S3 reset successful"
    echo ""
else
    echo -e "${TAG} x S3 reset failed"
    echo ""
    exit 1
fi

# Clean
unset AWS_ACCESS_KEY_ID
unset AWS_SECRET_ACCESS_KEY
unset AWS_DEFAULT_REGION