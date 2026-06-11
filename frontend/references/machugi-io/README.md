# Machugi.io Reference Notes

## Storage

- Raw reference files from the user's local downloads are stored only at `frontend/references/machugi-io/raw/`.
- The raw folder is intentionally ignored by Git and Docker because it contains third-party site assets.
- Commit only these notes and derived product-specific design decisions.

## Observed Patterns

- White top navigation over a saturated purple field.
- Geometric background shapes create a game-like arena mood without relying on photos.
- Main content is organized around search, category tabs, and dense card grids.
- Cards use strong title hierarchy, compact metadata, and clear status chips.
- The page opens directly to browsable content instead of a marketing landing page.

## Applied To This Product

- `/` now opens the public exploration screen for completed public debates and public characters.
- Category tabs are loaded from the backend API instead of hardcoded UI constants.
- Authenticated creation is presented as a studio section, gated by login.
- The visual system uses white surfaces, purple geometric background, bold Korean headings, search-first layout, and compact debate/character cards.

## Tokens

- Background: `#313492`
- Deep purple: `#24266f`
- Accent: `#6f2cdb`
- Accent deep: `#550ec2`
- Surface: `#ffffff`
- Soft surface: `#f7f5ff`
- Line: `#ddd9f2`
- Text: `#302d43`
- Muted text: `#716d86`
