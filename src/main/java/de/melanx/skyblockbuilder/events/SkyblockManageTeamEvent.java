package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This fires whenever something is managed in the team.<br>
 * <br>
 * All children of this event does have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class SkyblockManageTeamEvent extends Event {

    private final ServerPlayer player;
    private final Team team;

    /**
     * @param player Player who manages the team
     * @param team   Managed team
     */
    private SkyblockManageTeamEvent(@Nullable ServerPlayer player, Team team) {
        this.player = player;
        this.team = team;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    /**
     * This event is fired whenever the visits for a team are toggled.
     */
    public static class ToggleVisits extends SkyblockManageTeamEvent {

        private boolean allowVisits;

        public ToggleVisits(@Nonnull ServerPlayer player, Team team, boolean allowVisits) {
            super(player, team);
            this.allowVisits = allowVisits;
        }

        /**
         * @return The player who changes the visit ability
         */
        @Nonnull
        @Override
        public ServerPlayer getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        /**
         * @return Current visiting state
         */
        public boolean shouldAllowVisits() {
            return this.allowVisits;
        }

        /**
         * @param allowVisits The new visiting state
         */
        public void setAllowVisits(boolean allowVisits) {
            this.allowVisits = allowVisits;
        }
    }

    /**
     * This event is fired whenever the join requests for a team are toggled.
     */
    public static class ToggleRequests extends SkyblockManageTeamEvent {

        private boolean allowRequests;

        public ToggleRequests(@Nonnull ServerPlayer player, Team team, boolean allowRequests) {
            super(player, team);
            this.allowRequests = allowRequests;
        }

        /**
         * @return Current requesting state
         */
        public boolean shouldAllowRequests() {
            return this.allowRequests;
        }

        /**
         * @param allowRequests The new requesting state
         */
        public void setAllowRequests(boolean allowRequests) {
            this.allowRequests = allowRequests;
        }
    }

    /**
     * This event is fired whenever a player adds a new spawn point to the teams island.
     */
    public static class AddSpawn extends SkyblockManageTeamEvent {

        private BlockPos pos;
        private Direction direction;

        public AddSpawn(@Nonnull ServerPlayer player, Team team, BlockPos pos, Direction direction) {
            super(player, team);
            this.pos = pos;
            this.direction = direction;
        }

        /**
         * @return Player who adds the spawn point
         */
        @Nonnull
        @Override
        public ServerPlayer getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        /**
         * @return New spawn position
         */
        public BlockPos getPos() {
            return this.pos;
        }

        /**
         * @param pos A new position for the future spawn point
         * @return This {@link AddSpawn} event
         */
        public AddSpawn setPos(BlockPos pos) {
            this.pos = pos;
            return this;
        }

        /**
         * @return Facing direction for the spawn
         */
        public Direction getDirection() {
            return this.direction;
        }

        /**
         * @param direction Another direction for the future spawn point
         * @return This {@link AddSpawn} event
         */
        public AddSpawn setDirection(@Nonnull Direction direction) {
            this.direction = direction;
            return this;
        }
    }

    /**
     * This event is fired whenever a player removes a spawn point from the teams island.
     */
    public static class RemoveSpawn extends SkyblockManageTeamEvent {

        private final BlockPos pos;

        public RemoveSpawn(@Nonnull ServerPlayer player, Team team, BlockPos pos) {
            super(player, team);
            this.pos = pos;
        }

        /**
         * @return Player who removes the spawn point
         */
        @Nonnull
        @Override
        public ServerPlayer getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        /**
         * @return Removed spawn point position
         */
        public BlockPos getPos() {
            return this.pos;
        }
    }

    /**
     * This event is fired when the possible spawn points for an island are reset.
     */
    public static class ResetSpawns extends SkyblockManageTeamEvent {

        public ResetSpawns(ServerPlayer player, Team team) {
            super(player, team);
        }
    }

    /**
     * This event is fired when a team is renamed.
     */
    public static class Rename extends SkyblockManageTeamEvent {

        private String newName;

        public Rename(ServerPlayer player, Team team, String newName) {
            super(player, team);
            this.newName = newName;
        }

        /**
         * @return New team name
         */
        public String getNewName() {
            return this.newName;
        }

        /**
         * @param newName A new name
         * @return This {@link Rename} event
         */
        public Rename setNewName(String newName) {
            this.newName = newName;
            return this;
        }
    }

    /**
     * This event is fired when a {@link #player} leaves its team.
     */
    public static class Leave extends SkyblockManageTeamEvent {

        public Leave(@Nonnull ServerPlayer player, Team team) {
            super(player, team);
        }

        /**
         * @return Player who leaves the team
         */
        @Nonnull
        @Override
        public ServerPlayer getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }
    }
}
