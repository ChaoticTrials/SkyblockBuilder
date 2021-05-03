package de.melanx.skyblockbuilder.commands.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.LibXConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import io.github.noeppi_noeppi.libx.command.UppercaseEnumArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> showSpawns(context.getSource(), Mode.NORMAL))
                // use debug for setting up a new spawn points as pack author
                .then(Commands.argument("mode", UppercaseEnumArgument.enumArgument(Mode.class)).requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> showSpawns(context.getSource(), context.getArgument("mode", Mode.class))));
    }

    private static int showSpawns(CommandSource source, Mode mode) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (mode == Mode.EXPORT) {
            Team team = null;
            if (source.getEntity() instanceof ServerPlayerEntity) {
                team = data.getTeamFromPlayer(((PlayerEntity) source.getEntity()));
            }

            if (team == null) {
                team = data.getSpawn();
            }

            String folderName = "skyblock_exports";
            try {
                Files.createDirectories(Paths.get(folderName));
            } catch (IOException e) {
                throw new SimpleCommandExceptionType(new TranslationTextComponent("skyblockbuilder.command.error.creating_path", folderName)).create();
            }
            String filePath = RandomUtility.getFilePath(folderName, "spawns", "json");

            JsonObject json = new JsonObject();
            JsonArray spawns = new JsonArray();
            Set<BlockPos> possibleSpawns = team.getPossibleSpawns();
            for (BlockPos pos : possibleSpawns) {
                JsonArray arr = new JsonArray();
                arr.add(pos.getX() % LibXConfigHandler.World.islandDistance);
                arr.add(pos.getY() - LibXConfigHandler.Spawn.height);
                arr.add(pos.getZ() % LibXConfigHandler.World.islandDistance);
                spawns.add(arr);
            }

            json.add("islandSpawns", spawns);
            Path file = Paths.get(folderName).resolve(filePath.split("/")[1]);
            try {
                BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                w.write(SkyblockBuilder.PRETTY_GSON.toJson(json));
                w.close();
            } catch (IOException e) {
                throw new SimpleCommandExceptionType(new TranslationTextComponent("skyblockbuilder.command.error.creating_file", file)).create();
            }

            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.export_spawns", filePath).mergeStyle(TextFormatting.GOLD), true);
            return 1;
        }

        for (Team team : data.getTeams()) {
            IslandPos spawn = team.getIsland();
            Set<BlockPos> posSet = mode == Mode.NORMAL ? SkyblockSavedData.initialPossibleSpawns(spawn.getCenter()) : team.getPossibleSpawns();
            for (BlockPos pos : posSet) {
                if (source.getEntity() instanceof ServerPlayerEntity) {
                    world.spawnParticle(source.asPlayer(), ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
                } else {
                    world.spawnParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
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
