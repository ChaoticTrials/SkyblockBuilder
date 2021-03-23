package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class MineMentionCompat {

    public static void updateMentions(MinecraftServer server) {
        if (ModList.get().isLoaded("minemention")) {
            SpecialMentions.notifyAvailabilityChange(server);
        }
    }

    public static void register() {
        if (ModList.get().isLoaded("minemention")) {
            SpecialMentions.registerMention(new ResourceLocation(SkyblockBuilder.MODID, "team"), "team", TeamMention.INSTANCE);
        }
    }
}
