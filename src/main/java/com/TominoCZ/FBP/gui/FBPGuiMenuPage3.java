package com.TominoCZ.FBP.gui;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.util.ModReference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.awt.*;

public class FBPGuiMenuPage3 extends GuiScreen {

	GuiButton b1, b2, b3, b4, b5, b6, Defaults, Done, Reload, Back, Next, Enable, ReportBug;

	String description;

	final int GUIOffsetY = 4;

	@Override
	public void initGui() {
		int x = this.width / 2 - (96 * 2 + 8) / 2;

		b1 = new FBPGuiButton(1, x, (this.height / 5) - 10 + GUIOffsetY, I18n.format("menu.fancyflame.info"), FBP.fancyFlame, true, true);
		b2 = new FBPGuiButton(2, x, b1.y + b1.height + 1, I18n.format("menu.fancysmoke.info"), FBP.fancySmoke, true, true);
		b3 = new FBPGuiButton(3, x, b2.y + b2.height + 6, I18n.format("menu.fancyrain.info"), FBP.fancyRain, true, true);
		b4 = new FBPGuiButton(4, x, b3.y + b3.height + 1, I18n.format("menu.fancysnow.info"), FBP.fancySnow, true, true);
		b5 = new FBPGuiButton(5, x, b4.y + b4.height + 6, I18n.format("menu.waterphysics.info"), FBP.waterPhysics, true, true);
		b6 = new FBPGuiButton(6, x, b5.y + b1.height + 1, I18n.format("menu.restonfloor.info"), FBP.restOnFloor, true, true);

		Defaults = new FBPGuiButton(0, this.width / 2 + 2, b6.y + b6.height + 24 - GUIOffsetY, I18n.format("menu.defaults"), false, false, true);
		Done = new FBPGuiButton(-1, this.width / 2 - 100, Defaults.y, I18n.format("menu.done"), false, false, true);
		Defaults.width = Done.width = 98;
		Reload = new FBPGuiButton(-2, this.width / 2 - 100, Defaults.y + Defaults.height + 1, I18n.format("menu.reloadconfig"), false, false, true);
		Reload.width = b1.width = b2.width = b3.width = b4.width = b5.width = b6.width = 200;

		Back = new FBPGuiButton(-3, b6.x - 44, b6.y + 2 - GUIOffsetY, "<<", false, false, true);
		Next = new FBPGuiButton(-6, b6.x + b6.width + 25, b6.y + 2 - GUIOffsetY, ">>", false, false, true);
		Back.width = Next.width = 20;

		Enable = new FBPGuiButtonEnable(-5, (this.width - 25 - 27) - 4, 2, new Dimension(width, height), this.fontRenderer);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height), this.fontRenderer);

		this.buttonList.addAll(java.util.Arrays.asList(b1, b2, b3, b4, b5, b6, Defaults, Done, Reload, Back, Next, Enable, ReportBug));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case -6:
			this.mc.displayGuiScreen(new FBPGuiMenuPage4());
			break;
		case -5:
			FBP.setEnabled(!FBP.enabled);
			break;
		case -4:
			try {
				Desktop.getDesktop().browse(ModReference.ISSUE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case -3:
			this.mc.displayGuiScreen(new FBPGuiMenuPage2());
			break;
		case -2:
			FBPConfigHandler.init();
			break;
		case -1:
			this.mc.displayGuiScreen(null);
			break;
		case 0:
			this.mc.displayGuiScreen(new FBPGuiYesNo(this));
			break;
		case 1:
			FBP.fancyFlame = !FBP.fancyFlame;
			break;
		case 2:
			FBP.fancySmoke = !FBP.fancySmoke;
			break;
		case 3:
			FBP.fancyRain = !FBP.fancyRain;
			break;
		case 4:
			FBP.fancySnow = !FBP.fancySnow;
			break;
		case 5:
			FBP.waterPhysics = !FBP.waterPhysics;
			break;
		case 6:
			FBP.restOnFloor = !FBP.restOnFloor;
			break;
		}

		if (FBP.fancyRain || FBP.fancySnow)
			mc.world.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
		else
			mc.world.provider.setWeatherRenderer(FBP.originalWeatherRenderer);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		FBPGuiHelper.background(b1.y - 6 - GUIOffsetY, Done.y - 4, width, height);

		int posY = Done.y - 18;

		getDescription();

		if ((mouseX >= b1.x && mouseX < b1.x + b1.width) && (mouseY >= b1.y && mouseY < b6.y + b1.height)) {
			this.drawCenteredString(fontRenderer, description, this.width / 2, posY, fontRenderer.getColorCode('f'));
		}

		FBPGuiHelper.drawTitle(b1.y - GUIOffsetY, width, fontRenderer);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void getDescription() {
		for (GuiButton b : this.buttonList) {
			if (b.isMouseOver()) {
				switch (b.id) {
				case 1:
					description = I18n.format("menu.fancyflame.description");
					break;
				case 2:
					description = I18n.format("menu.fancysmoke.description");
					break;
				case 3:
					description = I18n.format("menu.fancyrain.description");
					break;
				case 4:
					description = I18n.format("menu.fancysnow.description");
					break;
				case 5:
					description = I18n.format("menu.waterphysics.description");
					break;
				case 6:
					description = I18n.format("menu.restonfloor.description");
					break;
				default:
					description = "No description available please report this";
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			for (GuiButton guibutton : this.buttonList) {
				if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
					if (!guibutton.isMouseOver())
						return;

					this.actionPerformed(guibutton);
				}
			}
		}
	}

	@Override
	public void onGuiClosed() {
		FBPConfigHandler.write();
	}
}
