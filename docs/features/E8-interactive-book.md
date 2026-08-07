# E8 — Interactive Book / Research Narrative

**Status:** 🚧 In Progress (core reading experience delivered August 2026; E8-8/E8-9 docs still open)
**Epic parent:** E8 Interactive Book / Research Narrative
**Scope:** long-form Markdown content chapters, embedded QGIS-generated images, chapter navigation/TOC, responsive prose styling
**Out of scope (deferred to E8-10, E8-13):** MDX/embedded live React components; bulk `.docx` manuscript ingestion
**Related ADR:** `docs/architecture/adr/ADR-011-book-content-storage-and-backup.md`

---

## Background

The project owner has ~20 pages of already-written long-form research narrative (prehistoric roads, Roman road-building, methodology, etc.) that currently has no home in the app beyond the hard-coded `About.tsx` paragraph block. The goal is to give this content a proper "book" reading experience that sits alongside the interactive map/data explorer, starting with the two things needed immediately: **large text blocks** and **inserting QGIS-generated map images with captions**. Deep cross-linking from prose into live map/data views (e.g. `<SiteLink id={123}>`) is a valuable future step but is explicitly deferred (E8-10) to keep this epic's first delivery small and dependency-light.

Once the Book existed as a proper home for long-form content, `About.tsx`'s original wall of text (the project's original research summary) was migrated there as `01-introduction.md` (E8-7). This freed `About` to become a short, focused project-introduction page (photo, project origin story, tech stack, GitHub links — E8-11), and gave an opening to also turn the previously single-paragraph `News` page into a proper dated changelog, with the `rinsewillet.net` deployment as its first real entry (E8-12).

---

## Content architecture

```
AncientDataWebGIS_FE/src/
  content/
    book/
      01-introduction.md
      02-prehistoric-roads.md
      03-roman-road-building.md
      chapters.ts          <- ordered manifest: slug, title, part/section grouping
  assets/
    book/
      <qgis-export-1>.png
      <qgis-export-2>.png
      ...
  components/
    Book/
      MarkdownImage.tsx     <- resolves image filename -> bundled asset, renders <figure>+<figcaption>
      TableOfContents.tsx    <- sidebar/nav built from chapters.ts
      ChapterNav.tsx         <- prev/next chapter footer links
  pages/
    BookChapter.tsx          <- loads and renders chapter Markdown for /book/:slug
    BookChapter.css
```

### Why Markdown (not MDX) for this delivery

MDX would allow embedding live React components directly in prose (map deep-links, inline mini-maps, live photo galleries), but requires a Vite build-plugin (`@mdx-js/rollup`), TypeScript module typings, and more moving parts. Since the immediate need is just **prose + captioned images**, plain Markdown rendered via `react-markdown` covers it with a single dependency and zero Vite config changes. The MDX upgrade is deferred as E8-10 — additive later, not a rewrite, since chapter content stays in per-chapter files either way.

### Image handling

Two categories, two sources — this distinction is intentional and should be preserved as the epic grows:

- **Illustrative QGIS exports / sketches / diagrams** (not tied to a specific database record): bundled under `src/assets/book/`, resolved automatically via `import.meta.glob(..., { eager: true })` keyed by filename, so authoring only requires referencing the filename in Markdown (`![alt](filename.png "caption")`) — no manual import statements per image.
- **Actual site/road photographs that already exist as `MediaAsset` records**: not duplicated into the book's static assets. A later story (part of E8-10, or an earlier opt-in story if needed sooner) can add an `InlinePhotoGallery` component that fetches these through the existing media API/`MediaGallery`, so the book always reflects the current, authoritative photo set.

### Routing

A single dynamic route instead of one manual `lazy()` import per chapter:

```text
// App.tsx
const BookChapter = lazy(() => import('./pages/BookChapter'));
// ...existing lazy route imports...
<Route path="/book/:slug" element={<BookChapter />} />
```

`BookChapter.tsx` uses `import.meta.glob('../content/book/*.md', { eager: true, query: '?raw', import: 'default' })` to map the `:slug` param to chapter content. Adding chapter 21+ later is: drop a new `.md` file (+ its images) in the folder and add one line to `chapters.ts` — no route wiring needed.

### Reading UX

- `TableOfContents` (built from `chapters.ts`) — lets readers browse chapters like a book's index.
- `ChapterNav` — prev/next chapter links in the chapter footer.
- A dedicated nav entry (e.g. "Research" or "The Book") separate from the existing Home/News/About, per the navbar convention in `NavbarHook.tsx`.

---

## Responsive design

Reuses the app's existing plain-CSS `@media` breakpoint conventions (see `InfoPage.css`, `App.css`: 400px / 480px / 850px / 1125px steps) rather than introducing a new responsive framework.

- **Prose width:** cap chapter text at `max-width: 70ch` (character-based unit caps line length for readability regardless of screen size) rather than a fixed pixel width.
- **Images:** `img { max-width: 100%; }` inside the `Figure`/`MarkdownImage` renderer so QGIS exports shrink to fit mobile viewports automatically.
- **Export resolution:** QGIS map exports should be generated at a sensible max width (~1600–2000px) before being added to `src/assets/book/` — Vite does not automatically downscale image dimensions (only hashes/bundles them), so oversized exports would unnecessarily bloat mobile page weight. `vite-imagetools` (automatic responsive `srcset` generation) is noted as optional future polish, not a requirement for this epic.

---

## Backup / content-as-data strategy

See `ADR-011-book-content-storage-and-backup.md` for the full decision record. Summary:

- Chapter Markdown files and bundled illustrative images live in the **frontend git repository** (`src/content/book/`, `src/assets/book/`). Git itself is the backup/versioning mechanism — every edit is committed, diffable, and recoverable, and is at least as strong a guarantee as the DB/media backup story for prose specifically (full edit history, not just periodic snapshots). The only operational requirement is pushing to the remote regularly (already standard practice).
- Any photographs that are also scientific/dataset records (tied to a site/road) should continue to be uploaded through the existing `MediaAsset` pipeline rather than duplicated as static book assets — this keeps them covered by the existing `NasBackupService` (weekly NAS sync) and `scripts/backup.sh` (nightly full archive) with zero new backend work.
- No database-backed content model (headless CMS, new Spring entities) is planned for this epic — the project is single-author right now, so file-based content with git as the backup layer is the right-sized solution. Revisit only if multi-author, non-technical editing becomes a real requirement (see ADR-011 "When to Revisit").

---

## Stories

See `FEATURE-SPEC-BACKLOG.md` § "E8 — Interactive Book / Research Narrative" for the full story table and the "Completed Epics" write-up of what's been delivered so far (E8-1 through E8-7, E8-11, E8-12).

## Outstanding / deferred

- **E8-8 / E8-9:** Document the git-push-as-backup expectation, and verify/document NAS coverage for any future `media/book/` data-linked photos. Not yet done — no code changes required, documentation only.
- **E8-10 (Deferred):** MDX upgrade for embedded live components (`<SiteLink>`, `<InlineMap>`, `<InlinePhotoGallery>`) enabling prose to deep-link directly into `/atlas/site_:id` and `/datalist/siteinfo/:id`, or embed a live `MapComponent`/`MediaGallery` inline. Not started; revisit once the base Markdown reading experience wants richer in-text interactivity.
- **E8-13 (Deferred):** Ingest the author's existing ~20-page `.docx` manuscript into further `src/content/book/*.md` chapters. Candidate approach: `pandoc manuscript.docx -o chapter.md --extract-media=./src/assets/book` to get a first-pass Markdown conversion plus extracted images, followed by a manual per-chapter review/cleanup pass (heading levels, captions, stray formatting artifacts) before publishing each chapter. Not started — pending the author sharing the manuscript file.


