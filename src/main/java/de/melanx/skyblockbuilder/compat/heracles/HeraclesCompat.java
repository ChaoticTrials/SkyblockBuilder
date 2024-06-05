package de.melanx.skyblockbuilder.compat.heracles;

import earth.terrarium.heracles.api.client.settings.Settings;
import earth.terrarium.heracles.api.tasks.QuestTaskDisplayFormatter;
import earth.terrarium.heracles.api.tasks.client.QuestTaskWidgets;
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter;
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatters;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;

public class HeraclesCompat {

    public static final String MODID = "heracles";

    public static void registerHeracles() {
        SpreadLocationTask.register();
        Settings.register(SpreadLocationTask.TYPE, SpreadTaskSettings.INSTANCE);
        QuestTaskDisplayFormatter.register(SpreadLocationTask.TYPE, ((progress, task) -> String.format("%d/%d", task.storage().read(progress.progress()) ? 1 : 0, 1)));
        QuestTaskWidgets.registerSimple(SpreadLocationTask.TYPE, SpreadTaskWidget::new);
        TaskTitleFormatter.register(SpreadLocationTask.TYPE, task -> {
            if (task.predicate().getSpreads().size() > 1) {
                return Component.translatable(TaskTitleFormatters.toTranslationKey(task, false), task.predicate().getSpreads().size());
            } else {
                return Component.translatable(TaskTitleFormatters.toTranslationKey(task, true));
            }
        });
        MinecraftForge.EVENT_BUS.register(new HeraclesEventHandler());
    }
}
