/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import org.junit.Assert;
import org.junit.Test;

public class DecoderOptionsTest {

    @Test
    public void testOptions() {
        // This test verifies that we can update global and attribute options of the
        // DecoderOptions class instance.
        DecoderOptions options = new DecoderOptions();
        options.setGlobalInt("test", 3);
        Assert.assertEquals(3, options.getGlobalInt("test", -1));

        options.setAttributeInt(GeometryAttribute.Type.POSITION, "test", 1);
        options.setAttributeInt(GeometryAttribute.Type.GENERIC, "test", 2);
        Assert.assertEquals(
                3,
                options.getAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", -1)
        );
        Assert.assertEquals(
                1,
                options.getAttributeInt(GeometryAttribute.Type.POSITION, "test", -1)
        );
        Assert.assertEquals(
                2,
                options.getAttributeInt(GeometryAttribute.Type.GENERIC, "test", -1)
        );
    }

    @Test
    public void testAttributeOptionsAccessors() {
        // This test verifies that we can query options stored in DecoderOptions
        // class instance.
        DecoderOptions options = new DecoderOptions();
        options.setGlobalInt("test", 1);
        options.setAttributeInt(GeometryAttribute.Type.POSITION, "test", 2);
        options.setAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", 3);

        Assert.assertEquals(
                2,
                options.getAttributeInt(GeometryAttribute.Type.POSITION, "test", -1)
        );
        Assert.assertEquals(
                -1,
                options.getAttributeInt(GeometryAttribute.Type.POSITION, "test2", -1)
        );
        Assert.assertEquals(
                3,
                options.getAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", -1)
        );
        Assert.assertEquals(
                1,
                options.getAttributeInt(GeometryAttribute.Type.NORMAL, "test", -1)
        );
    }

}
