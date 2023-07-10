package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.ModBlocks;
import de.melanx.skyblockbuilder.ModItems;
import de.melanx.skyblockbuilder.Registration;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "skyblockbuilder", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventListener {

    @SubscribeEvent
    public static void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        event.register(Registration.skyblockKey, ScreenCustomizeSkyblock::new);
    }

    @SubscribeEvent
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(() -> ModItems.structureSaver);
            event.accept(() -> ModBlocks.spawnBlock);
        }
    }
}
