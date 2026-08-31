# ADR-014: Positron Basemap — CARTO Raster to OpenFreeMap Vector Tiles

**Status:** Accepted
**Date:** 2026-08-31
**Decision makers:** Project owner
**Epic/Story:** None — ad-hoc fix (surfaced during manual smoke testing); the resulting
architecture is noted as a prerequisite for `E5-3` in `docs/features/FEATURE-SPEC-BACKLOG.md`.

---

## Context

The "Positron Modern Topographical" basemap
(`AncientDataWebGIS_FE/src/components/MapComponent/layersConfig.ts`) started rendering with
an "API Key Required" watermark on every tile. It used CARTO's legacy anonymous raster CDN
(`https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png`). No API key was ever wired
into either repo (confirmed by grepping both `.env` files and both codebases) — CARTO has
locked down that free-tier CDN and now requires a signed-up API key for any usage, even
non-commercial.

---

## Decision

**Replace the CARTO raster layer with [OpenFreeMap](https://openfreemap.org)'s hosted
`positron` style, rendered as MapLibre GL vector tiles via the
`@maplibre/maplibre-gl-leaflet` Leaflet binding, rather than sign up for and manage a CARTO
API key.** OpenFreeMap serves an MIT/BSD-licensed clone of the same visual style — CARTO
open-sourced the Positron style itself years ago — with no usage key, no rate limit tied to
an account, and no per-request cost.

- `layersConfig.ts` gained a `VectorLayerConfig` variant (`kind: 'vector'`, `styleUrl`)
  alongside the existing `TileLayerConfig`/`WmsLayerConfig`, and a shared
  `isBaseLayerConfig` guard (`kind === 'tile' || kind === 'vector'`) so both raster and
  vector entries are treated as basemaps everywhere a raster tile-kind check previously
  gated that (`positronBaseLayer`, `BaseLayers.tsx`'s `baseLayerConfigs`,
  `useMapInteractions.ts`'s `defaultBaseLayerName`).
- `mapUtils.ts`'s `buildLayer` — the single chokepoint every base-layer consumer already
  went through (`BaseLayers.tsx`, `useMapInteractions.ts`'s `useLayerPanelControl`, and a
  new `FixedBaseLayer` component replacing `MapContent.tsx`'s one direct `<TileLayer>` for
  the Home page) — gained a `vector` branch calling `maplibreGL({ style, attributionControl:
  { customAttribution } })`. `customAttribution` is required: OpenFreeMap's style JSON
  carries no `attribution` metadata on its own sources, so without it the map would show no
  attribution at all (the plugin always disables MapLibre's native on-canvas attribution
  control in favor of surfacing `getAttribution()` through Leaflet's own control).
- `vite.config.js` needed `optimizeDeps.exclude: ['maplibre-gl', '@maplibre/maplibre-gl-leaflet']`.
  Without it, esbuild's dev-server dependency pre-bundling rewrites the `import.meta.url`
  reference MapLibre GL JS uses to instantiate its web worker, pointing it at a
  `.vite/deps/maplibre-gl-worker.mjs` file that was never generated there — the worker
  silently 404s and no vector tiles ever paint (`npm run build`'s production bundle was
  unaffected; this was dev-server-only).
- jsdom has no WebGL context, so `@maplibre/maplibre-gl-leaflet` is mocked globally in
  `src/test/setupTests.js` (alongside the existing `matchMedia`/`PointerEvent` polyfills)
  rather than per test file, since any test rendering a real Leaflet map can now mount the
  Positron base layer.

---

## Alternatives Considered

### A. Sign up for a CARTO API key

- **Rejected.** Trades a keyless, self-inflicted outage for an ongoing external-account
  dependency (key rotation, CARTO's own future rate-limit/pricing changes) to keep using a
  raster tile format that's a dead end for future styling work anyway.

### B. Another keyless raster provider (e.g. Esri's `Canvas/World_Light_Gray_Base`)

- **Rejected.** Would fix the watermark with a smaller diff (no new dependency, no worker
  quirk to debug), but keeps the basemap on raster PNG tiles — baked server-side images that
  can't be recolored client-side. `E5-3` ("Add synthwave map style profile",
  `FEATURE-SPEC-BACKLOG.md`) needs exactly that recoloring capability; picking another raster
  provider now would mean re-doing this migration later anyway.

### C. OpenFreeMap vector tiles (chosen)

- Same visual style (literally the same open-sourced Positron style CARTO's raster tiles
  were rendering from), no key, no rate limit, and — as a side effect, not this task's goal —
  puts the basemap on a stylable vector renderer for whenever `E5-3` is picked up.

---

## Consequences

### Positive

- No external account/key to manage or rotate; OpenFreeMap's terms don't require one.
- Visually unchanged from the previous CARTO Positron style.
- Unblocks `E5-3` (synthwave map style profile) without a second basemap migration later.

### Negative

- New runtime dependency (`maplibre-gl` + `@maplibre/maplibre-gl-leaflet`) — `MapComponent`'s
  built JS chunk grew to ~1.06 MB (~278 KB gzipped) per `npm run build`'s chunk-size warning,
  the largest single chunk in the app. Not addressed here (code-splitting `MapComponent` is a
  separate, pre-existing concern the build warning already flagged before this change).
- Introduces a new external SaaS dependency on OpenFreeMap's continued free hosting — same
  category of risk this change was meant to get away from, just with a provider whose terms
  don't currently require a key or impose a rate limit tied to an account.
- The `vite.config.js` `optimizeDeps.exclude` requirement is a non-obvious workaround for a
  dev-server-only bug; if either package's Vite/esbuild interaction changes upstream, this
  may need revisiting.

### When to Revisit

- If OpenFreeMap's hosted service becomes unreliable, rate-limited, or shuts down — the
  `styleUrl` is the only thing that would need to change (self-hosting OpenFreeMap's tiles,
  per their own docs, is an explicit fallback path they document for exactly this case).
- When `E5-3` is picked up: the vector-tile groundwork here (MapLibre GL, the `vector`
  `layersConfig` kind) is the prerequisite it depends on.

---

## Implementation References

- `AncientDataWebGIS_FE/src/components/MapComponent/layersConfig.ts` (`VectorLayerConfig`,
  `isBaseLayerConfig`, the Positron entry)
- `AncientDataWebGIS_FE/src/components/MapComponent/mapUtils.ts` (`buildLayer`'s `vector` branch)
- `AncientDataWebGIS_FE/src/components/MapComponent/BaseLayers.tsx` (`FixedBaseLayer`, updated `baseLayerConfigs`)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.tsx` (Home page's fixed base layer)
- `AncientDataWebGIS_FE/src/components/MapComponent/useMapInteractions.ts` (`defaultBaseLayerName`)
- `AncientDataWebGIS_FE/vite.config.js` (`optimizeDeps.exclude`)
- `AncientDataWebGIS_FE/src/test/setupTests.js` (global `@maplibre/maplibre-gl-leaflet` mock)
