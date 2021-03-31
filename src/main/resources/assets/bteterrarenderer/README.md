# maps.json

The map data file which is used to list map links.

To make this file customizable, the file is loaded and then saved at `.../.minecraft/config/bteterrarenderer/maps.json`.



## JSON structure

<style>
    td { background-color: #fff; }
    th { background-color: #f0f0f0; }
</style>
<table>
    <tr>
        <th><b>property</b></th>
        <th><b>value type</b></th>
        <th><b>required?</b></th>
        <th><b>description</b></th>
    </tr>
    <tr>
        <td><code>categories</code></td>
        <td>Array of <a href=#category>Map Categories</a></td>
        <td>Yes</td>
        <td></td>
    </tr>
    <tr>
        <td colspan=4>
            <h3>
                Map Category
            </h3>
            <table>
                <tr>
                    <th><b>property</b></th>
                    <th><b>value type</b></th>
                    <th><b>required?</b></th>
                    <th><b>description</b></th>
                </tr>
                <tr>
                    <td><code>name</code></td>
                    <td>string</td>
                    <td>Yes</td>
                    <td>Display name of the category.</td>
                </tr>
                <tr>
                    <td><code>maps</code></td>
                    <td>Array of <a href=#map_object>Map Objects</a></td>
                    <td>Yes</td>
                    <td></td>
                </tr>
                <tr>
                    <td colspan=4>
                        <h3>
                            Map Object
                        </h3>
                        <table>
                            <tr>
                                <th><b>property</b></th>
                                <th><b>value type</b></th>
                                <th><b>required?</b></th>
                                <th><b>description</b></th>
                            </tr>
                            <tr>
                                <td><code>id</code></td>
                                <td>string</td>
                                <td>Yes</td>
                                <td>The id of the map.<br>
                                    This one has to be unique regardless of its category since the mod compares maps with this property.</td>
                            </tr>
                            <tr>
                                <td><code>name</code></td>
                                <td>string</td>
                                <td>No</td>
                                <td>Display name of the map.</td>
                            </tr>
                            <tr>
                                <td><code>tile_url</code></td>
                                <td>string</td>
                                <td>No</td>
                                <td>URL of the tile map. The value's parameter depends on the property <code>projection</code>. (See <a href=#projections>Projections</a> for more info)</td>
                            </tr>
                            <tr>
                                <td><code>projection</code></td>
                                <td><a href=#projections>Projection</a></td>
                                <td>Yes</td>
                                <td>Projection of the map. (See <a href=#projections>Projections</a> for more info)</td>
                            </tr>
                            <tr>
                                <td><code>max_thread</code></td>
                                <td>integer</td>
                                <td>No (default=2)</td>
                                <td>Maximum number of thread from which tile data is imported.</td>
                            </tr>
                            <tr>
                                <td><code>request_headers</code></td>
                                <td>Map&lt;string, string&gt;</td>
                                <td>No</td>
                                <td>Http request headers.</td>
                            </tr>
                            <tr>
                                <td><code>default_zoom</code></td>
                                <td>integer</td>
                                <td>No (default=18)</td>
                                <td>Default zoom value of the tile map.</td>
                            </tr>
                            <tr>
                                <td><code>invert_zoom</code></td>
                                <td>boolean</td>
                                <td>No (default=false)</td>
                                <td>Whether to invert the zoom system. If it's true, as the zoom gets bigger, the map gets smaller, and the resolution drops.</td>
                            </tr>
                            <tr>
                                <td><code>invert_lat</code></td>
                                <td>boolean</td>
                                <td>No (default=false)</td>
                                <td>If the value is true, the latitude is inversed. (Ex. <code>40 N</code> -> <code>40 S</code>)</td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>



## Projection

The string enum of available projections are listed here.

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

