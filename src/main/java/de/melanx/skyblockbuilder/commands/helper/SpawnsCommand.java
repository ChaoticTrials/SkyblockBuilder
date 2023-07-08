package de.melanx.skyblockbuilder.commands.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.moddingx.libx.command.EnumArgument2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> showSpawns(context.getSource(), Mode.NORMAL))
                // use debug for setting up a new spawn points as pack author
                .then(Commands.argument("mode", EnumArgument2.enumArgument(Mode.class)).requires(source -> source.hasPermission(2))
                        .executes(context -> showSpawns(context.getSource(), context.getArgument("mode", Mode.class))));
    }

    private static int showSpawns(CommandSourceStack source, Mode mode) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (mode == Mode.EXPORT) {
            Team team = null;
            if (source.getEntity() instanceof ServerPlayer) {
                team = data.getTeamFromPlayer(((Player) source.getEntity()));
            }

            if (team == null) {
                team = data.getSpawn();
            }

            String folderName = "skyblock_exports";
            try {
                Files.createDirectories(Paths.get(folderName));
            } catch (IOException e) {
                throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_path", folderName)).create();
            }
            String filePath = RandomUtility.getFilePath(folderName, "spawns", "json");

            JsonObject json = new JsonObject();
            JsonArray spawns = new JsonArray();
            Set<BlockPos> possibleSpawns = team.getPossibleSpawns();
            for (BlockPos pos : possibleSpawns) {
                JsonArray arr = new JsonArray();
                arr.add(pos.getX() % WorldConfig.islandDistance);
                arr.add(pos.getY() - team.getIsland().getCenter().getY());
                arr.add(pos.getZ() % WorldConfig.islandDistance);
                spawns.add(arr);
            }

            json.add("islandSpawns", spawns);
            Path file = Paths.get(folderName).resolve(filePath.split("/")[1]);
            try {
                BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                w.write(SkyblockBuilder.PRETTY_GSON.toJson(json));
                w.close();
            } catch (IOException e) {
                throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_file", file)).create();
            }

            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.export_spawns", filePath).withStyle(ChatFormatting.GOLD), true);
            return 1;
        }

        for (Team team : data.getTeams()) {
            Set<BlockPos> posSet = mode == Mode.NORMAL ? team.getDefaultPossibleSpawns() : team.getPossibleSpawns();
            for (BlockPos pos : posSet) {
                if (source.getEntity() instanceof ServerPlayer) {
                    level.sendParticles(source.getPlayerOrException(), ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
                } else {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
                }
            }
        }

        return 1;
    }

    public enum Mode {
        NORMAL,
        DEBUG,
        EXPORT
    }
}
