package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.util.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SkyblockManageTeamEvent extends Event {

    private final ServerPlayerEntity player;
    private final Team team;

    private SkyblockManageTeamEvent(@Nullable ServerPlayerEntity player, Team team) {
        this.player = player;
        this.team = team;
    }

    @Nullable
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    public static class ToggleVisits extends SkyblockManageTeamEvent {

        private boolean allowVisits;

        public ToggleVisits(@Nonnull ServerPlayerEntity player, Team team, boolean allowVisits) {
            super(player, team);
            this.allowVisits = allowVisits;
        }

        @Nonnull
        @Override
        public ServerPlayerEntity getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        public boolean shouldAllowVisits() {
            return this.allowVisits;
        }

        public void setAllowVisits(boolean allowVisits) {
            this.allowVisits = allowVisits;
        }
    }

    public static class AddSpawn extends SkyblockManageTeamEvent {

        private BlockPos pos;

        public AddSpawn(@Nonnull ServerPlayerEntity player, Team team, BlockPos pos) {
            super(player, team);
            this.pos = pos;
        }

        @Nonnull
        @Override
        public ServerPlayerEntity getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public AddSpawn setPos(BlockPos pos) {
            this.pos = pos;
            return this;
        }
    }

    public static class RemoveSpawn extends SkyblockManageTeamEvent {

        private final BlockPos pos;

        public RemoveSpawn(@Nonnull ServerPlayerEntity player, Team team, BlockPos pos) {
            super(player, team);
            this.pos = pos;
        }

        @Nonnull
        @Override
        public ServerPlayerEntity getPlayer() {
            //noinspection ConstantConditions
            return super.getPlayer();
        }

        public BlockPos getPos() {
            return this.pos;
        }
    }

    public static class ResetSpawns extends SkyblockManageTeamEvent {

        public ResetSpawns(ServerPlayerEntity player, Team team) {
            super(player, team);
        }
    }

    public static class Rename extends SkyblockManageTeamEvent {

        private String newName;

        public Rename(ServerPlayerEntity player, Team team, String newName) {
            super(player, team);
            this.newName = newName;
        }

        public String getNewName() {
            return this.newName;
        }

        public Rename setNewName(String newName) {
            this.newName = newName;
            return this;
        }
    }
    
    public static class Leave extends SkyblockManageTeamEvent {
        
        public Leave(ServerPlayerEntity player, Team team) {
            super(player, team);
        }
    }
}
