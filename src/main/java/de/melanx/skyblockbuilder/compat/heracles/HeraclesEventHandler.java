package de.melanx.skyblockbuilder.compat.heracles;

import earth.terrarium.heracles.common.handlers.progress.QuestProgressHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeraclesEventHandler {

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
