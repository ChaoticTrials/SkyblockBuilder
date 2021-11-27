package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * This fires whenever something is managed in the op manage command.<br>
 * <br>
 * All children of this event does not have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}.<br>
 * <br>
 * This event is {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class SkyblockOpManageEvent extends Event {

    private final CommandSourceStack source;

    private SkyblockOpManageEvent(CommandSourceStack source) {
        this.source = source;
    }

    /**
     * @return {@link CommandSourceStack}
     */
    @Nullable
    public CommandSourceStack getSource() {
        return this.source;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    /**
     * This event fires when a team is deleted
     */
    public static class DeleteTeam extends SkyblockOpManageEvent {

        private final Team team;

        public DeleteTeam(CommandSourceStack source, Team team) {
            super(source);
            this.team = team;
        }

        /**
         * @return Deleted team
         */
        public Team getTeam() {
            return this.team;
        }
    }

    /**
     * This event fires when a team is cleared
     */
    public static class ClearTeam extends SkyblockOpManageEvent {

        private final Team team;

        public ClearTeam(CommandSourceStack source, Team team) {
            super(source);
            this.team = team;
        }

        /**
         * @return Cleared team
         */
        public Team getTeam() {
            return this.team;
        }
    }

    /**
     * This event fires when a team is created
     */
    public static class CreateTeam extends SkyblockOpManageEvent {

        private String name;
        private final boolean join;

        public CreateTeam(CommandSourceStack source, String name, boolean join) {
            super(source);
            this.name = name;
            this.join = join;
        }

        /**
         * @return Teams name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name New name for the team
         * @return This {@link CreateTeam} event
         */
        public CreateTeam setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @return Whether the command source joins or not
         */
        public boolean isJoin() {
            return this.join;
        }
    }

    /**
     * This event fires whenever players are added to a team.
     */
    public static class AddToTeam extends SkyblockOpManageEvent {

        private final Team team;
        private final Set<ServerPlayer> players;

        public AddToTeam(CommandSourceStack source, Team team, HashSet<ServerPlayer> players) {
            super(source);
            this.team = team;
            this.players = players;
        }

        /**
         * @return The team the players are added to
         */
        public Team getTeam() {
            return this.team;
        }

        /**
         * This {@link Set} of {@link ServerPlayer} can be modified
         *
         * @return The players which will be added
         */
        public Set<ServerPlayer> getPlayers() {
            return this.players;
        }
    }

    /**
     * This event fires whenever players are removed from a team.
     */
    public static class RemoveFromTeam extends SkyblockOpManageEvent {

        private final Team team;
        private final Set<ServerPlayer> players;

        public RemoveFromTeam(CommandSourceStack source, Team team, HashSet<ServerPlayer> players) {
            super(source);
            this.team = team;
            this.players = players;
        }

        /**
         * @return The team the players are removed from
         */
        public Team getTeam() {
            return this.team;
        }

        /**
         * This {@link Set} of {@link ServerPlayer} can be modified
         *
         * @return The players which will be removed
         */
        public Set<ServerPlayer> getPlayers() {
            return this.players;
        }
    }
}
