# YAML Config Documentation
This documentation contains a guide for making custom map & projection configuration files.

All file extensions must be `.yml`.<br>
~~(Might also support `.json` extensions in the future)~~

If you're getting YML syntax errors, try fixing it in [YAML Validator site](https://codebeautify.org/yaml-validator)

If you don't know how nor want to make one, ask for support on [our Discord server](https://discord.com/invite/4gjrwWH2gS).


## Tile map service (TMS) configuration
TMS files contain map services that will be displayed as an in-game hologram when selected.

The config directory is in `.minecraft/config/bteterrarenderer/maps/`. Put your custom TMS config files there
to see it in Minecraft.

How to get to the `.minecraft` folder:
1. Open Minecraft Launcher and go to the `Installations` tab
2. Find the installation profile, hover your mouse on it, and click the folder icon

The file name could be anything except `default_maps.yml`.

Object properties are listed in [the structure table](#tms-object-structure).


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
      # ...
   # ...
```


### TMS object structure
| Key          | Value Type                      | Required? \[`Default`\] | Description                                                             |
|--------------|---------------------------------|-------------------------|-------------------------------------------------------------------------|
| `type`       | `string`                        | N \[`flat`\]            | Map type.                                                               |
| `name`       | `string`                        | Y                       | Displayed Name. It supports color codes.                                |
| `tile_url`   | `string`                        | Y                       | Tile URL.                                                               |
| `icon_url`   | `string`                        | N                       | Icon URL. This is for the UI.                                           |
| `copyright`  | `string` \| `TextComponentJSON` | N                       | Copyright text. Use this when the map provider wants you to write them. |
| `max_thread` | `int`                           | N \[`2`\]               | Maximum amount of threads to fetch map data.                            |


### Flat TMS object structure
Extends [TMS object](#tms-object-structure) (`type="flat"`),
which means required parameters in TMS object should also be in this structure.

URL parameters for the `tile_url` key gets applied for this object.
See [URL parameters](#url-parameters) for its parameters

| Key                    | Value Type | Required? \[`Default`\] | Description                                                                                        |
|------------------------|------------|-------------------------|----------------------------------------------------------------------------------------------------|
| `projection`           | `string`   | Y                       | Projection name. <br> See [projection configuration](#tile-projection-configuration) for more info |
| `default_zoom`         | `int`      | N \[`18`\]              | Default zoom value                                                                                 |
| `invert_zoom`          | `boolean`  | N \[`false`\]           | Zoom system is inverted when `true`. <br> Useful when lower zoom value means higher resolution     |
| `flip_vert`            | `boolean`  | N \[`false`\]           | *Flip tiles vertically when `true`.                                                                |
| `invert_lat`           | `boolean`  | N \[`false`\]           | *Latitude is inverted when `true`. It will also flip tiles vertically.                             |

&ast; Tiles won't be "flipped" when both `flip_vert` and `invert_lat` are set to `true`
(For nerds, think of it as a XOR gate)


### URL parameters
| URL parameter        | Description                                                           |
|----------------------|-----------------------------------------------------------------------|
| `{x}`                | X-axis parameter.                                                     |
| `{y}`                | Y-axis parameter.                                                     |
| `{z}`                | Absolute zoom parameter = `(default_zoom) + (zoom slider value)`      |
| `{u}`                | Map Tile Quad key                                                     |
| `{random:a,b,c,...}` | Picks string randomly from the given list; in this case `a`, `b`, `c` |


### 3D Tiles TMS object structure (By OGC Community Standards)
Extends [TMS object](#tms-object-structure) (`type="ogc3dtiles"`)

Made with the [Specification Documentation](https://docs.ogc.org/cs/22-025r4/22-025r4.html#toc0)

| Key                    | Value Type | Required? \[`Default`\] | Description                                                                                        |
|------------------------|------------|-------------------------|----------------------------------------------------------------------------------------------------|


## Tile projection configuration
Some map providers will use projections other than the standard `webmercator` projection in order to reduce the amount of distortion happening in their country/region when using it.

You can use non-standard projections for those map services by making custom projection config files.

Given the coordinate system (such as `epsg`) and the tile matrix, the mod will calculate tile positions and display tile images accordingly.

The config directory is `.../.minecraft/config/bteterrarenderer/projections/`. You can put your projection files there to add custom tile projections.

The file name could be anything except `default_projections.yml`.

### Default projections
| Projection name   | CRS name                            | Description                                                                           |
|-------------------|-------------------------------------|---------------------------------------------------------------------------------------|
| `webmercator`     | `EPSG:3857`<br>`EPSG:900913`<br>... | Web mercator projection. <br> The most common coordinate system used for map services |
| `worldmercator`   | `EPSG:3395`                         | World mercator projection. <br> Used in Yandex.Maps                                   |
| `kakaoprojection` | `EPSG:5181`                         | Used for Korean map services, such as Kakao Map                                       |


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

      # It also supports Terraplusplus' projection objects
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
| Key      | Value Type | Required? | Description                                |
|----------|------------|-----------|--------------------------------------------|
| `origin` | int[2]     | Y         | The coordinate at which the `0,0` tile is. |
| `size`   | int[2]     | Y         | Size of a single tile image.               |
