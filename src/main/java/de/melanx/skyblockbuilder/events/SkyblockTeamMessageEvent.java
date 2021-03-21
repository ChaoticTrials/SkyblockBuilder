package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.util.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;

// Canceling this will silently drop the message
public class SkyblockTeamMessageEvent extends Event {

    private final ServerPlayerEntity player;
    private final Team team;
    private ITextComponent message;

    public SkyblockTeamMessageEvent(ServerPlayerEntity player, Team team, ITextComponent message) {
        this.player = player;
        this.team = team;
        this.message = message;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    public ITextComponent getMessage() {
        return this.message;
    }

    public SkyblockTeamMessageEvent setMessage(ITextComponent message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
