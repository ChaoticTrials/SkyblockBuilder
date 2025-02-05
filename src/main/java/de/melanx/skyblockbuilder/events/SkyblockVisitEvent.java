package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;

/**
 * SkyblockJoinRequestEvent is fired whenever a player visits another team.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 */
public class SkyblockVisitEvent extends Event {

    private final ServerPlayer player;
    private final Team team;
    private Result result = Result.DEFAULT;

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

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return this.result;
    }

    public static enum Result {
        /**
         * Skip default checks
         */
        ALLOW,

        /**
         * Proceed with the default checks
         */
        DEFAULT,

        /**
         * Stop process instantly
         */
        DENY
    }
}
