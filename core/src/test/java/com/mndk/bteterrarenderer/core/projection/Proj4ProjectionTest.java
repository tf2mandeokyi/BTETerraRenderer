package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.dep.terraplusplus.config.GlobalParseRegistries;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import org.junit.Assert;
import org.junit.Test;

public class Proj4ProjectionTest {
    @Test
    public void testProjectionRegistered() {
        Assert.assertTrue(GlobalParseRegistries.PROJECTIONS.containsKey("proj4"));
    }

    @Test
    public void givenProj4JsonProjection_testJacksonReadability() {
        Proj4jProjection projection = (Proj4jProjection) GeographicProjection.parse("{\"proj4\":{" +
                "\"name\": \"WGS84\"," +
                "\"param\": \"+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs \"" +
        "}}");

        Assert.assertEquals("WGS84", projection.getTargetCrs().getName());
    }

    @Test
    public void givenProj4JsonProjection_testTransformation() {
        Proj4jProjection projection = (Proj4jProjection) GeographicProjection.parse("{\"proj4\":{" +
                "\"name\": \"EPSG:5186\"," +
                "\"param\": \"+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs\"" +
        "}}");

        double[] coord = projection.fromGeo(127, 37);
        Assert.assertArrayEquals(new double[] { 200000, 489012.95569100516 }, coord, 0.01);
    }

    static {
        try { Class.forName("com.mndk.bteterrarenderer.core.BTETerraRendererCore"); }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
}
