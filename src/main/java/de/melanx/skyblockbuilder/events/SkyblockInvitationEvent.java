package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;

/**
 * SkyblockInvitationEvent is fired whenever an invitation is handled.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 */
public abstract class SkyblockInvitationEvent extends Event {

    private final ServerPlayer invitedPlayer;
    private final Team team;
    private Result result = Result.DEFAULT;

    /**
     * @param invitedPlayer The player who is invited
     * @param team          The team the player is invited to
     */
    private SkyblockInvitationEvent(ServerPlayer invitedPlayer, Team team) {
        this.invitedPlayer = invitedPlayer;
        this.team = team;
    }

    /**
     * @return Invited player
     */
    public ServerPlayer getInvitedPlayer() {
        return this.invitedPlayer;
    }

    /**
     * @return Inviting team
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
     * This event is fired when an {@link #invitor} invites another player to its team.
     */
    public static class Invite extends SkyblockInvitationEvent {

        private final ServerPlayer invitor;

        public Invite(ServerPlayer invitedPlayer, Team team, @Nonnull ServerPlayer invitor) {
            super(invitedPlayer, team);
            this.invitor = invitor;
        }

        /**
         * @return Inviting player
         */
        @Nonnull
        public ServerPlayer getInvitor() {
            return this.invitor;
        }
    }

    /**
     * This event is fired before the invitation is accepted.
     */
    public static class Accept extends SkyblockInvitationEvent {

        public Accept(ServerPlayer invitedPlayer, Team team) {
            super(invitedPlayer, team);
        }
    }

    /**
     * This event is fired before the invitation is declined.
     */
    public static class Decline extends SkyblockInvitationEvent {

        public Decline(ServerPlayer invitedPlayer, Team team) {
            super(invitedPlayer, team);
        }
    }
}
