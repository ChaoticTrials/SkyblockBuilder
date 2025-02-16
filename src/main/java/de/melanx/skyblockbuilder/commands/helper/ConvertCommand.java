package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ConvertCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("convert").requires(source -> source.hasPermission(2))
                .executes(ConvertCommand::convert);
    }

    private static int convert(CommandContext<CommandSourceStack> context) {
        for (File original : Objects.requireNonNull(SkyPaths.CONVERT_INPUT.toFile().listFiles())) {
            Path fileName = original.toPath().getFileName();
            if (original.toString().endsWith(".nbt")) {
                String convertedName = fileName.toString().substring(0, fileName.toString().length() - ".nbt".length()) + ".snbt";
                Path converted = SkyPaths.CONVERT_OUTPUT.resolve(convertedName);

                try {
                    CompoundTag nbt = TemplateUtil.readTemplate(original.toPath(), false);
                    TemplateUtil.writeTemplate(converted, nbt, true);

                    context.getSource().sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.convert_template", fileName.toString(), convertedName), true);
                } catch (IOException | CommandSyntaxException e) {
                    SkyblockBuilder.getLogger().error("Failed to convert {} to {}", original, convertedName, e);
                }
            } else if (original.toString().endsWith(".snbt")) {
                String convertedName = fileName.toString().substring(0, fileName.toString().length() - ".snbt".length()) + ".nbt";
                Path converted = SkyPaths.CONVERT_OUTPUT.resolve(convertedName);

                try {
                    CompoundTag nbt = TemplateUtil.readTemplate(original.toPath(), true);
                    TemplateUtil.writeTemplate(converted, nbt, false);

                    context.getSource().sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.convert_template", fileName.toString(), convertedName), true);
                } catch (IOException | CommandSyntaxException e) {
                    SkyblockBuilder.getLogger().error("Failed to convert {} to {}", original, convertedName, e);
                }
            }
        }

        return 1;
    }
}
