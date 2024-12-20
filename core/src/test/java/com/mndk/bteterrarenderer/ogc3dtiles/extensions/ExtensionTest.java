package com.mndk.bteterrarenderer.ogc3dtiles.extensions;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ExtensionTest {
    @Test
    public void givenExtensionJson_testReadability() {
        Map<String, Object> json = new HashMap<String, Object>() {{
            put("decodeMatrix", new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 });
            put("decodedMin", new double[] { 0, 0, 0 });
            put("decodedMax", new double[] { 1, 1, 1 });
        }};
        Web3dQuantizedAttributes extension =
                BTETerraRenderer.JSON_MAPPER.convertValue(json, Web3dQuantizedAttributes.class);

        Assert.assertEquals(Matrix4f.IDENTITY, extension.getDecodeMatrix());
        Assert.assertEquals(new Cartesian3f(0, 0, 0), extension.getDecodedMin());
        Assert.assertEquals(new Cartesian3f(1, 1, 1), extension.getDecodedMax());
    }
}
