package com.mndk.bteterrarenderer.mcconnector;

public interface IResourceLocation {
    static IResourceLocation of(String modId, String location) {
        return MixinUtil.notOverwritten(modId, location);
    }
}
