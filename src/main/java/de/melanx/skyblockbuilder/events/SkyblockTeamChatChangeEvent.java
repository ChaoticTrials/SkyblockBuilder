package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.util.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class SkyblockTeamChatChangeEvent extends Event {

    private final ServerPlayerEntity player;
    private final Team team;
    private final boolean teamChat;

    public SkyblockTeamChatChangeEvent(ServerPlayerEntity player, Team team, boolean teamChat) {
        this.player = player;
        this.team = team;
        this.teamChat = teamChat;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    public boolean isTeamChat() {
        return this.teamChat;
    }
}
