package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * SkyblockJoinRequestEvent is fired whenever a join request is handled.<br>
 * <br>
 * All children of this event does have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class SkyblockJoinRequestEvent extends Event {

    private final ServerPlayer requestingPlayer;
    private final Team team;

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

    @Override
    public boolean hasResult() {
        return true;
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
