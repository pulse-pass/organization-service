#!/usr/bin/env bash
set -eo pipefail

MODE="${1:-test}"
if [ "$MODE" != "test" ] && [ "$MODE" != "development" ]; then
    echo "Invalid mode '$MODE'. Usage: ./service-start.sh [test|development]" >&2
    exit 1
fi

cd "$(dirname "$0")"

docker network inspect pulse-pass >/dev/null 2>&1 || docker network create pulse-pass

if [ ! -f .env ]; then
    cp .env.example .env
fi

set -a
. ./.env

SUFFIX="_$(echo "$MODE" | tr '[:lower:]' '[:upper:]')_URL"
for var in $(grep -oE '^[A-Z0-9_]+(_TEST_URL|_DEVELOPMENT_URL)' .env | sort -u); do
    case "$var" in
        *"$SUFFIX")
            name="${var%"$SUFFIX"}"
            export "${name}_URL=${!var}"
            ;;
    esac
done
set +a

if [ "$MODE" = "test" ]; then
    PROJECT="organization-service-test"
    PROFILE_ARGS=(--profile test)
    export SERVICE_PREFIX="$PROJECT"
    export PUBLISHED_API_PORT="${API_PORT:-8082}"
    export MYSQL_HOST="$PROJECT-mysql"

    ./service-stop.sh test
else
    PROJECT="organization-service"
    PROFILE_ARGS=()
    SHARED_MYSQL="pulse-pass-mysql"
    export SERVICE_PREFIX="$PROJECT"
    export PUBLISHED_API_PORT="${DEVELOPMENT_API_PORT:-8092}"
    export MYSQL_HOST="$SHARED_MYSQL"

    HEALTH="$(docker inspect -f '{{.State.Health.Status}}' "$SHARED_MYSQL" 2>/dev/null || true)"
    if [ "$HEALTH" != "healthy" ]; then
        echo "The shared MySQL container '$SHARED_MYSQL' is not running or not healthy. Start the pulse-pass-infrastructure first (infrastructure-start.sh in that repository)." >&2
        exit 1
    fi

    ./service-stop.sh development

    {
        echo "CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';"
        echo "ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';"
        cat database.sql
        echo "GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';"
    } | docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$SHARED_MYSQL" mysql -uroot
fi

docker compose -p "$PROJECT" "${PROFILE_ARGS[@]}" up -d --build --wait

echo "organization-service is up in $MODE mode at http://localhost:${PUBLISHED_API_PORT}"
