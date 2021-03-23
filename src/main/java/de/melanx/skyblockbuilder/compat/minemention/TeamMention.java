package de.melanx.skyblockbuilder.compat.minemention;

import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class TeamMention implements SpecialMention {

    public static final TeamMention INSTANCE = new TeamMention();

    @Override
    public IFormattableTextComponent description() {
        return new TranslationTextComponent("minemention.skyblockbuilder.team");
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        SkyblockSavedData data = SkyblockSavedData.get(sender.getServerWorld());
        Team team = data.getTeamFromPlayer(sender);

        if (team == null) return player -> false;

        return team::hasPlayer;
    }

    @Override
    public boolean available(ServerPlayerEntity sender) {
        return SkyblockSavedData.get(sender.getServerWorld()).hasPlayerTeam(sender);
    }
}
