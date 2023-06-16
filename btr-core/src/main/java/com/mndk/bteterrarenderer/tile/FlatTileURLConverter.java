package com.mndk.bteterrarenderer.tile;

import lombok.RequiredArgsConstructor;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class FlatTileURLConverter {

    protected final int defaultZoom;
    protected final boolean invertZoom;

    public final String convertToUrl(String template, int tileX, int tileY, int relativeZoom) {
        return this.convert(template, tileX, tileY, defaultZoom + (invertZoom ? -relativeZoom : relativeZoom));
    }

    private String convert(String template, int tileX, int tileY, int absoluteZoom) {
        return replaceRandoms(template)
                .replace("{z}", String.valueOf(absoluteZoom))
                .replace("{x}", String.valueOf(tileX))
                .replace("{y}", String.valueOf(tileY))
                .replace("{u}", tileToQuadKey(tileX, tileY, absoluteZoom));
    }

    private static String replaceRandoms(String url) {
        Matcher m = Pattern.compile("\\{random:([^{}]+)}").matcher(url);
        StringBuffer buffer = new StringBuffer();
        Random r = new Random();
        while(m.find()) {
            String[] randoms = m.group(1).split(",");
            m.appendReplacement(buffer, randoms[r.nextInt(randoms.length)]);
        }
        m.appendTail(buffer);
        return buffer.toString();
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
