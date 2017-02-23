package com.mattsmeets.macrokey.gui.config;

import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

/**
 * Created by Matt on 4/4/2016.
 */
public class GuiConfigMod extends GuiConfig {
    public GuiConfigMod(GuiScreen parent) {
        super(parent,
                new ConfigElement(
                        MacroKey.instance.configuration.getCategory(Configuration.CATEGORY_GENERAL))
                        .getChildElements(),
                Reference.MOD_ID,
                false,
                false,
                "Config Options for MacroKey");
        titleLine2 = MacroKey.instance.configuration.getConfigFile().getAbsolutePath();
    }

    @Override
    public void initGui() {
        super.initGui();
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }
}
