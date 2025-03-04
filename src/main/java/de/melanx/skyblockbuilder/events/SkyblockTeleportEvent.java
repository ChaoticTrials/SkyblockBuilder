package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event fires when a player tries to teleport home, to spawn, or visit another island.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public abstract class SkyblockTeleportEvent extends Event {

    private final ServerPlayer player;
    private final Team team;
    private Result result = Result.DEFAULT;

    protected SkyblockTeleportEvent(ServerPlayer player, Team team) {
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

    public enum Result {
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

    /**
     * This event fires when a player tries to teleport home.
     */
    public static class Home extends SkyblockTeleportEvent {

        public Home(ServerPlayer player, Team team) {
            super(player, team);
        }
    }

    /**
     * This event fires when a player tries to teleport to spawn.
     */
    public static class Spawn extends SkyblockTeleportEvent {

        public Spawn(ServerPlayer player, Team team) {
            super(player, team);
        }
    }

    /**
     * This event fires when a player tries to teleport for visiting another island.
     */
    public static class Visit extends SkyblockTeleportEvent {

        public Visit(ServerPlayer player, Team team) {
            super(player, team);
        }
    }
}
