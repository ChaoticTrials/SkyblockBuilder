package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class SkyblockJoinRequestEvent extends Event {

    private final ServerPlayerEntity requestingPlayer;
    private final Team team;

    private SkyblockJoinRequestEvent(ServerPlayerEntity requestingPlayer, Team team) {
        this.requestingPlayer = requestingPlayer;
        this.team = team;
    }

    public ServerPlayerEntity getRequestingPlayer() {
        return this.requestingPlayer;
    }

    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    public static class SendRequest extends SkyblockJoinRequestEvent {

        public SendRequest(ServerPlayerEntity requestingPlayer, Team team) {
            super(requestingPlayer, team);
        }
    }

    public static class AcceptRequest extends SkyblockJoinRequestEvent {

        private final ServerPlayerEntity acceptingPlayer;

        public AcceptRequest(ServerPlayerEntity acceptingPlayer, ServerPlayerEntity requestingPlayer, Team team) {
            super(requestingPlayer, team);
            this.acceptingPlayer = acceptingPlayer;
        }

        public ServerPlayerEntity getAcceptingPlayer() {
            return this.acceptingPlayer;
        }
    }

    public static class DenyRequest extends SkyblockJoinRequestEvent {

        private final ServerPlayerEntity denyingPlayer;

        public DenyRequest(ServerPlayerEntity denyingPlayer, ServerPlayerEntity requestingPlayer, Team team) {
            super(requestingPlayer, team);
            this.denyingPlayer = denyingPlayer;
        }

        public ServerPlayerEntity getDenyingPlayer() {
            return this.denyingPlayer;
        }
    }
}
