package de.melanx.skyblockbuilder.compat.heracles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import earth.terrarium.heracles.api.CustomizableQuestElement;
import earth.terrarium.heracles.api.quests.QuestIcon;
import earth.terrarium.heracles.api.quests.QuestIcons;
import earth.terrarium.heracles.api.quests.defaults.ItemQuestIcon;
import earth.terrarium.heracles.api.tasks.QuestTask;
import earth.terrarium.heracles.api.tasks.QuestTaskType;
import earth.terrarium.heracles.api.tasks.QuestTasks;
import earth.terrarium.heracles.api.tasks.storage.defaults.BooleanTaskStorage;
import net.minecraft.nbt.NumericTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public record SpreadLocationTask(String id, String title, QuestIcon<?> icon, SpreadPredicate predicate) implements QuestTask<ServerPlayer, NumericTag, SpreadLocationTask>, CustomizableQuestElement {

    public static final QuestTaskType<SpreadLocationTask> TYPE = new Type();

    @Override
    public NumericTag test(QuestTaskType<?> type, NumericTag progress, ServerPlayer player) {
        return this.storage().of(progress, this.predicate.matches(player));
    }

    @Override
    public float getProgress(NumericTag numericTag) {
        return this.storage().readBoolean(numericTag) ? 1.0F : 0.0F;
    }

    @Override
    public BooleanTaskStorage storage() {
        return BooleanTaskStorage.INSTANCE;
    }

    @Override
    public QuestTaskType<SpreadLocationTask> type() {
        return SpreadLocationTask.TYPE;
    }

    public static void register() {
        QuestTasks.register(SpreadLocationTask.TYPE);
    }

    private static class Type implements QuestTaskType<SpreadLocationTask> {

        @Override
        public ResourceLocation id() {
            return SkyblockBuilder.getInstance().resource("spread_location");
        }

        public Codec<SpreadLocationTask> codec(String id) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    RecordCodecBuilder.point(id),
                    Codec.STRING.optionalFieldOf("title", "").forGetter(SpreadLocationTask::title),
                    QuestIcons.CODEC.optionalFieldOf("icon", ItemQuestIcon.AIR).orElse(new ItemQuestIcon(Items.FILLED_MAP)).forGetter(SpreadLocationTask::icon),
                    SpreadPredicate.CODEC.fieldOf("predicate").forGetter(SpreadLocationTask::predicate)
            ).apply(instance, SpreadLocationTask::new));
        }
    }
}
