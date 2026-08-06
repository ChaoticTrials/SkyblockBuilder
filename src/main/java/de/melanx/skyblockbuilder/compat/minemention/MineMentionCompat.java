package de.melanx.skyblockbuilder.compat.minemention;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.server.level.ServerPlayer;

public class MineMentionCompat {

    public static void updateMentions(ServerPlayer player) {
        SpecialMentions.notifyAvailabilityChange(player);
    }

    public static void register() {
//        SpecialMentions.registerMention(Identifier.fromNamespaceAndPath(SkyblockBuilder.getInstance().modid, "sky_team"), "sky_team", TeamMention.INSTANCE); todo MineMention
    }
}
