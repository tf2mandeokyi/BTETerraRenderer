# YAML Config Documentation
This documentation contains a guide for making custom map & projection configuration files.

All file extensions must be `.yml`.<br>
~~(Might also support `.json` extensions in the future)~~

If you're getting YML syntax errors, try fixing it in [YAML Validator site](https://codebeautify.org/yaml-validator)

If you don't know how to nor want to make one, feel free to ask for support in [our Discord server](https://discord.com/invite/4gjrwWH2gS).


## Tile map service (TMS) configuration
TMS files contain map services that will be displayed as in-game hologram when selected.

Put your custom TMS files in `.minecraft/config/bteterrarenderer/maps/` to see it as hologram.

How to get to the `.minecraft` folder:
1. Open Minecraft Launcher and go to the `Installations` tab
2. Find the installation profile, hover your mouse on it, and click the folder icon

The file names can be anything except `default_maps.yml`.

Object properties are listed in [the structure table](#type-tilemapservice).


### `example_maps.yml`

See [`default_maps.yml`](core/src/main/resources/assets/bteterrarenderer/default_maps.yml) for more examples

```yaml
categories:
   # Map category name.
   Global:
      # Map ID. It should be unique in its category and its file.
      # FYI: The mod automatically converts map IDs into something like "<category>.<id>".
      # for example, this ID gets converted into "Global.osm"
      osm:
         type: "flat"
         name: OpenStreetMap
         tile_url: https://{random:a,b,c}.tile.openstreetmap.org/{z}/{x}/{y}.png
         icon_url: https://wiki.openstreetmap.org/w/images/c/c8/Public-images-osm_logo.png
         projection: webmercator
         max_thread: 2
         copyright: [
           "",
           { "text": "© ", "color": "white" },
           { "text": "OpenStreetMap", "underlined": true, "color": "aqua", "clickEvent": {
             "action": "open_url",
             "value": "https://www.openstreetmap.org"
           }},
           { "text": " contributors", "color": "white" }
         ]

      google_earth:
        type: ogc3dtiles
        name: "Google Earth 3d"
        tile_url: https://tile.googleapis.com/v1/3dtiles/root.json?key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        max_thread: 64
        rotate_model_x: true
        geoid: egm96
      # ...
   # ...
```


### Type `TileMapService`
| Key                   | Value Type                         | Required? \[`Default`\] | Description                                                             |
|-----------------------|------------------------------------|-------------------------|-------------------------------------------------------------------------|
| `type`                | `string`                           | N \[`flat`\]            | Map type.                                                               |
| `name`                | `string`                           | Y                       | Displayed Name. It supports color codes.                                |
| `tile_url`            | `string`                           | Y                       | Tile URL.                                                               |
| `max_thread`          | `int`                              | N \[`2`\]               | Maximum amount of threads to fetch map data.                            |
| `copyright`           | `string` \| `TextComponentJSON`    | N                       | Copyright text. Use this when the map provider wants you to write them. |
| `icon_url`            | `string`                           | N                       | Icon URL. This is for the UI.                                           |
| `hologram_projection` | `GeographicProjection`             | N                       | Projection for the hologram.                                            |
| `cache`               | [`CacheConfig`](#type-cacheconfig) | N                       | Cache configuration.                                                    |


### Type `CacheConfig`

| Key                   | Value Type | Required? \[`Default`\] | Description                                                      |
|-----------------------|------------|-------------------------|------------------------------------------------------------------|
| `expire_milliseconds` | `int`      | N \[`1,800,000`\]       | Cache expiration time in milliseconds. Set to `-1` for no limits |
| `maximum_size`        | `int`      | N \[`-1`\]              | Maximum cache size. Set to `-1` for no limits                    |
| `debug`               | `boolean`  | N \[`false`\]           | Debug mode.                                                      |


### Map Type `FlatTileMapService`
Extends [type `TileMapService`](#type-tilemapservice) (`type="flat"`),
meaning that its parameters also apply to this structure.

URL parameters functionality gets applied to the `tile_url`.
See [URL parameters](#url-parameters) for its parameters

| Key                    | Value Type | Required? \[`Default`\] | Description                                                                                        |
|------------------------|------------|-------------------------|----------------------------------------------------------------------------------------------------|
| `projection`           | `string`   | Y                       | Projection name. <br> See [projection configuration](#tile-projection-configuration) for more info |
| `default_zoom`         | `int`      | N \[`18`\]              | Default zoom value                                                                                 |
| `invert_zoom`          | `boolean`  | N \[`false`\]           | Zoom system is inverted when `true`. <br> Useful when lower zoom value means higher resolution     |
| `flip_vert`            | `boolean`  | N \[`false`\]           | *Flip tiles vertically when `true`.                                                                |
| `invert_lat`           | `boolean`  | N \[`false`\]           | *Latitude is inverted when `true`. It will also flip tiles vertically.                             |

&ast; Tiles won't be "flipped" when both `flip_vert` and `invert_lat` are set to `true`
(For nerds, think of it as an XOR gate)


### URL parameters
| URL parameter        | Description                                                                      |
|----------------------|----------------------------------------------------------------------------------|
| `{x}`                | X-axis parameter.                                                                |
| `{y}`                | Y-axis parameter.                                                                |
| `{z}`                | Absolute zoom parameter = `(default_zoom) + (zoom slider value)`                 |
| `{u}`                | Map Tile Quad key. Used in [Bing Maps](https://www.bing.com/maps)                |
| `{random:a,b,c,...}` | Picks string randomly from the given list. In this case, `"a"`, `"b"`, or `"c"`. |


### Map Type `Ogc3dTileMapService`
Extends [type `TileMapService`](#type-tilemapservice) (`type="ogc3dtiles"`)

Based on the [Specification Documentation](https://docs.ogc.org/cs/22-025r4/22-025r4.html#toc0).

| Key              | Value Type | Required? \[`Default`\]  | Description                           |
|------------------|------------|--------------------------|---------------------------------------|
| `semi_major`     | `double`   | N \[`6,378,137.0`\]      | Semi-major axis of the ellipsoid.     |
| `semi_minor`     | `double`   | N \[`6,356,752.314245`\] | Semi-minor axis of the ellipsoid.     |
| `geoid`          | `string`   | N \[`"wgs84"`\]          | Geoid type.                           |
| `rotate_model_x` | `boolean`  | N \[`false`\]            | Rotates GlTF models along the X-axis. |


### Supported Geoids
| Geoid   | Description                                                                 |
|---------|-----------------------------------------------------------------------------|
| `wgs84` | World Geodetic System 1984. Used in GPS systems.                            |
| `egm96` | Earth Gravitational Model 1996. Used in Google Earth and other map services |


## Tile projection configuration
Some map providers will use projections other than the standard `webmercator` projection in order to reduce the amount of distortion happening in their region.

You can use non-standard projections for those map services by making custom projection config files.

The mod will use the coordinate system (such as `epsg`) and the tile matrix to calculate tile positions and display tile images accordingly.

Place your projection files in `.../.minecraft/config/bteterrarenderer/projections/` to add custom tile projections.

The file names can be anything except `default_projections.yml`.

### Default projections
| Projection name   | CRS name                            | Description                                                                           |
|-------------------|-------------------------------------|---------------------------------------------------------------------------------------|
| `webmercator`     | `EPSG:3857`<br>`EPSG:900913`<br>... | Web mercator projection. <br> The most common coordinate system used for map services |
| `worldmercator`   | `EPSG:3395`                         | World mercator projection. <br> Used in [Yandex.Maps](https://yandex.com/maps/)       |
| `kakaoprojection` | `EPSG:5181`                         | Used for Korean map services, such as [Kakao Map](https://map.kakao.com/)             |


### example_projections.yml

See [`default_projections.yml`](core/src/main/resources/assets/bteterrarenderer/default_projections.yml) for more examples

```yaml
tile_projections:
   # Projection name.
   # This is global across every projection file, so beware of name clashes.
   webmercator:
      # The coordinate system object
      projection:
         proj4:
            name: 'EPSG:3857'
            param: '+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 ...'

      # It also supports Terraplusplus' projection objects `GeographicProjection`
      # projection: {
      #     "scale": {
      #         "delegate": {
      #             "flip_vertical": {
      #                 "delegate": {
      #                     "bte_conformal_dymaxion": {}
      #                 }
      #             }
      #         },
      #         "x": 7318261.522857145,
      #         "y": 7318261.522857145
      #     }
      # }
            
      # Tile matrix. Key numbers mean the absolute zoom level.
      tile_matrices:
         0: { "origin": [ -30000.0, -60000.0 ], "size": [ 524288.0, -524288.0 ] }
         1: { "origin": [ -30000.0, -60000.0 ], "size": [ 262144.0, -262144.0 ] }
         2: { "origin": [ -30000.0, -60000.0 ], "size": [ 131072.0, -131072.0 ] }
         # ...
```


### Projection object structure
| Key           | Value Type                                | Required? | Description               |
|---------------|-------------------------------------------|-----------|---------------------------|
| `projection`  | [CoordinateSystem](#CoordinateSystem)     | Y         | Coordinate system object. |
| `Tile matrix` | Map<int, [TileMatrixRow](#TileMatrixRow)> | Y         | Tile matrix.              |


### CoordinateSystem
The structure is equal to Terraplusplus' projection JSON structure but with `proj4` projection added.

The table below is for the `proj4` projection object. (Visit [epsg.io](https://epsg.io/) for PROJ.4 projections)

| Key     | Value Type | Required? | Description                                                             |
|---------|------------|-----------|-------------------------------------------------------------------------|
| `name`  | string     | Y         | Coordinate system name                                                  |
| `param` | string     | Y         | PROJ.4 parameters. Visit [epsg.io](https://epsg.io/) for the parameters |


### TileMatrixRow
| Key      | Value Type | Required? | Description                                  |
|----------|------------|-----------|----------------------------------------------|
| `origin` | int[2]     | Y         | The coordinate where the `0,0` tile lies at. |
| `size`   | int[2]     | Y         | Size of a single tile image.                 |
