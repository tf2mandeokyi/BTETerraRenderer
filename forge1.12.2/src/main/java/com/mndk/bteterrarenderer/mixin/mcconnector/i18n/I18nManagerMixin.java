package com.mndk.bteterrarenderer.mixin.mcconnector.i18n;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = I18nManager.class, remap = false)
public class I18nManagerMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String getCurrentLanguage() {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    @SuppressWarnings("OverwriteModifiers")
    public String format(String key, Object... parameters) {
        return I18n.format(key);
    }
}
