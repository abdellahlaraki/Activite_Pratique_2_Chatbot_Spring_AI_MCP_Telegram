#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Le fichier .env a été créé. Ajoutez OPENAI_API_KEY puis relancez."
  exit 1
fi

set -a
source .env
set +a

./mvnw clean package
docker compose up --build
