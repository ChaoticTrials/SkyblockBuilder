package de.melanx.skyblockbuilder.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.registration.ModBlocks;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import de.melanx.skyblockbuilder.registration.ModItems;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Path;
import java.util.function.Supplier;

public record StructureSaverSettings(String name, boolean saveToConfig, boolean ignoreAir, boolean nbtToSnbt, boolean keepPositions) {

    public static final Codec<StructureSaverSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(StructureSaverSettings::name),
            Codec.BOOL.optionalFieldOf("save_to_config", false).forGetter(StructureSaverSettings::saveToConfig),
            Codec.BOOL.optionalFieldOf("ignore_air", false).forGetter(StructureSaverSettings::ignoreAir),
            Codec.BOOL.optionalFieldOf("nbt_to_snbt", false).forGetter(StructureSaverSettings::nbtToSnbt),
            Codec.BOOL.optionalFieldOf("keep_positions", true).forGetter(StructureSaverSettings::keepPositions)
    ).apply(instance, StructureSaverSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureSaverSettings> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeUtf(value.name);
                buffer.writeBoolean(value.saveToConfig);
                buffer.writeBoolean(value.ignoreAir);
                buffer.writeBoolean(value.nbtToSnbt);
                buffer.writeBoolean(value.keepPositions);
            }, buffer -> new StructureSaverSettings(buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean())
    );

    public static final StructureSaverSettings DEFAULT = new StructureSaverSettings("", false, false, false, true);

    public enum Type {
        ISLAND(SkyComponents.SCREEN_STRUCTURE_SAVER_TAB_ISLAND, SkyPaths.ISLANDS_DIR, () -> ModBlocks.spawnBlock),
        SPREAD(SkyComponents.SCREEN_STRUCTURE_SAVER_TAB_SPREAD, SkyPaths.SPREADS_DIR, () -> Blocks.AIR),
        NETHER(SkyComponents.SCREEN_STRUCTURE_SAVER_TAB_NETHER, SkyPaths.PORTALS_DIR, () -> Blocks.NETHER_PORTAL);

        private final Component tooltip;
        private final Path outputPath;
        private final Supplier<Block> blockSupplier;
        private ItemStack stack;

        Type(Component tooltip, Path outputPath, Supplier<Block> blockSupplier) {
            this.tooltip = tooltip;
            this.outputPath = outputPath;
            this.blockSupplier = blockSupplier;
        }

        public ItemStack getStack() {
            if (this.stack == null) {
                this.stack = new ItemStack(ModItems.structureSaver);
                this.stack.set(ModDataComponentTypes.structureSaverType, this);
            }

            return this.stack;
        }

        public Component getTooltip() {
            return this.tooltip;
        }

        public Path getOutputPath() {
            return this.outputPath;
        }

        public Block getRequiredBlock() {
            return this.blockSupplier.get();
        }
    }
}
