# maps.yaml

The map data file which is used to make custom map objects.

You can create the map data file `<insert_name_here>.yml` at `.../.minecraft/config/bteterrarenderer/maps/` directory.


## Map Object structure

| Map Object Key | Value Type | Required? | Description |
|-|-|-|-|
| `name` | String | true | Displayed name of the map |
| `tile_url` | String | true | Tile URL. |
| `projection` | Projection Enum (String) | true | Name of the projection. |
| `max_thread` | integer | No (Default = 2 | The maximum amount of threads from which tile datas are loaded. |
| `request_headers` | Map<String, String> | No | HTTP request headers. |
| `default_zoom` | integer | No (Default = 18) | Default zoom value of the tile map. |
| `invert_zoom` | boolean | No (Default = false) | Whether to invert the zoom system. |
| `invert_lat` | boolean | No (Default = false) | Whether to invert the latitude. |


## Projections

The string enum of available projections are listed here.

If you want it more, make a PR of it. (Projection map classes are listed at `com.mndk.bteterrarenderer.map`. They all should be the subclass of `ExternalTileMap`, and should be registered at `ExternalTileMap.parse()`)

#### `webmercator` 

Mercator projection. (alias: `mercator`)

| tile parameter | description                          |
| -------------- | ------------------------------------ |
| `{x}`          | X-axis parameter.                    |
| `{y}`          | Y-axis parameter.                    |
| `{z}`          | The value of the map zoom parameter. |

#### `bing`

Mercator projection, but for Bing maps.

| tile parameter | description      |
| -------------- | ---------------- |
| `{u}`          | Map Tile Quadkey |

#### `kakao_wtm`

GRS80 projection ([EPSG:5181](http://epsg.io/5181)) for Korean maps.

Parameters are the same as `webmercator` projection's. 


## YAML map file example

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
         
         ## Projection name. Projections are listed at at end of this README.
         projection: webmercator
         
         # The maximum amount of threads from which tile datas are loaded.
         max_thread: 2
         
         # HTTP request headers.
         request_headers:
            User-Agent: bteterrarenderer/1.0 Java/1.8
      
      # ...
      
   # ...
```

