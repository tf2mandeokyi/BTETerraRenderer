# maps.json

The map data file which is used to list map links.

To make this file customizable, the file is loaded and then saved at `.../.minecraft/config/bteterrarenderer/maps.json`.

## JSON structure

| property     | value type                    | required? | description                |
| ------------ | ----------------------------- | --------- | -------------------------- |
| `categories` | Array\<[Category](#category)> | true      | The list of map categories |



### Category

The group of maps that has similar properties, or are from the same location.

| property | value type          | required? | description                    |
| -------- | ------------------- | --------- | ------------------------------ |
| `name`   | string              | true      | Displayed name of the category |
| `maps`   | Array\<[Map](#map)> | true      | The list of map objects.       |



### Map

The map object.

| property          | value type                 | required?            | description                                                  |
| ----------------- | -------------------------- | -------------------- | ------------------------------------------------------------ |
| `id`              | string                     | Yes                  | The id of the map.<br>This one has to be unique regardless of its category since the mod compares maps with this property. |
| `name`            | string                     | No                   | Displayed name of the map.                                   |
| `tile_url`        | string                     | Yes                  | URL of the tile map. The value depends on the property `projection`. (See [Projections](#projections) for more info) |
| `projection`      | [Projection](#projections) | Yes                  | Projection of the map. (See [Projections](#projections) for more info) |
| `max_thread`      | integer                    | No (default=`2`)     | Maximum number of thread from which tile data is imported.   |
| `request_headers` | Map<string, string>        | No                   | Http request headers.                                        |
| `default_zoom`    | integer                    | No (default=`18`)    | Default zoom value of the tile map.                          |
| `invert_zoom`     | boolean                    | No (default=`false`) | Whether to invert the zoom system. If it's true, as the zoom gets bigger, the map gets smaller, and the resolution drops. |
| `invert_lat`      | boolean                    | No (default=`false`) | If the value is true, the latitude is inversed. (Ex. `40 N` -> `40 S`) |



### Projections

The enum of available projections.

If you want it more, make a PR of it. (Projection map classes are listed at `com.mndk.bteterrarenderer.map`. They all should be the subclass of `ExternalTileMap`, and should be registered at `ExternalTileMap.parse()`)

#### Value: `mercator`

Mercator projection.

| tile parameter | description                          |
| -------------- | ------------------------------------ |
| `{x}`          | X-axis parameter.                    |
| `{y}`          | Y-axis parameter.                    |
| `{z}`          | The value of the map zoom parameter. |

#### Value: `bing`

Mercator projection, but is used for bing maps.

| tile parameter | description               |
| -------------- | ------------------------- |
| `{u}`          | The ID of Bing map tiles. |

#### Value: `kakao_wtm`

GRS80 projection that is used for Kakao map. (Proj4: `EPSG:5181`, `+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=GRS80 +units=m +no_defs`)

| tile parameter | description                          |
| -------------- | ------------------------------------ |
| `{x}`          | X-axis parameter.                    |
| `{y}`          | Y-axis parameter.                    |
| `{z}`          | The value of the map zoom parameter. |

## Example

```json
{
    "categories": [
        {
            "name": "Global",
            "maps": [
                {
                    "id": "osm",
                    "name": "OpenStreetMap",
                    "tile_url": "https://{random:a,b,c}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "projection": "mercator",
                    "max_thread": 2,
                    "request_headers": {
                        "User-Agent": "bteterrarender/1.0 Java/1.8"
                    }
                }
            ]
        }
    ]
}
```

