#!/usr/bin/env bash
set -euo pipefail

# The SSH alias/address belongs in local SSH config or CI secrets, never here.
target=${1:?Usage: bash infra/deploy.sh SSH_TARGET [REMOTE_DIR]}
remote_dir=${2:-/opt/integritylens}
[[ "$target" != -* && "$target" != *[[:space:]]* ]] || { echo 'Invalid SSH target' >&2; exit 1; }
[[ "$remote_dir" =~ ^/[a-zA-Z0-9_/-]+$ && "$remote_dir" != / ]] || { echo 'Invalid remote directory' >&2; exit 1; }
root=$(cd "$(dirname "$0")/.." && pwd)
ssh_opts=(-o BatchMode=yes -o ConnectTimeout=15 -o ConnectionAttempts=5 -o ServerAliveInterval=15 -o ServerAliveCountMax=4)

stage=$(ssh "${ssh_opts[@]}" "$target" "test -r '$remote_dir/.env' && docker compose version >/dev/null && mktemp -d '$remote_dir/upload.XXXXXXXX'")
[[ "$stage" == "$remote_dir"/upload.* && "$stage" =~ ^/[a-zA-Z0-9_./-]+$ ]] || { echo 'Invalid staging directory' >&2; exit 1; }
rsync -az --delete \
  --exclude='.git/' --exclude='node_modules/' --exclude='target/' \
  --exclude='dist/' --exclude='__pycache__/' --exclude='.venv/' \
  --exclude='.env*' --exclude='reports/' --exclude='*.log' \
  -e 'ssh -o BatchMode=yes -o ConnectTimeout=15 -o ConnectionAttempts=5 -o ServerAliveInterval=15 -o ServerAliveCountMax=4' \
  --include='/api/***' --include='/worker/***' --include='/web/***' \
  --include='/infra/***' --include='/.dockerignore' --exclude='*' \
  "$root/" "$target:$stage/"

ssh "${ssh_opts[@]}" "$target" "bash -s -- '$remote_dir' '$stage'" <<'REMOTE'
set -euo pipefail
upload_dir=$2
trap 'rm -rf -- "$upload_dir"' EXIT
# Lock deployments from different clients as well as CI.
exec 9>"$1/deploy.lock"
flock -w 900 9
mkdir -p "$1/app"
rsync -a --delete "$upload_dir/" "$1/app/"
cd "$1/app"
compose=(docker compose --env-file "$1/.env" -f infra/docker-compose.yml)
# Sequential builds keep peak memory low on a small VM.
"${compose[@]}" build api
"${compose[@]}" build worker-jplag
"${compose[@]}" build web
"${compose[@]}" up -d --wait --wait-timeout 240
"${compose[@]}" ps
curl --fail --silent --show-error http://127.0.0.1:8080/api/health
curl --fail --silent --show-error http://127.0.0.1/api/health
REMOTE
