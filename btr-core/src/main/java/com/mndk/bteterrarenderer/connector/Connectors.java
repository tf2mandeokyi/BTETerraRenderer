package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.minecraft.*;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.GraphicsManager;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiStaticConnector;

public class Connectors {

    public static I18nConnector I18N;
    public static GuiStaticConnector GUI;
    public static GraphicsManager GRAPHICS;
    public static SoundHandler SOUND;
    public static MouseConnector MOUSE;
    public static GameSettingsConnector GAME_SETTINGS;
    public static ErrorMessageHandler ERROR_HANDLER;
    public static PlayerConnector PLAYER;

    public static ConnectorSupplier SUPPLIER;
}
