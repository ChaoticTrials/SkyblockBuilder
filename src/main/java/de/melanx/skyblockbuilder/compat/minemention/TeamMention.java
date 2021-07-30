package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class TeamMention implements SpecialMention {

    public static final TeamMention INSTANCE = new TeamMention();

    @Override
    public MutableComponent description() {
        return new TranslatableComponent("minemention.skyblockbuilder.team");
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        SkyblockSavedData data = SkyblockSavedData.get(sender.getLevel());
        Team team = data.getTeamFromPlayer(sender);

        if (team == null) return player -> false;

        return team::hasPlayer;
    }

    @Override
    public boolean available(ServerPlayer sender) {
        return SkyblockSavedData.get(sender.getLevel()).hasPlayerTeam(sender);
    }
}
