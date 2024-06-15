package de.melanx.skyblockbuilder.compat.heracles;

import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.events.SkyblockOpManageEvent;
import earth.terrarium.heracles.common.handlers.progress.QuestProgressHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class HeraclesEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLeavesTeam(SkyblockManageTeamEvent.Leave event) {
        ServerPlayer player = event.getPlayer();
        HeraclesCompat.resetQuestProgress(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerRemovedFromTeam(SkyblockOpManageEvent.RemoveFromTeam event) {
        HeraclesCompat.resetQuestProgress(event.getPlayers());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTeamDeletion(SkyblockOpManageEvent.DeleteTeam event) {
        for (UUID player : event.getTeam().getPlayers()) {
            HeraclesCompat.resetQuestProgress(event.getSource().getServer(), player);
        }
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) {
            return;
        }

        MinecraftServer server = event.getServer();
        server.getPlayerList().getPlayers().forEach(player -> {
            QuestProgressHandler.getProgress(server, player.getUUID()).testAndProgressTaskType(player, player, SpreadLocationTask.TYPE);
        });
    }
}
