# YAML map data file

The map data file containing custom map objects.

Notes:
* Custom map data files should be in `.../.minecraft/config/bteterrarenderer/maps/`.
* There are no restrictions of naming your data file, so name it whatever you want.
* If you think you're getting the syntax error, try validating it [here](https://codebeautify.org/yaml-validator)

## example.yml

```yaml
categories:

  # Map Category.
  Global:

    # Map ID. This should be unique in its local category.
    #
    # Just to know, the map id is automatically converted into something like
    # "<file_name>.<category>.<id>".
    # So in this case, the id of this map object is converted to:
    # "example.Global.osm"
    osm:
      name: OpenStreetMap
      tile_url: https://{random:a,b,c}.tile.openstreetmap.org/{z}/{x}/{y}.png
      projection: webmercator
      max_thread: 2
      
    # ...

  # ...
```


## Map Object structure

| Key | Value Type | Required? \[`Default`\] | Description |
|---|---|---|---|
| `name` | String | Yes | Displayed Name. Supports color codes. |
| `tile_url` | String | Yes | Tile URL. Parameters are dependent to its map projection. |
| `projection` | String | Yes | Name of the projection. Projections are listed in [here](#Projections). |
| `max_thread` | Integer | No \[`2`\] | Maximum amount of threads to load data. |
| `default_zoom` | Integer | No \[`18`\] | Default zoom value of the tile map. |
| `invert_zoom` | Boolean | No \[`false`\] | Whether to invert the zoom system. |
| `invert_lat` | Boolean | No \[`false`\] | Whether to invert the latitude. |


## Projections

All available projections are listed here.

### Basic parameters:
| URL parameter | Description |
|---|---|
| `{x}` | X-axis parameter. |
| `{y}` | Y-axis parameter. |
| `{z}` | The value of the map zoom parameter. |
| `{random:a,b,c,...}` | Picks string randomly from the given list. |

### `webmercator` (EPSG:3857, alias: `mercator`)

Web Mercator projection.

### `worldmercator` (EPSG:3395)

World Mercator projection. Used for Yandex.Maps tile server.

### `bing` (EPSG:3857)

Web Mercator projection, but for Bing maps.

| URL parameter | Description |
| --- | --- |
| `{u}` | Map Tile Quadkey |

### `kakao_wtm` (EPSG:5186)

Tile projection for Kakao Map (Korean map service).


## Adding Projections

To add a projection you want in this mod, make a suggestion/PR of it.

Requirements: 
* Projection classes should be located in the package `com.mndk.bteterrarenderer.tile.proj`
* They all must be subclasses of `com.mndk.bteterrarenderer.tile.proj.TileProjection`
* They all should be registered in the static method `com.mndk.bteterrarenderer.tile.TileMapService.getTileProjection()`
