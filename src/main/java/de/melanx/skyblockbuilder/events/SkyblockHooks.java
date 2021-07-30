package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SkyblockHooks {

    public static Event.Result onVisit(ServerPlayer player, Team team) {
        SkyblockVisitEvent event = new SkyblockVisitEvent(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static Pair<Event.Result, Boolean> onToggleVisits(ServerPlayer player, Team team, boolean allowVisits) {
        SkyblockManageTeamEvent.ToggleVisits event = new SkyblockManageTeamEvent.ToggleVisits(player, team, allowVisits);
        MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.shouldAllowVisits());
    }

    public static Pair<Event.Result, BlockPos> onAddSpawn(ServerPlayer player, Team team, BlockPos pos) {
        SkyblockManageTeamEvent.AddSpawn event = new SkyblockManageTeamEvent.AddSpawn(player, team, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.getPos());
    }

    public static Event.Result onRemoveSpawn(ServerPlayer player, Team team, BlockPos pos) {
        SkyblockManageTeamEvent.RemoveSpawn event = new SkyblockManageTeamEvent.RemoveSpawn(player, team, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static Event.Result onResetSpawns(ServerPlayer player, Team team) {
        SkyblockManageTeamEvent.ResetSpawns event = new SkyblockManageTeamEvent.ResetSpawns(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockManageTeamEvent.Rename onRename(ServerPlayer player, Team team, String newName) {
        SkyblockManageTeamEvent.Rename event = new SkyblockManageTeamEvent.Rename(player, team, newName);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static Event.Result onLeave(@Nonnull ServerPlayer player, Team team) {
        SkyblockManageTeamEvent.Leave event = new SkyblockManageTeamEvent.Leave(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockInvitationEvent.Invite onInvite(ServerPlayer player, Team team, @Nonnull ServerPlayer invitor) {
        SkyblockInvitationEvent.Invite event = new SkyblockInvitationEvent.Invite(player, team, invitor);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static Event.Result onAccept(ServerPlayer player, Team team) {
        SkyblockInvitationEvent.Accept event = new SkyblockInvitationEvent.Accept(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static Event.Result onDecline(ServerPlayer player, Team team) {
        SkyblockInvitationEvent.Decline event = new SkyblockInvitationEvent.Decline(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockJoinRequestEvent.SendRequest onSendJoinRequest(ServerPlayer player, Team team) {
        SkyblockJoinRequestEvent.SendRequest event = new SkyblockJoinRequestEvent.SendRequest(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockJoinRequestEvent.AcceptRequest onAcceptJoinRequest(ServerPlayer acceptor, ServerPlayer requester, Team team) {
        SkyblockJoinRequestEvent.AcceptRequest event = new SkyblockJoinRequestEvent.AcceptRequest(acceptor, requester, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockJoinRequestEvent.DenyRequest onDenyJoinRequest(ServerPlayer denier, ServerPlayer requester, Team team) {
        SkyblockJoinRequestEvent.DenyRequest event = new SkyblockJoinRequestEvent.DenyRequest(denier, requester, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static Pair<Event.Result, Boolean> onToggleRequests(ServerPlayer player, Team team, boolean allowVisits) {
        SkyblockManageTeamEvent.ToggleRequests event = new SkyblockManageTeamEvent.ToggleRequests(player, team, allowVisits);
        MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.shouldAllowRequests());
    }

    public static boolean onCreateTeam(String name) {
        SkyblockCreateTeamEvent event = new SkyblockCreateTeamEvent(name);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static Event.Result onHome(ServerPlayer player, Team team) {
        SkyblockTeleportHomeEvent event = new SkyblockTeleportHomeEvent(player, team);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static boolean onManageDeleteTeam(CommandSourceStack source, Team team) {
        SkyblockOpManageEvent.DeleteTeam event = new SkyblockOpManageEvent.DeleteTeam(source, team);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static boolean onManageClearTeam(CommandSourceStack source, Team team) {
        SkyblockOpManageEvent.ClearTeam event = new SkyblockOpManageEvent.ClearTeam(source, team);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static Pair<Boolean, String> onManageCreateTeam(CommandSourceStack source, String name, boolean join) {
        SkyblockOpManageEvent.CreateTeam event = new SkyblockOpManageEvent.CreateTeam(source, name, join);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getName());
    }

    public static Pair<Boolean, Set<ServerPlayer>> onManageAddToTeam(CommandSourceStack source, Team team, Collection<ServerPlayer> players) {
        SkyblockOpManageEvent.AddToTeam event = new SkyblockOpManageEvent.AddToTeam(source, team, new HashSet<>(players));
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getPlayers());
    }

    public static Pair<Boolean, Set<ServerPlayer>> onManageRemoveFromTeam(CommandSourceStack source, Team team, Collection<ServerPlayer> players) {
        SkyblockOpManageEvent.RemoveFromTeam event = new SkyblockOpManageEvent.RemoveFromTeam(source, team, new HashSet<>(players));
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return Pair.of(canceled, event.getPlayers());
    }
}
