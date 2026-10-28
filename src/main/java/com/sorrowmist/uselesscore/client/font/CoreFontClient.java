package com.sorrowmist.uselesscore.client.font;

import com.sorrowmist.uselesscore.UselessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=UselessCore.MOD_ID,value=Dist.CLIENT)
public final class CoreFontClient {
    @SubscribeEvent public static void titleScreen(ScreenEvent.Init.Post event) {
        if(!(event.getScreen() instanceof TitleScreen screen))return;
        event.addListener(Button.builder(Component.translatable("gui.useless_core.font.title"),
                button->Minecraft.getInstance().setScreen(new CoreFontScreen(screen)))
                .bounds(Math.max(4,screen.width-108),8,100,20).build());
    }
}
