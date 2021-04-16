package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class MineMentionCompat {

    public static void updateMentions(ServerPlayerEntity player) {
        SpecialMentions.notifyAvailabilityChange(player);
    }

    public static void register() {
        SpecialMentions.registerMention(new ResourceLocation(SkyblockBuilder.getInstance().modid, "team"), "team", TeamMention.INSTANCE);
    }
}
