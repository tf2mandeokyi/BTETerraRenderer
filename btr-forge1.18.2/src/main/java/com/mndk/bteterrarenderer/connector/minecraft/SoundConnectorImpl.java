package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

@ConnectorImpl
@SuppressWarnings("unused")
public class SoundConnectorImpl implements SoundConnector {
    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
