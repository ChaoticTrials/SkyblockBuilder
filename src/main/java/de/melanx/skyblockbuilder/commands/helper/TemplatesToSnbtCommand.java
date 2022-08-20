package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TemplatesToSnbtCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("templates_to_snbt").requires(source -> source.hasPermission(2))
                .executes(TemplatesToSnbtCommand::convert);
    }

    private static int convert(CommandContext<CommandSourceStack> context) {
        for (File original : Objects.requireNonNull(SkyPaths.TEMPLATES_DIR.toFile().listFiles())) {
            if (original.toString().endsWith(".nbt")) {
                Path fileName = original.toPath().getFileName();
                String convertedName = fileName.toString().substring(0, fileName.toString().length() - ".nbt".length()) + ".snbt";
                Path converted = SkyPaths.TEMPLATES_DIR.resolve(convertedName);

                StructureTemplate template = new StructureTemplate();
                try {
                    CompoundTag nbt = NbtIo.readCompressed(original);
                    template.load(nbt);
                    Files.writeString(converted, NbtUtils.structureToSnbt(nbt));

                    context.getSource().sendSuccess(Component.translatable("skyblockbuilder.command.success.convert_template", fileName, convertedName), true);
                } catch (IOException e) {
                    SkyblockBuilder.getLogger().error("Failed to convert " + original + " to " + convertedName, e);
                }
            }
        }

        return 1;
    }
}
