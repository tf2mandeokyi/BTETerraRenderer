package com.mndk.bteterrarenderer.ogc3dtiles.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtilTest {

    @Test
    public void givenUrlAndUri_testCombinability() throws MalformedURLException {
        Assert.assertEquals(
                new URL("https://a.com/b/c/d/f/g"),
                URLUtil.combineUri(new URL("https://a.com/b/c/d/e"), "f/g")
        );
        Assert.assertEquals(
                new URL("https://a.com/f/g"),
                URLUtil.combineUri(new URL("https://a.com/"), "/f/g")
        );
    }

    @Test
    public void givenUrlAndUri_testQueryCombinability() throws MalformedURLException {
        Assert.assertEquals(
                new URL("https://a.com/b/c/d/f/g?a=3&b=4"),
                URLUtil.combineUri(new URL("https://a.com/b/c/d/e?a=3"), "f/g?b=4")
        );
        Assert.assertEquals(
                new URL("https://a.com/f/g?a=3&b=5&c=6"),
                URLUtil.combineUri(new URL("https://a.com/b/c/d/e?a=3&b=5"), "/f/g?c=6")
        );
    }

    @Test
    public void givenUrlAndUri_testQueryOverwrite() throws MalformedURLException {
        Assert.assertEquals(
                new URL("https://a.com/b/c/d/f/g?a=4"),
                URLUtil.combineUri(new URL("https://a.com/b/c/d/e?a=3"), "f/g?a=4")
        );
        Assert.assertEquals(
                new URL("https://a.com/f/g?a=15&b=6"),
                URLUtil.combineUri(new URL("https://a.com/b/c/d/e?a=3&b=6"), "/f/g?a=15")
        );
    }

}
