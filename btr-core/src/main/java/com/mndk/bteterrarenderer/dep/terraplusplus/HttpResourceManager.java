package com.mndk.bteterrarenderer.dep.terraplusplus;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.io.InputStream;

@UtilityClass
public class HttpResourceManager {
    public InputStream download(String url) {
        return MixinUtil.notOverwritten(url);
    }
}
