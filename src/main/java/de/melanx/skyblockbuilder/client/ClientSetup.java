package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeWorldPresetScreens;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

    public static void clientSetup() {
        ForgeWorldPresetScreens.registerPresetEditor(Registration.skyblock, (parent, settings) -> new ScreenCustomizeSkyblock(parent));
    }
}
