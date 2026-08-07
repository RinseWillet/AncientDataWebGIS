# E4 — Responsive UX for Field Use

**Status:** Complete (August 2026)
**Epic parent:** E4 Responsive UX for Field Use
**Scope:** map interactions, touch targets, `DataList` mobile readability, regression checklist
**Out of scope:** visual theme/branding (→ E5 Synthwave)

## Target hardware

| Device | Viewport / context | Priority |
|---|---|---|
| Fairphone 5 (and similar Android phones) | ~393 px CSS width, Chrome Mobile, touch input | **Primary mobile** |
| Generic tablet | ~768 px, touch input | **Secondary mobile** |
| 15-inch laptop | ~1280–1440 px, desktop browser | **Primary desktop** |
| 32-inch external screen | ~1920 px browser window | **Secondary desktop** |

---

## E4-1 — Mobile bottom-sheet map details ✅

**What was delivered:**
- New `BottomSheetCard` component (`AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.tsx`) wraps `MapInfoCard`'s content.
  - Desktop/tablet (> 600px): renders as the existing fixed side panel — the drag handle is hidden via CSS and behaviour is unchanged.
  - Mobile (≤ 600px): renders as a bottom sheet with two snap states (`sheet-half` = 45% viewport height, `sheet-full` = 88%), a drag handle (tap to toggle state, or drag) and a swipe-down-to-dismiss gesture (threshold: 120px).
- `MapInfoCard.tsx` now wraps its site/road detail views in `<BottomSheetCard onDismiss={clearSelection}>` instead of a plain `<div className="infoCard">`.
- CSS (`MapComponent.css`) no longer hardcodes `.infoCard` height to 50% on mobile — height is now driven by the sheet state class from `BottomSheetCard.css`.

**Files changed:**
- `AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.tsx` (new)
- `AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.css` (new)
- `AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.test.tsx` (new — 4 tests: renders + handle, half↔full toggle, drag-past-threshold dismiss, small-drag no-op)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapInfoCard.tsx`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapComponent.css`

**Test infrastructure note:** jsdom does not implement the `PointerEvent` constructor ([jsdom/jsdom#2527](https://github.com/jsdom/jsdom/issues/2527)), so `@testing-library`'s `fireEvent.pointerDown/Move/Up` silently dropped `clientY` and the drag-dismiss test failed. Fixed by adding a minimal `PointerEvent` polyfill (extends `MouseEvent`) to `src/test/setupTests.js`, so pointer coordinates now propagate correctly in tests.

---

## E4-2 — Touch target spacing/sizing ✅

Audited all interactive controls introduced or touched by E4-1 against the ≥ 44×44px touch-target guideline (WCAG 2.5.5 / Apple & Material HIG):

| Control | Before | After |
|---|---|---|
| `.closeBtn` (map info card ✖) | 18px font, no explicit hit area | `min-width/height: 44px`, flex-centered |
| `.infoCard-detailsBtn` ("View full details") | padding-only, ~34px tall | `min-height: 44px` |
| `.infoCard-dragHandle` (bottom-sheet grip) | ~21px tall tap strip | `min-height: 44px` |
| Leaflet zoom in/out buttons | native 26px | `44px` on mobile (`.leaflet-touch .leaflet-bar a`) |
| Leaflet layers-control toggle | native ~36px | `44px` on mobile |
| `DataList` pagination Prev/Next buttons | ~34–42px depending on breakpoint | `min-height/width: 44px` |

Nav bar toggle/close/logout controls were already hardened to 44px in a prior responsive pass (`make-map-datalist-responsiver`) and needed no further changes.

**Files changed:**
- `AncientDataWebGIS_FE/src/components/MapComponent/MapInfoCard.css`
- `AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.css`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapComponent.css` (mobile-only Leaflet control overrides)
- `AncientDataWebGIS_FE/src/pages/DataList.css`

---

## E4-3 — `DataList` mobile readability ✅

Largely already delivered in the prior `make-map-datalist-responsiver` responsive pass (scrollable table wrapper, responsive font-size/padding steps at 850px/400px breakpoints, full-height desktop layout ≥1200px). This pass closed the remaining gap: pagination controls now meet the 44px touch-target minimum (see E4-2 table above), so the readability and interaction acceptance criteria are both satisfied.

---

## E4-4 — Responsive QA matrix / regression checklist ✅

### Breakpoints to test

| Breakpoint | Represents |
|---|---|
| 393px | Fairphone 5 / small Android phone |
| 600px | Mobile ⇄ tablet CSS breakpoint used across `MapComponent`/`BottomSheetCard` |
| 768px | Tablet portrait |
| 850px | `DataList` font/padding step |
| 1024px | Tablet landscape / small laptop |
| 1280px | Laptop |
| 1920px | External monitor |

### Regression checklist (run before merging any change touching `MapComponent`, `MapInfoCard`, `NavbarHook`, or `DataList`)

- [ ] At ≤600px, selecting a site/road marker opens the bottom sheet in the **half** state; tapping the drag handle expands it to **full**, tapping again collapses back to **half**.
- [ ] At ≤600px, dragging the handle down past ~120px dismisses the sheet (`clearSelection` fires) and the map reappears full-screen.
- [ ] At >600px, the info card behaves as a static right-side panel — no drag handle visible, no height animation.
- [ ] All info-card buttons (`✖`, "View full details") are comfortably tappable on a real touch device or via Chrome DevTools touch emulation (visually ≥44px hit area, not just the visible glyph).
- [ ] Leaflet zoom (+/−) and the layers-control toggle are easily tappable at ≤600px without mis-tapping an adjacent control.
- [ ] `DataList` table remains horizontally scroll-free at 393px and 768px; pagination Prev/Next buttons are easily tappable and clearly show the disabled state at the first/last page.
- [ ] Nav bar hamburger menu opens/closes correctly at ≤1150px; backdrop dismiss and Escape/route-change close both still work.
- [ ] No layout shift/overflow regressions at 1280px and 1920px (dashboard/table `max-width` centering still holds).

### Tooling notes

- Preferred quick check: Chrome DevTools device toolbar (toggle with `Ctrl+Shift+M`), set to "Fairphone 5" custom preset (393×851) or a stock preset near that size, with touch simulation enabled so pointer events (not just mouse) are exercised.
- `BottomSheetCard.test.tsx` and `MapInfoCard.test.tsx` cover the toggle/drag/dismiss logic in CI; the checklist above is for manual/visual verification that automated tests can't catch (real touch hit-testing, visual overlap, animation smoothness).

---

## Outstanding (deferred, not blocking)

- No further E4 work is planned at this time. Future mobile UX issues should be filed as new stories rather than reopening E4-1..E4-4.

