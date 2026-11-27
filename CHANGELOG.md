# Changelog

## 3.8.0
### Added
- JEI/HEI pages: how to build the totem shrine (with materials) and what each of Herobrine's 14 creatures does, with 3D previews. English + Russian. Requires ArchaicLib 1.47.0+.

## 3.7.2
### Changed
- The original mod file is now downloaded automatically on first start (needs ArchaicLib 1.42.0+): sources and mirrors are listed in `config/archaiclib/originals/` and can be edited there. If the download fails, a banner on the main menu links to the manual download.

## 3.7.1
### Changed
- Performance: uses ArchaicLib 1.40.0 FastRandom instead of java.util.Random, plus the 1.40.0 cached block-state / allocation-free legacy world calls. Requires ArchaicLib 1.40.0+.

## 3.7.0
First release of the 1.12.2 port (Cleanroom Loader). Based on The Herobrine Mod 3.7 for 1.7.10 by Burnner.

- Shrine, Herobrine and all 14 mobs ported.
- Ships no original assets: textures, sounds and structure files load from the original jar in `archaic_originals/`.
- Removed browser ads, dead update checker and fake log; fixed several original bugs (signs, traps, hit reactions, drops).
