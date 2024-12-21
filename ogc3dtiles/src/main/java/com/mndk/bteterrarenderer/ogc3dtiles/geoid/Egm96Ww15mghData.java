package com.mndk.bteterrarenderer.ogc3dtiles.geoid;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.util.IOUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

class Egm96Ww15mghData implements GeoidHeightFunction {

    private static final int WIDTH = 1440, HEIGHT = 721;

    // Data format:
    //  LAT \ LON | 0.00 | 0.25 | 0.50 | ... | 179.75 | 180.00(-180.00) | ... | 359.75(-0.25)
    // -----------+------+------+------+-----+--------+-----------------+-----+---------------
    //      90.00 |
    //      89.75 |
    //      89.50 |
    //       ...  |
    //       0.00 |                              (height data)
    //       ...  |
    //     -89.50 |
    //     -89.75 |
    //     -90.00 |
    private final float[][] data = new float[HEIGHT][WIDTH];

    Egm96Ww15mghData() {
        try (InputStream stream = Egm96Ww15mghData.class.getResourceAsStream("WW15MGH.dat.gz")) {
            if (stream == null) throw new IOException("File not found");

            GZIPInputStream gzipStream = new GZIPInputStream(stream);
            byte[] data = IOUtil.readAllBytes(gzipStream);
            ByteBuf buffer = Unpooled.wrappedBuffer(data);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    this.data[y][x] = buffer.readFloat();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getHeight(Spheroid3 spheroid3) {
        double longitudeInDegrees = spheroid3.getLongitudeDegrees();
        double latitudeInDegrees = spheroid3.getLatitudeDegrees();
        if (latitudeInDegrees > 90 || latitudeInDegrees < -90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees, but was instead " + latitudeInDegrees);
        }
        double wrappedLongitude = ((longitudeInDegrees % 360) + 360) % 360;
        int xIndex = (int) Math.floor(wrappedLongitude * 4);
        int yIndex = (int) Math.floor((90 - latitudeInDegrees) * 4);

        double x = wrappedLongitude * 4 - xIndex;
        double y = (90 - latitudeInDegrees) * 4 - yIndex;
        float[] p = new float[4];
        for (int i = 0; i < 4; i++) {
            int wrappedYIndex = wrapYIndex(yIndex + i - 1);
            p[i] = cubicInterpolate(
                    data[wrappedYIndex][wrapXIndex(xIndex - 1)],
                    data[wrappedYIndex][wrapXIndex(xIndex    )],
                    data[wrappedYIndex][wrapXIndex(xIndex + 1)],
                    data[wrappedYIndex][wrapXIndex(xIndex + 2)],
                    (float) x
            );
        }
        return cubicInterpolate(p[0], p[1], p[2], p[3], (float) y);
    }

    private int wrapXIndex(int xIndex) { return (xIndex + WIDTH) % WIDTH; }
    private int wrapYIndex(int yIndex) {
        if (yIndex == -1) return 1;
        if (yIndex == 721) return 719;
        return yIndex;
    }

    private static float cubicInterpolate(float p0, float p1, float p2, float p3, float x) {
        return p1 + 0.5f * x * (p2 - p0 + x * (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3 + x * (3.0f * (p1 - p2) + p3 - p0)));
    }
}
