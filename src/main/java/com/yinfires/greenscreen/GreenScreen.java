package com.yinfires.greenscreen;

import com.mojang.logging.LogUtils;
import com.yinfires.greenscreen.registry.ModBlockEntities;
import com.yinfires.greenscreen.registry.ModBlocks;
import com.yinfires.greenscreen.registry.ModCreativeTabs;
import com.yinfires.greenscreen.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(GreenScreen.MOD_ID)
public class GreenScreen {
    public static final String MOD_ID = "greenscreen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GreenScreen(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Green Screen initialized.");
    }
}
