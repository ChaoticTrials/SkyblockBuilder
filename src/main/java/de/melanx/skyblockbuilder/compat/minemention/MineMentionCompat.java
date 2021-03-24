package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class MineMentionCompat {

    public static void updateMentions(ServerPlayerEntity player) {
        if (ModList.get().isLoaded("minemention")) {
            SpecialMentions.notifyAvailabilityChange(player);
        }
    }

    public static void register() {
        if (ModList.get().isLoaded("minemention")) {
            SpecialMentions.registerMention(new ResourceLocation(SkyblockBuilder.MODID, "team"), "team", TeamMention.INSTANCE);
        }
    }
}
