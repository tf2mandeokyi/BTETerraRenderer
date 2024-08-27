package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.mcconnector.TestEnvironmentVirtualMinecraftManager;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SvgToPngConversionTest {

    @Test
    public void givenUrl_testConversion() throws IOException, ExecutionException, InterruptedException {
        String url = "https://upload.wikimedia.org/wikipedia/commons/9/9c/Bing_Fluent_Logo.svg";
        BufferedImage image = HttpResourceManager.downloadAsImage(url);
        Assert.assertEquals(image.getWidth(), 678); // This number might change in the future
    }

    static {
        try {
            BTETerraRenderer.initialize(TestEnvironmentVirtualMinecraftManager.getInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
