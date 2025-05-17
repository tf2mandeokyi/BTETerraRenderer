package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.BTETerraRendererCore;
import com.mndk.bteterrarenderer.mcconnector.client.TestEnvironmentDummyMinecraft;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class SvgToPngConversionTest {

    @Test
    public void givenUrl_testConversion() throws ExecutionException, InterruptedException {
        String url = "https://upload.wikimedia.org/wikipedia/commons/9/9c/Bing_Fluent_Logo.svg";
        HttpResourceManager.downloadAsImage(url, null).get();
    }

    static {
        try { BTETerraRendererCore.initialize(TestEnvironmentDummyMinecraft.getInstance()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
