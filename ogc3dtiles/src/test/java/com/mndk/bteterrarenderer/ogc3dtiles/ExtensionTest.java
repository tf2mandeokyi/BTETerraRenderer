package com.mndk.bteterrarenderer.ogc3dtiles;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import org.joml.Matrix4d;
import org.joml.Vector3d;
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

        Assert.assertEquals(new Matrix4d(), extension.getDecodeMatrix());
        Assert.assertEquals(new Vector3d(0, 0, 0), extension.getDecodedMin());
        Assert.assertEquals(new Vector3d(1, 1, 1), extension.getDecodedMax());
    }
}
