package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * SkyblockJoinRequestEvent is fired whenever a join request is handled.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 */
public class SkyblockJoinRequestEvent extends Event {

    private final ServerPlayer requestingPlayer;
    private final Team team;
    private Result result = Result.DEFAULT;

    /**
     * @param requestingPlayer The player who requests joining
     * @param team             The team the player wants to join to
     */
    private SkyblockJoinRequestEvent(ServerPlayer requestingPlayer, Team team) {
        this.requestingPlayer = requestingPlayer;
        this.team = team;
    }

    /**
     * @return Requesting player
     */
    public ServerPlayer getRequestingPlayer() {
        return this.requestingPlayer;
    }

    /**
     * @return Team who gets the request
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

    /**
     * This event is fired when the {@link #requestingPlayer} sends the join request.
     */
    public static class SendRequest extends SkyblockJoinRequestEvent {

        public SendRequest(ServerPlayer requestingPlayer, Team team) {
            super(requestingPlayer, team);
        }
    }

    /**
     * This event is fired when the {@link #acceptingPlayer} accepts the request.
     */
    public static class AcceptRequest extends SkyblockJoinRequestEvent {

        private final ServerPlayer acceptingPlayer;

        public AcceptRequest(ServerPlayer acceptingPlayer, ServerPlayer requestingPlayer, Team team) {
            super(requestingPlayer, team);
            this.acceptingPlayer = acceptingPlayer;
        }

        /**
         * @return Player who accepts the request
         */
        public ServerPlayer getAcceptingPlayer() {
            return this.acceptingPlayer;
        }
    }

    /**
     * This event is fired when the {@link #denyingPlayer} denies the request.
     */
    public static class DenyRequest extends SkyblockJoinRequestEvent {

        private final ServerPlayer denyingPlayer;

        public DenyRequest(ServerPlayer denyingPlayer, ServerPlayer requestingPlayer, Team team) {
            super(requestingPlayer, team);
            this.denyingPlayer = denyingPlayer;
        }

        /**
         * @return Player who denies the request
         */
        public ServerPlayer getDenyingPlayer() {
            return this.denyingPlayer;
        }
    }
}
