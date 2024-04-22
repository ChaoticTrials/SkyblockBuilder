package de.melanx.skyblockbuilder.commands.operator;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.moddingx.libx.command.CommandUtil;

import java.io.IOException;

public class GenerateCommand {

    private static final StructurePlaceSettings SETTINGS = new StructurePlaceSettings().setKnownShape(true).setKeepLiquids(false);

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("generate").requires(source -> source.hasPermission(2))
                .then(Commands.literal("template")
                        .then(Commands.argument("template", StringArgumentType.string()).suggests(Suggestions.TEMPLATES)
                                .executes(GenerateCommand::generateTemplate)
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(GenerateCommand::generateTemplate)
                                        .then(Commands.argument("border", BoolArgumentType.bool())
                                                .executes(GenerateCommand::generateTemplate)
                                                .then(Commands.argument("spreads", BoolArgumentType.bool())
                                                        .executes(GenerateCommand::generateTemplate))))))
                .then(Commands.literal("spread")
                        .then(Commands.argument("file", StringArgumentType.string()).suggests(Suggestions.SPREADS)
                                .executes(GenerateCommand::generateSpread)
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(GenerateCommand::generateSpread))));
    }

    private static int generateTemplate(CommandContext<CommandSourceStack> context) {
        String template = StringArgumentType.getString(context, "template");
        BlockPos pos = BlockPos.containing(context.getSource().getPosition());
        boolean border = CommandUtil.getArgumentOrDefault(context, "border", Boolean.class, false);
        boolean spreads = CommandUtil.getArgumentOrDefault(context, "spreads", Boolean.class, false);
        try {
            pos = BlockPosArgument.getBlockPos(context, "pos");
        } catch (IllegalArgumentException ignored) {}

        ServerLevel level = context.getSource().getLevel();
        ConfiguredTemplate configuredTemplate = TemplateLoader.getConfiguredTemplate(template);


        if (configuredTemplate == null) {
            context.getSource().sendFailure(Component.translatable("skyblockbuilder.command.generated.fail"));
            return 0;
        }
        if (spreads) {
            configuredTemplate.placeInWorld(level, pos, SETTINGS, level.random, Block.UPDATE_CLIENTS);
        } else {
            configuredTemplate.getTemplate().placeInWorld(level, pos, pos, SETTINGS, level.random, Block.UPDATE_CLIENTS);
        }

        if (border) {
            SkyblockSavedData.surround(level, pos, configuredTemplate);
        }

        GenerateCommand.showLocationResult(context.getSource(), template, pos);
        return 1;
    }

    private static int generateSpread(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String file = StringArgumentType.getString(context, "file");
        BlockPos pos = BlockPos.containing(context.getSource().getPosition());
        try {
            pos = BlockPosArgument.getBlockPos(context, "pos");
        } catch (IllegalArgumentException ignored) {}

        CompoundTag nbt;
        try {
            nbt = TemplateUtil.readTemplate(SkyPaths.SPREADS_DIR.resolve(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StructureTemplate template = new StructureTemplate();
        //noinspection deprecation
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);

        ServerLevel level = context.getSource().getLevel();
        template.placeInWorld(level, pos, pos, SETTINGS, level.random, Block.UPDATE_CLIENTS);
        showLocationResult(context.getSource(), file, pos);

        return 1;
    }

    private static void showLocationResult(CommandSourceStack source, String structureName, BlockPos generatedAt) {
        MutableComponent coords = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", generatedAt.getX(), generatedAt.getY(), generatedAt.getZ()).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + generatedAt.getX() + " " + generatedAt.getY() + " " + generatedAt.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))));
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.generated", structureName, coords), true);
    }
}
