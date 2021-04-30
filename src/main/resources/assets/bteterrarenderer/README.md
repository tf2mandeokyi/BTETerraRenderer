# YAML map data file

The map data file that contains custom map objects.

You can create your custom map data file at `.../.minecraft/config/bteterrarenderer/maps/`.

(Note: There is no limit to the map data file's name, so name it to whatever you want.)


## example.yml

```yaml
categories:

   # Map Category name.
   Global:
   
      # Map ID. This should be unique among the entire map data files.
      osm: 
      
         ## Map Display Name
         name: OpenStreetMap
         
         ## Tile URL. The parameters are dependent to map's projection.
         tile_url: https://{random:a,b,c}.tile.openstreetmap.org/{z}/{x}/{y}.png
         
         ## Projection name. Projections are listed at end of this README.
         projection: webmercator
         
         # The maximum amount of threads from which tile data are loaded.
         max_thread: 2
         
         # HTTP request headers.
         request_headers:
            User-Agent: bteterrarenderer/1.0 Java/1.8
      
      # ...
      
   # ...
```


## Map Object structure

| Map Object Key | Value Type | Required? \[`Default`\] | Description |
|-|-|-|-|
| `name` | String | true | Displayed name of the map |
| `tile_url` | String | true | Tile URL. |
| `projection` | Projection Enum (String) | true | Name of the projection. |
| `max_thread` | integer | No \[`2`\] | The maximum amount of threads from which tile datas are loaded. |
| `request_headers` | Map<String, String> | No | HTTP request headers. |
| `default_zoom` | integer | No \[`18`\] | Default zoom value of the tile map. |
| `invert_zoom` | boolean | No \[`false`\] | Whether to invert the zoom system. |
| `invert_lat` | boolean | No \[`false`\] | Whether to invert the latitude. |


## Projections

The available projection enums are listed here.

If you want it more, make an issue (or suggestion), or make a PR of it. (Projection map classes are listed at `com.mndk.bteterrarenderer.tms`. They all should be the subclass of `ExternalTileMap` and should be registered at `ExternalTileMap.parse()`)

#### `webmercator` 

Web Mercator projection. (alias: `mercator`)

| tile parameter | description                          |
| -------------- | ------------------------------------ |
| `{x}`          | X-axis parameter.                    |
| `{y}`          | Y-axis parameter.                    |
| `{z}`          | The value of the map zoom parameter. |

#### `bing`

Web Mercator projection, but for Bing maps.

| tile parameter | description      |
| -------------- | ---------------- |
| `{u}`          | Map Tile Quadkey |

#### `kakao_wtm`

Tile projection ([EPSG:5181](http://epsg.io/5181)) for Korean maps.

Parameters are the same as `webmercator`'s parameters. 
