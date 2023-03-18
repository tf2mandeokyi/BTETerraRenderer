# YAML Configuration API
This documentation contains a guide to make custom map & projection configuration files.

All configuration files' file extension must be `.yml`.

If you're getting the YML syntax error, try fixing it in [YAML Validator site](https://codebeautify.org/yaml-validator)

If you don't know how to make one of these, ask for support in [our Discrod server](https://discord.com/invite/4gjrwWH2gS).


## Tile map service (TMS) configuration
TMS files contain map services that are going to be displayed as in-game hologram when selected.

The config directory is `.../.minecraft/config/bteterrarenderer/maps/`. Put your custom TMS config files there
to see it in Minecraft.

The file name could be anything except `default.yml`.

Object properties are listed in [the structure table](#tms-object-structure).


### example_map.yml

```yaml
categories:

   # Map category.
   Global:

      # Map ID. This should be unique in its local category.
      #
      # Just to know, this map id is automatically converted into something like
      # "<category>.<id>".
      # So in this case, the id of this map object is converted to:
      # "Global.osm"
      osm:
         name: OpenStreetMap
         tile_url: https://{random:a,b,c}.tile.openstreetmap.org/{z}/{x}/{y}.png
         projection: webmercator
         max_thread: 2
      
      # ...

   # ...
```


### TMS object structure
| Key            | Value Type | Required? \[`Default`\] | Description                                                                                        |
|----------------|------------|-------------------------|----------------------------------------------------------------------------------------------------|
| `name`         | string     | Y                       | Displayed Name. Supports color codes.                                                              |
| `tile_url`     | string     | Y                       | Tile URL. <br> See [URL parameters](#url-parameters) for its parameters                            |
| `projection`   | string     | Y                       | Projection name. <br> See [projection configuration](#tile-projection-configuration) for more info |
| `max_thread`   | int        | N \[`2`\]               | Maximum amount of threads to load map images                                                       |
| `default_zoom` | int        | N \[`18`\]              | Default zoom value                                                                                 |
| `invert_zoom`  | boolean    | N \[`false`\]           | Zoom system is inverted when `true`. <br> Useful when lower zoom value means higher resolution     |
| `flip_vert`    | boolean    | N \[`false`\]           | *Flip tiles vertically when `true`.                                                                |
| `invert_lat`   | boolean    | N \[`false`\]           | *Latitude is inverted when `true`. Will also flip tiles vertically.                                |

&ast; Tiles won't be "flipped" when both `flip_vert` and `invert_lat` are true.

### URL parameters
| URL parameter        | Description                                                           |
|----------------------|-----------------------------------------------------------------------|
| `{x}`                | X-axis parameter.                                                     |
| `{y}`                | Y-axis parameter.                                                     |
| `{z}`                | Absolute zoom parameter = `(default_zoom) + (zoom slider value)`      |
| `{u}`                | Map Tile Quad key                                                     |
| `{random:a,b,c,...}` | Picks string randomly from the given list; in this case `a`, `b`, `c` |


## Tile projection configuration
Some map providers will use projections other than the standard `webmercator` projection,<br>
in order to reduce the amount of distortion happening by using the standard one for their country/region. 

This is fine, as this mod supports custom map projection by making projection config files.

Given the coordinate system (such as `epsg`) and the tile matrix, the mod will calculate the tile position<br>
and display its image accordingly.

The config directory is `.../.minecraft/config/bteterrarenderer/projections/`. Put your projection config files there
to add custom tile projections.

### Default projections
| Projection name   | CRS name                            | Description                                                                           |
|-------------------|-------------------------------------|---------------------------------------------------------------------------------------|
| `webmercator`     | `EPSG:3857`<br>`EPSG:900913`<br>... | Web mercator projection. <br> The most common coordinate system used for map services |
| `worldmercator`   | `EPSG:3395`                         | World mercator projection. <br> Used in Yandex.Maps                                   |
| `kakaoprojection` | `EPSG:5181`                         | Used for Korean map services, such as Kakao Map                                       |


### example_projection.yml

```yaml
tile_projections:

   # Projection name.
   # This is global across every projection file, so beware of name clashes.
   webmercator:
      
      # Coordinate system object. Also supports T++ projection objects.
      projection:
         proj4:
            name: 'EPSG:3857'
            param: '+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 ...'
            
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
This is as same as T++'s projection JSON config, but in YAML format and with `proj4` added.

The table below is for the `proj4` projection object. (See [epsg.io](https://epsg.io/) for PROJ.4 information)

| Key     | Value Type | Required? | Description                                                             |
|---------|------------|-----------|-------------------------------------------------------------------------|
| `name`  | string     | Y         | Coordinate system name                                                  |
| `param` | string     | Y         | PROJ.4 parameters. Visit [epsg.io](https://epsg.io/) for the parameters |


### TileMatrixRow
| Key      | Value Type | Required? | Description                                       |
|----------|------------|-----------|---------------------------------------------------|
| `origin` | int[2]     | Y         | The coordinate in which `0,0` tile is located at. |
| `size`   | int[2]     | Y         | Size of a single tile image.                      |
