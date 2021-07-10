package the;

import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import com.mndk.bteterrarenderer.tms.WebMercatorTMS;
import com.mndk.bteterrarenderer.tms.WorldMercatorTMS;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class TestClass {

    public static final double R_A = 6378137;
    public static final double R_B = 6356752.3142;
    public static final double E = Math.sqrt(1 - (R_B * R_B) / (R_A * R_A));

    @Test
    public void a() throws Exception {
        // x=447076&y=203492&z=19
        double[] latlon = new WebMercatorTMS("sus", Collections.emptyMap()).tileCoordToGeoCoord(447076, 203491, 19);
        System.out.println(Arrays.toString(latlon));
    }

    @Test
    public void b() throws Exception {
        // 19/447076/203151
        double[] latlon = new WebMercatorTMS("sus", Collections.emptyMap()).tileCoordToGeoCoord(447076, 203151, 19);
        System.out.println(Arrays.toString(latlon));

        double lat_rad = Math.toRadians(37.50155517264162);

        double c = Math.pow((1 - E * Math.sin(lat_rad)) / (1 + E * Math.sin(lat_rad)), E / 2);
        lat_rad = Math.log(Math.tan(Math.PI / 4 + lat_rad / 2) * c);
        System.out.println(lat_rad * R_A);
    }

    @Test
    public void projectionTest() throws OutOfProjectionBoundsException {
        GeographicProjection epsg3395 = new Proj4jProjection("EPSG:3395",
                "+proj=merc +lon_0=0 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs".split(" ")
        );
        GeographicProjection epsg3857 = new Proj4jProjection("EPSG:3857",
                "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext +no_defs".split(" ")
        );
        double[] a = epsg3395.fromGeo(126.98272705078125, 37.50155517264162);
        double equatorA = 2 * Math.PI * WorldMercatorTMS.R_A;
        int zoom = 19;
        double pixelX =  (a[0] + (equatorA / 2.0)) * Math.pow(2, zoom) / equatorA;
        double pixelY = -(a[1] - (equatorA / 2.0)) * Math.pow(2, zoom) / equatorA;
        System.out.println(pixelX);
        System.out.println(pixelY);
    }

    @Test
    public void tileCoordTest() throws Exception {
        System.out.println(Arrays.toString(
                new WorldMercatorTMS("sus", Collections.emptyMap()).tileCoordToGeoCoord(154351, 197501, 19)
        ));
    }

}
