package de.melanx.skyblockbuilder.compat.heracles;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

public class HeraclesCompat {

    public static final String MODID = "heracles";

    public static void registerHeracles() {
//        SpreadLocationTask.register();
//        QuestTaskDisplayFormatter.register(SpreadLocationTask.TYPE, ((progress, task) -> String.format("%d/%d", task.storage().read(progress.progress()) ? 1 : 0, 1)));
//        TaskTitleFormatter.register(SpreadLocationTask.TYPE, task -> {
//            if (task.predicate().getSpreads().size() > 1) {
//                return Component.translatable(TaskTitleFormatters.toTranslationKey(task, false), task.predicate().getSpreads().size());
//            } else {
//                return Component.translatable(TaskTitleFormatters.toTranslationKey(task, true));
//            }
//        });
//        NeoForge.EVENT_BUS.register(new HeraclesEventHandler());
    }

    public static void resetQuestProgress(Collection<ServerPlayer> players) {
//        for (ServerPlayer player : players) {
//            HeraclesCompat.resetQuestProgress(player);
//        }
    }

    public static void resetQuestProgress(ServerPlayer player) {
//        if (!HeraclesConfig.resetQuestProgress || player.hasPermissions(2)) {
//            return;
//        }
//
//        QuestProgressHandler.getProgress(player.server, player.getUUID()).reset();
//        player.sendSystemMessage(Component.translatable("heracles.skyblockbuilder.reset_quest_progress").withStyle(ChatFormatting.RED), true);
    }

    public static void resetQuestProgress(MinecraftServer server, UUID player) {
//        if (!HeraclesConfig.resetQuestProgress || server.getProfilePermissions(GameProfileCache.get(player)) >= 2) {
//            return;
//        }
//
//        QuestProgressHandler.getProgress(server, player).reset();
    }

    public static void registerHeraclesClient() {
//        Settings.register(SpreadLocationTask.TYPE, SpreadTaskSettings.INSTANCE);
//        QuestTaskWidgets.registerSimple(SpreadLocationTask.TYPE, SpreadTaskWidget::new);
    }
}
