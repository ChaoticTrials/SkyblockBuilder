package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MineMentionCompat {

    public static void updateMentions(ServerPlayer player) {
        SpecialMentions.notifyAvailabilityChange(player);
    }

    public static void register() {
        SpecialMentions.registerMention(new ResourceLocation(SkyblockBuilder.getInstance().modid, "sky_team"), "sky_team", TeamMention.INSTANCE);
    }
}
