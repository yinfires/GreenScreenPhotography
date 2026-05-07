package com.yinfires.greenscreen.registry;

import com.yinfires.greenscreen.GreenScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GreenScreen.MOD_ID);

    public static final Supplier<CreativeModeTab> GREEN_SCREEN_TAB = CREATIVE_MODE_TABS.register("green_screen", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + GreenScreen.MOD_ID + ".green_screen"))
            .icon(() -> new ItemStack(ModBlocks.GREEN_SCREEN_BLOCK.get()))
            .displayItems((params, output) -> output.accept(ModBlocks.GREEN_SCREEN_BLOCK.get()))
            .build());

    private ModCreativeTabs() {
    }
}
