package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AbstractGuiScreenImpl extends GuiScreen {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

    public final AbstractGuiScreenCopy delegate;
    private URI clickedLinkURI;
    private double pMouseX = 0, pMouseY = 0;

    public AbstractGuiScreenImpl(@Nonnull AbstractGuiScreenCopy delegate) {
        this.delegate = delegate;
    }

    public void initGui() {
        delegate.initGui(this.width, this.height);
    }
    public void onResize(@Nonnull Minecraft client, int width, int height) {
        super.onResize(client, width, height);
        delegate.setScreenSize(width, height);
    }
    public void updateScreen() {
        delegate.tick();
    }
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(GuiDrawContextWrapperImpl.INSTANCE, mouseX, mouseY, partialTicks);
    }
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Skip if the "pressed" mouse button is either scroll up or scroll down
        if (mouseButton == 63 || mouseButton == 64) return;

        this.pMouseX = mouseX; this.pMouseY = mouseY;
        super.mouseClicked(mouseX, mouseY, mouseButton);
        delegate.mousePressed(mouseX, mouseY, mouseButton);
    }
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        delegate.mouseReleased(mouseX, mouseY, state);
    }
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        delegate.mouseScrolled(mouseX, mouseY, Mouse.getEventDWheel());
    }
    public void keyTyped(char key, int keyCode) throws IOException {
        if (delegate.shouldCloseOnEsc()) {
            super.keyTyped(key, keyCode);
        }
        delegate.charTyped(key, keyCode);
        delegate.keyPressed(InputKey.fromKeyboardCode(keyCode), 0, 0);
    }
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        delegate.mouseDragged(mouseX, mouseY, clickedMouseButton, pMouseX, pMouseY);
        this.pMouseX = mouseX; this.pMouseY = mouseY;
    }

    public void onGuiClosed() {
        delegate.onRemoved();
    }
    public boolean doesGuiPauseGame() {
        return delegate.doesScreenPauseGame();
    }

    public boolean handleStyleClick(@Nonnull Style style) {
        ClickEvent clickevent = style.getClickEvent();

        if (isShiftKeyDown()) {
            if (style.getInsertion() != null) {
                this.setText(style.getInsertion(), false);
            }
            return false;
        }

        if (clickevent == null) return false;

        ClickEvent.Action action = clickevent.getAction();
        switch (action) {
            case OPEN_URL:
                if (!this.mc.gameSettings.chatLinks) return false;

                try {
                    URI uri = new URI(clickevent.getValue());
                    String s = uri.getScheme();

                    if (s == null) {
                        throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                    }

                    if (!PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
                        throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
                    }

                    if (this.mc.gameSettings.chatLinksPrompt) {
                        this.clickedLinkURI = uri;
                        this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 31102009, false));
                    } else {
                        this.openWebLink(uri);
                    }
                } catch (URISyntaxException urisyntaxexception) {
                    Loggers.get(this).error("Can't open url for {}", clickevent, urisyntaxexception);
                }
                break;

            case OPEN_FILE:
                URI uri1 = (new File(clickevent.getValue())).toURI();
                this.openWebLink(uri1);
                break;

            case SUGGEST_COMMAND:
                this.setText(clickevent.getValue(), true);
                break;

            case RUN_COMMAND:
                this.sendChatMessage(clickevent.getValue(), false);
                break;

            default:
                Loggers.get(this).error("Don't know how to handle {}", clickevent);
                break;
        }

        return true;
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        if (id != 31102009) return;

        if (result) this.openWebLink(this.clickedLinkURI);
        this.clickedLinkURI = null;
        this.mc.displayGuiScreen(this);
    }

    private void openWebLink(URI url) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke(null);
            oclass.getMethod("browse", URI.class).invoke(object, url);
        } catch (Throwable throwable1) {
            Throwable throwable = throwable1.getCause();
            Loggers.get(this).error("Couldn't open link: {}", throwable == null ? "<UNKNOWN>" : throwable.getMessage());
        }
    }

    public void handleStyleHover(@Nonnull Style style, int x, int y) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) return;

        switch (hoverEvent.getAction()) {
            case SHOW_ITEM:
                ItemStack itemstack = ItemStack.EMPTY;

                try {
                    NBTTagCompound nbtbase = JsonToNBT.getTagFromJson(hoverEvent.getValue().getUnformattedText());

                    if (nbtbase instanceof NBTTagCompound) {
                        itemstack = new ItemStack(nbtbase);
                    }
                } catch (NBTException ignored) {}

                if (itemstack.isEmpty()) {
                    this.drawHoveringText(TextFormatting.RED + "Invalid Item!", x, y);
                } else {
                    this.renderToolTip(itemstack, x, y);
                }
                break;

            case SHOW_ENTITY:
                if (!this.mc.gameSettings.advancedItemTooltips) break;
                try {
                    NBTTagCompound nbttagcompound = JsonToNBT.getTagFromJson(hoverEvent.getValue().getUnformattedText());
                    List<String> list = Lists.newArrayList();
                    list.add(nbttagcompound.getString("name"));

                    if (nbttagcompound.hasKey("type", 8)) {
                        String s = nbttagcompound.getString("type");
                        list.add("Type: " + s);
                    }

                    list.add(nbttagcompound.getString("id"));
                    this.drawHoveringText(list, x, y);
                } catch (NBTException var8) {
                    this.drawHoveringText(TextFormatting.RED + "Invalid Entity!", x, y);
                }
                break;

            case SHOW_TEXT:
                this.drawHoveringText(this.mc.fontRenderer.listFormattedStringToWidth(hoverEvent.getValue().getFormattedText(), Math.max(this.width / 2, 200)), x, y);
                break;
        }

        GlStateManager.disableLighting();
    }
}
