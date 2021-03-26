package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.util.Team;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SkyblockHooks {

    public static Event.Result onVisit(ServerPlayerEntity player, Team team) {
        SkyblockVisitEvent event = new SkyblockVisitEvent(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }
    
    public static Pair<Event.Result, Boolean> onToggleVisits(ServerPlayerEntity player, Team team, boolean allowVisits) {
        SkyblockManageTeamEvent.ToggleVisits event = new SkyblockManageTeamEvent.ToggleVisits(player, team, allowVisits);
        MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.shouldAllowVisits());
    }
    
    public static Pair<Event.Result, BlockPos> onAddSpawn(ServerPlayerEntity player, Team team, BlockPos pos) {
        SkyblockManageTeamEvent.AddSpawn event = new SkyblockManageTeamEvent.AddSpawn(player, team, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.getPos());
    }
    
    public static Event.Result onRemoveSpawn(ServerPlayerEntity player, Team team, BlockPos pos) {
        SkyblockManageTeamEvent.RemoveSpawn event = new SkyblockManageTeamEvent.RemoveSpawn(player, team, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }
    
    public static Event.Result onResetSpawns(ServerPlayerEntity player, Team team) {
        SkyblockManageTeamEvent.ResetSpawns event = new SkyblockManageTeamEvent.ResetSpawns(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockManageTeamEvent.Rename onRename(ServerPlayerEntity player, Team team, String newName) {
        SkyblockManageTeamEvent.Rename event = new SkyblockManageTeamEvent.Rename(player, team, newName);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
    
    public static Event.Result onLeave(ServerPlayerEntity player, Team team) {
        SkyblockManageTeamEvent.Leave event = new SkyblockManageTeamEvent.Leave(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockInvitationEvent.Invite onInvite(ServerPlayerEntity player, Team team, @Nonnull ServerPlayerEntity invitor) {
        SkyblockInvitationEvent.Invite event = new SkyblockInvitationEvent.Invite(player, team, invitor);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static Event.Result onAccept(ServerPlayerEntity player, Team team) {
        SkyblockInvitationEvent.Accept event = new SkyblockInvitationEvent.Accept(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static Event.Result onDecline(ServerPlayerEntity player, Team team) {
        SkyblockInvitationEvent.Decline event = new SkyblockInvitationEvent.Decline(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static boolean onCreateTeam(String name) {
        SkyblockCreateTeamEvent event = new SkyblockCreateTeamEvent(name);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static Event.Result onHome(ServerPlayerEntity player, Team team) {
        SkyblockTeleportHomeEvent event = new SkyblockTeleportHomeEvent(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }
    
    public static boolean onManageDeleteTeam(CommandSource source, Team team) {
        SkyblockOpManageEvent.DeleteTeam event = new SkyblockOpManageEvent.DeleteTeam(source, team);
        return MinecraftForge.EVENT_BUS.post(event);
    }
    
    public static boolean onManageClearTeam(CommandSource source, Team team) {
        SkyblockOpManageEvent.ClearTeam event = new SkyblockOpManageEvent.ClearTeam(source, team);
        return MinecraftForge.EVENT_BUS.post(event);
    }
    
    public static Pair<Boolean, String> onManageCreateTeam(CommandSource source, String name, boolean join) {
        SkyblockOpManageEvent.CreateTeam event = new SkyblockOpManageEvent.CreateTeam(source, name, join);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getName());
    }
    
    public static Pair<Boolean, Set<ServerPlayerEntity>> onManageAddToTeam(CommandSource source, Team team, Collection<ServerPlayerEntity> players) {
        SkyblockOpManageEvent.AddToTeam event = new SkyblockOpManageEvent.AddToTeam(source, team, new HashSet<>(players));
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getPlayers());
    }
    
    public static Pair<Boolean, Set<ServerPlayerEntity>> onManageRemoveFromTeam(CommandSource source, Team team, Collection<ServerPlayerEntity> players) {
        SkyblockOpManageEvent.RemoveFromTeam event = new SkyblockOpManageEvent.RemoveFromTeam(source, team, new HashSet<>(players));
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getPlayers());
    }
}
