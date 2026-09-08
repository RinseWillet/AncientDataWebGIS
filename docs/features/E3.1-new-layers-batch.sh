#!/usr/bin/env bash
# Registers every row of E3.1-new-layers-batch.csv via POST /api/raster/catalog.
# Requires: jq, curl.
#
# Usage:
#   export HOST="https://rinsewillet.net/webGIS"
#   export TOKEN="<admin JWT from POST /api/auth/login>"
#   ./E3.1-new-layers-batch.sh
set -euo pipefail

: "${HOST:?Set HOST, e.g. https://rinsewillet.net/webGIS}"
: "${TOKEN:?Set TOKEN to an admin JWT from POST /api/auth/login}"

CSV="$(dirname "$0")/E3.1-new-layers-batch.csv"

tail -n +2 "$CSV" | while IFS=, read -r source name collection attribution \
    bounds_south bounds_west bounds_north bounds_east zoom_min zoom_max category hillshade; do

  [ -z "$source" ] && continue

  body=$(jq -n \
    --arg name "$name" \
    --arg source "$source" \
    --argjson south "$bounds_south" \
    --argjson west "$bounds_west" \
    --argjson north "$bounds_north" \
    --argjson east "$bounds_east" \
    --argjson zmin "$zoom_min" \
    --argjson zmax "$zoom_max" \
    --arg attribution "$attribution" \
    --arg category "$category" \
    --arg collection "$collection" \
    --argjson hillshade "$(echo "$hillshade" | tr '[:upper:]' '[:lower:]')" \
    '{
      name: $name,
      source: $source,
      bounds: { south: $south, west: $west, north: $north, east: $east },
      zoom: { min: $zmin, max: $zmax },
      attribution: $attribution,
      category: $category,
      hillshade: $hillshade
    } + (if $collection == "" then {} else { collection: $collection } end)')

  echo "== $source =="
  curl -sS -o /dev/null -w "  HTTP %{http_code}\n" -X POST "$HOST/api/raster/catalog" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$body"
done
