package com.mndk.bteterrarenderer.tms.url;

public class BingURLConverter extends DefaultTileURLConverter {

    @Override
    public String convert(String template, int tileX, int tileY, int absoluteZoom) {
        return super.convert(template, tileX, tileY, absoluteZoom)
                .replace("{u}", tileToQuadKey(tileX, tileY, absoluteZoom));
    }

    private static String tileToQuadKey(int tileX, int tileY, int absoluteZoom) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = absoluteZoom; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) digit++;
            if ((tileY & mask) != 0) digit+=2;
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

}
