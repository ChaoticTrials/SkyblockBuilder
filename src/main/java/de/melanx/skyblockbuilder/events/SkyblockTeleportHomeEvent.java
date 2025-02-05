package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event fires when a player tries to teleport home.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class SkyblockTeleportHomeEvent extends Event {

    private final ServerPlayer player;
    private final Team team;
    private Result result = Result.DEFAULT;

    public SkyblockTeleportHomeEvent(ServerPlayer player, Team team) {
        this.player = player;
        this.team = team;
    }

    /**
     * @return Teleporting player
     */
    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * @return Team from the player
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
