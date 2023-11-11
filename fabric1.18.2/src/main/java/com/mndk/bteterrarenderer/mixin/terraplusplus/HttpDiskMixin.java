package com.mndk.bteterrarenderer.mixin.terraplusplus;

import com.mndk.bteterrarenderer.dep.terraplusplus.TerraConfig;
import com.mndk.bteterrarenderer.dep.terraplusplus.http.Disk;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;

@UtilityClass
@Mixin(value = Disk.class, remap = false)
public class HttpDiskMixin {
    /**
     * @author m4ndeokyi
     * @reason mixin overwrite
     */
    @Overwrite
    public File getMinecraftRoot() {
        File mcRoot;
        try {
            TerraConfig.LOGGER.info("Detected Minecraft root dir: {}", FabricLoader.getInstance().getGameDir());
            mcRoot = FabricLoader.getInstance().getGameDir().toFile();
        } catch (
                NullPointerException e) { //an NPE probably means we're running in a test environment, and FML isn't initialized
            if (!PFiles.checkDirectoryExists(mcRoot = new File("run"))) {
                mcRoot = new File(".");
            }
        }
        return mcRoot;
    }
}
