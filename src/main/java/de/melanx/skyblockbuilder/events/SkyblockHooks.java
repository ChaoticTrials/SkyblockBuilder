package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SkyblockHooks {

    public static SkyblockVisitEvent.Result onVisit(ServerPlayer player, Team team) {
        SkyblockVisitEvent event = new SkyblockVisitEvent(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static Pair<SkyblockManageTeamEvent.Result, Boolean> onToggleVisits(ServerPlayer player, Team team, boolean allowVisits) {
        SkyblockManageTeamEvent.ToggleVisits event = new SkyblockManageTeamEvent.ToggleVisits(player, team, allowVisits);
        NeoForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.shouldAllowVisits());
    }

    public static Pair<SkyblockManageTeamEvent.Result, TemplatesConfig.Spawn> onAddSpawn(ServerPlayer player, Team team, BlockPos pos, Direction direction) {
        SkyblockManageTeamEvent.AddSpawn event = new SkyblockManageTeamEvent.AddSpawn(player, team, pos, direction);
        NeoForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), new TemplatesConfig.Spawn(event.getPos(), WorldUtil.SpawnDirection.fromDirection(event.getDirection())));
    }

    public static SkyblockManageTeamEvent.Result onRemoveSpawn(ServerPlayer player, Team team, BlockPos pos) {
        SkyblockManageTeamEvent.RemoveSpawn event = new SkyblockManageTeamEvent.RemoveSpawn(player, team, pos);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockManageTeamEvent.Result onResetSpawns(ServerPlayer player, Team team) {
        SkyblockManageTeamEvent.ResetSpawns event = new SkyblockManageTeamEvent.ResetSpawns(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockManageTeamEvent.Rename onRename(ServerPlayer player, Team team, String newName) {
        SkyblockManageTeamEvent.Rename event = new SkyblockManageTeamEvent.Rename(player, team, newName);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockManageTeamEvent.Result onLeave(@Nonnull ServerPlayer player, Team team) {
        SkyblockManageTeamEvent.Leave event = new SkyblockManageTeamEvent.Leave(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockInvitationEvent.Invite onInvite(ServerPlayer player, Team team, @Nonnull ServerPlayer invitor) {
        SkyblockInvitationEvent.Invite event = new SkyblockInvitationEvent.Invite(player, team, invitor);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockInvitationEvent.Result onAccept(ServerPlayer player, Team team) {
        SkyblockInvitationEvent.Accept event = new SkyblockInvitationEvent.Accept(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockInvitationEvent.Result onDecline(ServerPlayer player, Team team) {
        SkyblockInvitationEvent.Decline event = new SkyblockInvitationEvent.Decline(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static SkyblockJoinRequestEvent.SendRequest onSendJoinRequest(ServerPlayer player, Team team) {
        SkyblockJoinRequestEvent.SendRequest event = new SkyblockJoinRequestEvent.SendRequest(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockJoinRequestEvent.AcceptRequest onAcceptJoinRequest(ServerPlayer acceptor, ServerPlayer requester, Team team) {
        SkyblockJoinRequestEvent.AcceptRequest event = new SkyblockJoinRequestEvent.AcceptRequest(acceptor, requester, team);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static SkyblockJoinRequestEvent.DenyRequest onDenyJoinRequest(ServerPlayer denier, ServerPlayer requester, Team team) {
        SkyblockJoinRequestEvent.DenyRequest event = new SkyblockJoinRequestEvent.DenyRequest(denier, requester, team);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static Pair<SkyblockManageTeamEvent.Result, Boolean> onToggleRequests(ServerPlayer player, Team team, boolean allowVisits) {
        SkyblockManageTeamEvent.ToggleRequests event = new SkyblockManageTeamEvent.ToggleRequests(player, team, allowVisits);
        NeoForge.EVENT_BUS.post(event);
        return Pair.of(event.getResult(), event.shouldAllowRequests());
    }

    public static boolean onCreateTeam(String name) {
        SkyblockCreateTeamEvent event = new SkyblockCreateTeamEvent(name);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static SkyblockTeleportHomeEvent.Result onHome(ServerPlayer player, Team team) {
        SkyblockTeleportHomeEvent event = new SkyblockTeleportHomeEvent(player, team);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static boolean onManageDeleteTeam(CommandSourceStack source, Team team) {
        SkyblockOpManageEvent.DeleteTeam event = new SkyblockOpManageEvent.DeleteTeam(source, team);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onManageClearTeam(CommandSourceStack source, Team team) {
        SkyblockOpManageEvent.ClearTeam event = new SkyblockOpManageEvent.ClearTeam(source, team);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static Pair<Boolean, String> onManageCreateTeam(CommandSourceStack source, String name, boolean join) {
        SkyblockOpManageEvent.CreateTeam event = new SkyblockOpManageEvent.CreateTeam(source, name, join);
        boolean canceled = NeoForge.EVENT_BUS.post(event).isCanceled();
        return Pair.of(canceled, event.getName());
    }

    public static Pair<Boolean, Set<ServerPlayer>> onManageAddToTeam(CommandSourceStack source, Team team, Collection<ServerPlayer> players) {
        SkyblockOpManageEvent.AddToTeam event = new SkyblockOpManageEvent.AddToTeam(source, team, new HashSet<>(players));
        boolean canceled = NeoForge.EVENT_BUS.post(event).isCanceled();
        return Pair.of(canceled, event.getPlayers());
    }

    public static Pair<Boolean, Set<ServerPlayer>> onManageRemoveFromTeam(CommandSourceStack source, Team team, Collection<ServerPlayer> players) {
        SkyblockOpManageEvent.RemoveFromTeam event = new SkyblockOpManageEvent.RemoveFromTeam(source, team, new HashSet<>(players));
        boolean canceled = NeoForge.EVENT_BUS.post(event).isCanceled();
        return Pair.of(canceled, event.getPlayers());
    }
}
