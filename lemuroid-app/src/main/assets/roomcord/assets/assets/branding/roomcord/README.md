# Roomcord in-app assets

REPLACE WITH REAL ROOMCORD ASSETS — PR 2 set up the structure but kept current
graphics. Drop new files at these paths to complete the visual rebrand.

PLACEHOLDER copies of the original WizeUp logos.
Replace `logo.png` and `logo_transparent.png` with real Roomcord artwork
before release.

These paths are referenced via `BrandConfig.logoAssetPath` /
`BrandConfig.logoTransparentPath` in `lib/config/brand_config.dart`. Call
sites use `Brand.current.logoAssetPath` (e.g. `lib/widgets/common/app_logo.dart`).

## Generating launcher icons

Drop a 1024×1024 master at `icon-master.png`, then run:

    flutter pub run flutter_launcher_icons

This regenerates Android `mipmap-*` launcher icons, iOS `AppIcon.appiconset`,
and the web favicon set per the `flutter_launcher_icons:` block in
`pubspec.yaml`.
