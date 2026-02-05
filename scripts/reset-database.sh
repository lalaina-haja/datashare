#!/bin/bash
set -e  # Stop on error

# echo message formatting
source "$(dirname "$0")/message-format.sh"
TAG="${NC}[ ${TITLE}reset-database ${NC}]" # [reset-database]

# Load .env
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
set -a
source "$ENV_FILE"
set +a

# Configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-datashare_db}
DB_USER=${DB_USER:-datashare_usr}
DB_PASSWORD=${DB_PASSWORD:-datashare_pwd}

echo -e "${TAG} Resetting test database..."
echo -e "   Host: ${KEYWORD}$DB_HOST:$DB_PORT${NC}"
echo -e "   Database: ${KEYWORD}$DB_NAME${NC}"

# Check PostgreSQL is accessible
if ! nc -z $DB_HOST $DB_PORT 2>/dev/null; then
    echo -e "${TAG} ERROR: PostgreSQL is not accessible at $DB_HOST:$DB_PORT"
    echo ""
    exit 1
fi

# Export password for psql
export PGPASSWORD=$DB_PASSWORD

# Rest database
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << EOF
-- Désactiver les contraintes
SET session_replication_role = replica;

-- Delete all data
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE user_roles CASCADE;
TRUNCATE TABLE files CASCADE;

-- Reinit sequences
ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS roles_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS files_id_seq RESTART WITH 1;

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_TEST') ON CONFLICT DO NOTHING;

-- Reactivate constraints
SET session_replication_role = DEFAULT;
EOF

if [ $? -eq 0 ]; then
    echo -e "${TAG} ✔ Database reset successful"
    echo ""
else
    echo -e "${TAG} ✘ Database reset failed"
    echo ""
    exit 1
fi

# Clean
unset PGPASSWORD