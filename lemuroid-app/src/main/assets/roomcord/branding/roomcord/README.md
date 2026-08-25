# Roomcord web assets

Real Roomcord brand artwork. `tool/build_web.sh` copies these into `web/`
at build time (icons under `web/icons/`, favicon as `web/favicon.png`).

Source of truth: `assets/branding/roomcord/logo.png` (1024×1024). To
regenerate, resize that PNG into the targets below.

Files:
- `favicon.png` — 32×32 browser favicon (replaces `web/favicon.png`)
- `Icon-192.png`, `Icon-512.png` — PWA icons
- `Icon-maskable-192.png`, `Icon-maskable-512.png` — PWA maskable icons
- `og-default.png` — 512×512 Open Graph default image
