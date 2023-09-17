package com.mndk.bteterrarenderer.ogc3dtiles.util;

import lombok.experimental.UtilityClass;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class URLUtil {

    public static URL combineUri(URL url, String uri) throws MalformedURLException {
        if (url == null) return new URL(uri);
        Map<String, String> queryMap = splitQuery(url);

        String[] querySplit = uri.split("\\?");
        if(querySplit.length == 0) return url;
        String querylessUri = querySplit[0];
        URL result = new URL(url, querylessUri);

        if(querySplit.length != 1) {
            String uriQuery = querySplit[1];
            queryMap.putAll(splitQuery(uriQuery));
        }

        String resultString = result.toString();
        if(!queryMap.isEmpty()) {
            resultString += "?" + combineQuery(queryMap);
        }
        return new URL(resultString);
    }

    public static Map<String, String> splitQuery(URL url) {
        String query = url.getQuery();
        return query != null ? splitQuery(query) : Collections.emptyMap();
    }

    /**
     * @link <a href="https://stackoverflow.com/a/13592567">The stack overflow answer</a>
     */
    private static Map<String, String> splitQuery(String urlQuery) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String[] pairs = urlQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(urlDecode(pair.substring(0, idx)), urlDecode(pair.substring(idx + 1)));
        }
        return queryPairs;
    }

    /**
     * @link <a href="https://stackoverflow.com/a/2810102">The stack overflow answer</a>
     */
    public static String combineQuery(Map<String, String> queryPair) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : queryPair.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(String.format("%s=%s", urlEncode(entry.getKey().toString()), urlEncode(entry.getValue().toString())));
        }
        return sb.toString();
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("error while decoding url", e);
        }
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("error while encoding url", e);
        }
    }

}
