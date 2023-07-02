package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "skyblockbuilder", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventListener {

    @SubscribeEvent
    public static void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        event.register(ResourceKey.create(Registries.WORLD_PRESET, SkyblockBuilder.getInstance().resource("skyblock")), ScreenCustomizeSkyblock::new);
    }
}
