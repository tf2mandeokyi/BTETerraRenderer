# YAML map data file

The map data file that contains custom map objects.

You can create your custom map data file at `.../.minecraft/config/bteterrarenderer/maps/`.

(Note: There is no limit to the map data file's name, so name it to whatever you want.)


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
      request_headers:
        User-Agent: bteterrarenderer/1.0 Java/1.8

    # ...

  # ...
```


## Map Object structure

| Key | Value Type | Required? \[`Default`\] | Description |
|-|-|-|-|
| `name` | String | Yes | Displayed Name. Supports color codes. |
| `tile_url` | String | Yes | Tile URL. Parameters are dependent to its map projection. |
| `projection` | String | Yes | Name of the projection. Projections are listed in [here](#Projections). |
| `max_thread` | Integer | No \[`2`\] | Maximum amount of threads to load data. |
| `request_headers` | Map<String, String> | No | HTTP request headers. |
| `default_zoom` | Integer | No \[`18`\] | Default zoom value of the tile map. |
| `invert_zoom` | Boolean | No \[`false`\] | Whether to invert the zoom system. |
| `invert_lat` | Boolean | No \[`false`\] | Whether to invert the latitude. |


## Projections

Available projection names are listed here.

If you want a certain projection added, you can either make an issue (or suggestion), or make a PR about it. (Projection map classes are listed at `com.mndk.bteterrarenderer.tms`. They all should be subclass of `TileMapService` and should be registered at `TileMapService.parse()`)

### Basic parameters:
| Url parameter | Description                          |
| ------------- | ------------------------------------ |
| `{x}`         | X-axis parameter.                    |
| `{y}`         | Y-axis parameter.                    |
| `{z}`         | The value of the map zoom parameter. |

### `webmercator` (EPSG:3857, alias: `mercator`)

Web Mercator projection.

### `worldmercator` (EPSG:3395)

World Mercator projection. Used for Yandex.Maps tile server.

### `bing` (EPSG:3857)

Web Mercator projection, but for Bing maps.

| tile parameter | description      |
| -------------- | ---------------- |
| `{u}`          | Map Tile Quadkey |

### `kakao_wtm` (EPSG:5186)

Tile projection for common Korean maps.
