package com.mndk.mapdisp4bte.map.mercator;

import com.mndk.mapdisp4bte.map.ExternalMapRenderer;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.map.tmap.TMapRenderer;
import com.mndk.mapdisp4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

public class MercatorMapRenderer extends ExternalMapRenderer {


    private final String plainMapTemplate, aerialTemplate;


    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };


    public MercatorMapRenderer(RenderMapSource source, String plainMapTemplate, String aerialTemplate, int maximumDownloadThreads) {
        super(source, maximumDownloadThreads);
        this.plainMapTemplate = plainMapTemplate;
        this.aerialTemplate = aerialTemplate;
    }


    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return MercatorTileConverter.geoToTile(temp[0], temp[1], 18 - level);
    }


    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException {
        double[] temp = MercatorTileConverter.tileToGeo(tileX, tileY, 18 - level);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }


    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }


    protected String getRandom() {return "";}


    @Override
    public String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type) {
        String template = type == RenderMapType.AERIAL ? aerialTemplate : plainMapTemplate;

        return template.replace("{random}", this.getRandom())
                .replace("{z}", (18 - level) + "")
                .replace("{x}", tileX + "")
                .replace("{y}", tileY + "");
    }


    public static void main(String[] args) throws Throwable {
        double x = 12444462.968534665, z = -7655289.338730685;
        /*BufferedImage image;

        image = new OpenStreetMapRenderer()
                .fetchMapSync(x, z, 0, 0, 0, RenderMapType.PLAIN_MAP);
        ImageIO.write(image, "png", new File("a.png"));
        image = new TMapRenderer()
                .fetchMapSync(x, z, 0, 0, 0, RenderMapType.PLAIN_MAP);
        ImageIO.write(image, "png", new File("b.png"));*/
        int[] tile = new TMapRenderer().playerPositionToTileCoord(x, z, 0);

        System.out.println(tile[0] + ", " + tile[1]);

        double[] a = new TMapRenderer().tileCoordToPlayerPosition(tile[0], tile[1], 0);

        System.out.println(a[0] + ", " + a[1]);
    }
}
