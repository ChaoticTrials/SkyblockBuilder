package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

/**
 * SkyblockJoinRequestEvent is fired whenever a player visits another team.<br>
 * <br>
 * All children of this event does have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class SkyblockVisitEvent extends Event {

    private final ServerPlayer player;
    private final Team team;

    public SkyblockVisitEvent(@Nonnull ServerPlayer player, Team team) {
        this.player = player;
        this.team = team;
    }

    /**
     * @return Visiting player
     */
    @Nonnull
    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * @return Visited team
     */
    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }
}
