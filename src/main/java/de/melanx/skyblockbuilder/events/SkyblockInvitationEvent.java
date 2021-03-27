package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

/**
 * SkyblockInvitationEvent is fired whenever an invitation is handled.<br>
 * <br>
 * All children of this event do have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class SkyblockInvitationEvent extends Event {

    private final ServerPlayerEntity invitedPlayer;
    private final Team team;

    /**
     * @param invitedPlayer The player who is invited
     * @param team          The team the player is invited to
     */
    private SkyblockInvitationEvent(ServerPlayerEntity invitedPlayer, Team team) {
        this.invitedPlayer = invitedPlayer;
        this.team = team;
    }

    /**
     * @return Invited player
     */
    public ServerPlayerEntity getInvitedPlayer() {
        return this.invitedPlayer;
    }

    /**
     * @return Inviting team
     */
    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    /**
     * This event is fired when an {@link #invitor} invites another player to its team.
     */
    public static class Invite extends SkyblockInvitationEvent {

        private final ServerPlayerEntity invitor;

        public Invite(ServerPlayerEntity invitedPlayer, Team team, @Nonnull ServerPlayerEntity invitor) {
            super(invitedPlayer, team);
            this.invitor = invitor;
        }

        /**
         * @return Inviting player
         */
        @Nonnull
        public ServerPlayerEntity getInvitor() {
            return this.invitor;
        }
    }

    /**
     * This event is fired before the invitation is accepted.
     */
    public static class Accept extends SkyblockInvitationEvent {

        public Accept(ServerPlayerEntity invitedPlayer, Team team) {
            super(invitedPlayer, team);
        }
    }

    /**
     * This event is fired before the invitation is declined.
     */
    public static class Decline extends SkyblockInvitationEvent {

        public Decline(ServerPlayerEntity invitedPlayer, Team team) {
            super(invitedPlayer, team);
        }
    }
}
