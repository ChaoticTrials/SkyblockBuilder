package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "skyblockbuilder", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventListener {

    @SubscribeEvent
    public static void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        event.register(Registration.skyblockKey, ScreenCustomizeSkyblock::new);
    }
}
