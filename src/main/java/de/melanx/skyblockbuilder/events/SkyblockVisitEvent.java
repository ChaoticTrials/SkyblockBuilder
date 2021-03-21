package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.util.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class SkyblockVisitEvent extends Event {

    private final ServerPlayerEntity player;
    private final Team team;

    public SkyblockVisitEvent(ServerPlayerEntity player, Team team) {
        this.player = player;
        this.team = team;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }
}
