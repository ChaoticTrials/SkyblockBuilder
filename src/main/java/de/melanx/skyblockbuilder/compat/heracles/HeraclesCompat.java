package de.melanx.skyblockbuilder.compat.heracles;

import earth.terrarium.heracles.api.client.settings.Settings;
import earth.terrarium.heracles.api.tasks.QuestTaskDisplayFormatter;
import earth.terrarium.heracles.api.tasks.client.QuestTaskWidgets;
import net.minecraftforge.common.MinecraftForge;

public class HeraclesCompat {

    public static final String MODID = "heracles";

    public static void registerHeracles() {
        SpreadLocationTask.register();
        Settings.register(SpreadLocationTask.TYPE, SpreadTaskSettings.INSTANCE);
        QuestTaskDisplayFormatter.register(SpreadLocationTask.TYPE, ((progress, task) -> String.format("%d/%d", task.storage().read(progress.progress()) ? 1 : 0, 1)));
        QuestTaskWidgets.registerSimple(SpreadLocationTask.TYPE, SpreadTaskWidget::new);
        MinecraftForge.EVENT_BUS.register(new HeraclesEventHandler());
    }
}
