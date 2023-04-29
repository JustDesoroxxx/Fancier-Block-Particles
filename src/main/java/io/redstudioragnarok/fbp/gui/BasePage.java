package io.redstudioragnarok.fbp.gui;

import com.google.common.collect.Lists;
import io.redstudioragnarok.fbp.FBP;
import io.redstudioragnarok.fbp.handlers.ConfigHandler;
import io.redstudioragnarok.fbp.utils.MathUtil;
import io.redstudioragnarok.fbp.utils.ModReference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Desktop;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.redstudioragnarok.fbp.gui.Button.ButtonSize.*;

public abstract class BasePage extends GuiScreen {

    protected static int mouseX;
    protected static int mouseY;
    protected static int x;
    protected static int y;

    private long lastTime;

    private float hoverBoxY;
    private float targetHoverBoxY;

    protected boolean writeConfig;
    protected boolean isSettings;

    protected GuiButtonEnable toggle;

    protected ButtonBugReport issue, settings;

    private GuiScreen previousPage, nextPage;

    public void initPage(GuiScreen previousPage, GuiScreen nextPage) {
        x = width / 2 - 100;
        y = height / 5 + 148;

        addButton(0, x, y, medium, I18n.format("menu.defaults"));
        addButton(-1, x + 102, y, medium, I18n.format("menu.reloadConfig"));

        addButton(-2, x, y + 22, large, I18n.format("menu.done"));

        if (!isSettings)
            settings = new ButtonBugReport(-3, width - 32, 6);

        issue = new ButtonBugReport(-4, width - (isSettings ? 32 : 64), 6);
        toggle = new GuiButtonEnable(-5, width - (isSettings ? 64 : 96), 6);

        super.buttonList.addAll(Arrays.asList(toggle, issue, settings));

        this.previousPage = previousPage;
        this.nextPage = nextPage;

        if (previousPage!= null)
            addButton(-6, x - 45, y - 50, small, "§6<<");

        if (nextPage!= null)
            addButton(-7, x + 225, y - 50, small, "§6>>");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiYesNo(this));
                break;
            case -1:
                ConfigHandler.init();
                break;
            case -2:
                mc.displayGuiScreen(null);
                break;
            case -3:
                break;
            case -4:
                try {
                    Desktop.getDesktop().browse(ModReference.newIssueLink);
                } catch (Exception exception) {
                    // TODO: (Debug Mode) This should count to the problem counter and should output a stack trace
                }
                break;
            case -5:
                FBP.setEnabled(!FBP.enabled);
                writeConfig = true;
                break;
            case -6:
                mc.displayGuiScreen(previousPage);
                break;
            case -7:
                mc.displayGuiScreen(nextPage);
                break;
        }

        onActionPerformed(button);
    }

    protected void onActionPerformed(GuiButton button) {
    }

    @Override
    public void updateScreen() {
        buttonList.forEach(button -> {
            if (button instanceof InteractiveElement)
                ((InteractiveElement) button).update(mouseX, mouseY);

            if (button instanceof Slider) {
                Slider slider = (Slider) button;

                if (slider.isMouseOver())
                    targetHoverBoxY = slider.y;

                if (!((MathUtil.round(slider.originalValue, 2))  == (MathUtil.round(slider.value, 2))))
                    writeConfig = true;
            }
        });
    }

    protected void updateSliderHoverBox() {
        float step = 0.5F;
        long time = System.currentTimeMillis();

        if (lastTime > 0)
            step = (time - lastTime) / 3F;

        lastTime = time;

        if (hoverBoxY > targetHoverBoxY) {
            if (hoverBoxY - targetHoverBoxY <= step)
                hoverBoxY = targetHoverBoxY;
            else
                hoverBoxY -= step;
        }

        if (hoverBoxY < targetHoverBoxY) {
            if (targetHoverBoxY - hoverBoxY <= step)
                hoverBoxY = targetHoverBoxY;
            else
                hoverBoxY += step;
        }
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        mouseX = mouseXIn;
        mouseY = mouseYIn;

        if (FBP.mc.world != null)
            drawRectangle(0, 0, width, height, 0, 0, 0, 191);
        else
            drawBackground(0);

        if (!FBP.enabled)
            drawCenteredString(fontRenderer, "§L= " + I18n.format("menu.disabled") + " =", x + 100, y - 193, fontRenderer.getColorCode('c'));

        drawCenteredString(fontRenderer, "§L= " + I18n.format("name") + " =", x + 100, y - 183, fontRenderer.getColorCode('6'));
        drawCenteredString(fontRenderer, "§L= " + ModReference.version + " =", x + 100, y - 173, fontRenderer.getColorCode('a'));

        drawTitle();

        if (targetHoverBoxY > 0)
            updateSliderHoverBox();

        for (GuiButton button : super.buttonList) {
            if (button.id <= 0)
                continue;

            Slider slider = button instanceof Slider ? (Slider) button : new Slider();

            if (slider.isMouseOver())
                drawRectangle(x - 2, hoverBoxY + 2, 204, 16, 200, 200, 200, 35);

            if (button.isMouseOver() || slider.isMouseOver()) {
                drawCenteredString(fontRenderer, getDescription(), this.width / 2, height / 5 + 131, fontRenderer.getColorCode('f'));
                break;
            }
        }

        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
    }

    protected void drawTitle() {
    }

    protected abstract String getDescription();

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (GuiButton guibutton : super.buttonList) {
                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    if (!guibutton.isMouseOver())
                        return;

                    actionPerformed(guibutton);
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        final int scrollAmount = Mouse.getEventDWheel();

        if (scrollAmount != 0) {
            if (scrollAmount > 0) {
                if (previousPage != null)
                    mc.displayGuiScreen(previousPage);
            } else if (nextPage != null)
                mc.displayGuiScreen(nextPage);
        }
    }

    @Override
    public void onGuiClosed() {
        if (writeConfig)
            ConfigHandler.writeMainConfig();
    }

    protected Button addButton(final int id, final int x, final int y, final Button.ButtonSize size, final String text) {
        Button button = new Button(id, x, y, size, text, false, false, true);
        buttonList.add(button);

        return button;
    }

    protected void addButton(final int id, final String text, final Boolean toggle, final Boolean toggleButton, final Boolean... disabled) {
        buttonList.add(new Button(id, x, calculatePosition(id), large, text, toggle, toggleButton, disabled.length < 1));
    }

    protected Slider addSlider(final int id, final float minValue, final float inputValue, final float maxValue, final Boolean... disabled) {
        Slider slider = new Slider(id, x, calculatePosition(id), minValue, inputValue, maxValue, disabled.length < 1);
        buttonList.add(slider);

        if (hoverBoxY == 0) {
            hoverBoxY = slider.y;
            targetHoverBoxY = slider.y;
        }

        return slider;
    }

    private int calculatePosition(final int id) {
        final int evenButtonSpacing = 26;
        final int oddButtonSpacing = 21;

        int totalSpacing = 0;

        for (int i = 1; i < id; i++) {
            totalSpacing += (i % 2 == 0) ? evenButtonSpacing : oddButtonSpacing;
        }

        return this.height / 5 - 6 + totalSpacing;
    }

    protected static void drawRectangle(final double x, final double y, final double x2, final double y2, final int red, final int green, final int blue, final int alpha) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x, y + y2, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + x2, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + x2, y + y2, 0).color(red, green, blue, alpha).endVertex();

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}
