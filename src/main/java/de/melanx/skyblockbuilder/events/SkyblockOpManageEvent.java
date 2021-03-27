package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.Set;

// Generic event on any invocation of the manage command.
public abstract class SkyblockOpManageEvent extends Event {

    private final CommandSource source;

    private SkyblockOpManageEvent(CommandSource source) {
        this.source = source;
    }

    public CommandSource getSource() {
        return this.source;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
    
    public static class DeleteTeam extends SkyblockOpManageEvent {

        private final Team team;
        
        public DeleteTeam(CommandSource source, Team team) {
            super(source);
            this.team = team;
        }

        public Team getTeam() {
            return this.team;
        }
    }
    
    public static class ClearTeam extends SkyblockOpManageEvent {

        private final Team team;

        public ClearTeam(CommandSource source, Team team) {
            super(source);
            this.team = team;
        }

        public Team getTeam() {
            return this.team;
        }
    }
    
    public static class CreateTeam extends SkyblockOpManageEvent {

        private String name;
        private final boolean join;

        public CreateTeam(CommandSource source, String name, boolean join) {
            super(source);
            this.name = name;
            this.join = join;
        }

        public String getName() {
            return this.name;
        }

        public CreateTeam setName(String name) {
            this.name = name;
            return this;
        }

        public boolean isJoin() {
            return this.join;
        }
    }
    
    // players can be modified
    public static class AddToTeam extends SkyblockOpManageEvent {
        
        private final Team team;
        private final Set<ServerPlayerEntity> players;

        public AddToTeam(CommandSource source, Team team, HashSet<ServerPlayerEntity> players) {
            super(source);
            this.team = team;
            this.players = players;
        }

        public Team getTeam() {
            return this.team;
        }

        public Set<ServerPlayerEntity> getPlayers() {
            return this.players;
        }
    }
    
    // players can be modified
    public static class RemoveFromTeam extends SkyblockOpManageEvent {
        
        private final Team team;
        private final Set<ServerPlayerEntity> players;

        public RemoveFromTeam(CommandSource source, Team team, HashSet<ServerPlayerEntity> players) {
            super(source);
            this.team = team;
            this.players = players;
        }

        public Team getTeam() {
            return this.team;
        }

        public Set<ServerPlayerEntity> getPlayers() {
            return this.players;
        }
    }
}
