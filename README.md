![Logo](core/src/main/resources/assets/bteterrarenderer/textures/icon.png)
# BTETerraRenderer
![example workflow](https://github.com/tf2mandeokyi/BTETerraRenderer/actions/workflows/gradle.yml/badge.svg) [![Discord Chat](https://img.shields.io/discord/804025113216548874.svg)](https://discord.gg/4gjrwWH2gS)

The map hologram rendering tool for BuildTheEarth project that renders external maps,
such as [Bing maps](https://www.bing.com/maps) and [OpenStreetMap](http://openstreetmap.org/).

This mod can help builders easily map accurate road details and building tops.

Logo by [vicrobex](https://github.com/vicrobex)


## Supported maps

* Global
  * [OpenStreetMap](http://openstreetmap.org/)
  * [Bing maps](https://www.bing.com/maps/)
  * [Yandex.Maps](https://yandex.com/maps/)
* Korean
  * [Naver map](https://map.naver.com/)
  * [Kakao map](https://map.kakao.com/)
  * [T map](https://www.tmap.co.kr/) (No Aerial)
* Japan
  * [Japan GSI map](https://maps.gsi.go.jp/)

You can add other map services by editing the configuration files.<br>
See [API Documentation](YML_CONFIG.md) for more information.

## How to use

1. [Download](https://github.com/tf2mandeokyi/BTETerraRenderer/releases) the latest version of the mod
2. Put the mod in mods folder
3. Run Minecraft

### Controls

| Key               | Description                               |
|-------------------|-------------------------------------------|
| `` ` ``(Backtick) | Opens render settings UI                  |
| `R`               | Toggles map rendering                     |
| `Y`               | Moves map up along Y-axis by 0.5 blocks   |
| `I`               | Moves map down along Y-axis by 0.5 blocks | 

## Screenshot

![Reference screenshot](docs/screenshot0.png "Location: Seattle, USA")

## Building

```bash
./gradlew :core:build
./gradlew :<version>:build
```
The `<version>` can be any Minecraft version that this project supports (ex: `forge1.12.2`)