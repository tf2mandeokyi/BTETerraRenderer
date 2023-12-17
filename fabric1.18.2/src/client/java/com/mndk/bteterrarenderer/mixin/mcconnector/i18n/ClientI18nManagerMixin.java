package com.mndk.bteterrarenderer.mixin.mcconnector.i18n;

import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = I18nManager.class, remap = false)
public class ClientI18nManagerMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String getCurrentLanguage() {
        return MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
    }
}
