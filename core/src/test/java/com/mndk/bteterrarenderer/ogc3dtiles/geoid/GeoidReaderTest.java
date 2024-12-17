package com.mndk.bteterrarenderer.ogc3dtiles.geoid;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

public class GeoidReaderTest {

    @Test
    public void givenPOIs_testGeoidHeights() {
        GeoidHeightFunction function = GeoidHeightFunction.EGM96_WW15MGH;
        Assert.assertEquals(-34.65, function.getHeight(Spheroid3.fromDegrees(-80.014148, 40.441575, 0)), 0.01);
        Assert.assertEquals(47.76, function.getHeight(Spheroid3.fromDegrees(-1.258417, 51.752439, 0)), 0.01);
        Assert.assertEquals(43.09, function.getHeight(Spheroid3.fromDegrees(4.772462, 52.265339, 0)), 0.01);
        Assert.assertEquals(22.76, function.getHeight(Spheroid3.fromDegrees(150.985325, -33.852682, 0)), 0.01);
        Assert.assertEquals(31.03, function.getHeight(Spheroid3.fromDegrees(18.429578, -33.916120, 0)), 0.01);
        Assert.assertEquals(36.63, function.getHeight(Spheroid3.fromDegrees(139.516599, 35.309069, 0)), 0.01);
    }

    @Test
    public void givenGzippedDataFile_testChecksum() throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream stream = Egm96Ww15mghData.class.getResourceAsStream("WW15MGH.dat.gz");
            DigestInputStream dis = new DigestInputStream(stream, md)) {

            Assert.assertNotNull(dis);
            GZIPInputStream gzipStream = new GZIPInputStream(dis);
            byte[] data = IOUtil.readAllBytes(gzipStream);
            Assert.assertEquals(1440 * 721 * 4, data.length);
        }

        String expected = "411e657962d9efd923b286c75261e0b2";
        byte[] actual = md.digest();
        for (int i = 0; i < actual.length; i++) {
            byte expectedByte = (byte) Integer.parseInt(expected.substring(i * 2, i * 2 + 2), 16);
            Assert.assertEquals(expectedByte, actual[i]);
        }
    }

}
