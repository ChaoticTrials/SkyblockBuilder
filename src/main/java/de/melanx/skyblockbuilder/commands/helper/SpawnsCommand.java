package de.melanx.skyblockbuilder.commands.helper;

import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.moddingx.libx.command.EnumArgumentIgnoreCase;
import org.moddingx.libx.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> showSpawns(context.getSource(), Mode.NORMAL))
                // use debug for setting up a new spawn points as pack author
                .then(Commands.argument("mode", EnumArgumentIgnoreCase.enumArgument(Mode.class)).requires(source -> source.hasPermission(2))
                        .executes(context -> showSpawns(context.getSource(), context.getArgument("mode", Mode.class))));
    }

    @SuppressWarnings("SameReturnValue")
    private static int showSpawns(CommandSourceStack source, Mode mode) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (mode.isExporting()) {
            Team team = null;
            if (source.getEntity() instanceof ServerPlayer) {
                team = data.getTeamFromPlayer(((Player) source.getEntity()));
            }

            if (team == null) {
                team = data.getSpawn();
            }

            if (mode == Mode.EXPORT) {
                try {
                    Files.createDirectories(SkyPaths.MOD_EXPORTS);
                } catch (IOException e) {
                    throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_path", SkyPaths.MOD_EXPORTS)).create();
                }
                Path filePath = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, "spawns", "json");

                JsonObject json = TemplateUtil.possibleSpawnsAsJson(team);

                Path file = SkyPaths.MOD_EXPORTS.resolve(filePath.getFileName());
                try {
                    Files.writeString(file, SkyblockBuilder.PRETTY_GSON.toJson(json));
                } catch (IOException e) {
                    throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_file", file)).create();
                }

                source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.export_spawns", filePath).withStyle(ChatFormatting.GOLD), true);
            } else if (mode == Mode.EXPORT_TO_CONFIG) {
                JsonObject json = TemplateUtil.possibleSpawnsAsJson(team);

                Path configFile = SkyPaths.MOD_CONFIG.resolve("templates.json5");
                try {
                    JsonObject config = SkyblockBuilder.PRETTY_GSON.fromJson(Files.readString(configFile), JsonObject.class);
                    if (!config.has("spawns")) {
                        config.add("spawns", new JsonObject());
                    }

                    JsonObject spawns = config.get("spawns").getAsJsonObject();
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                    String formattedDate = dateFormat.format(calendar.getTime());
                    spawns.add("exported_at_" + formattedDate, json);
                    config.add("spawns", spawns);

                    Files.writeString(configFile, SkyblockBuilder.PRETTY_GSON.toJson(config));
                    ConfigManager.reloadConfig(TemplatesConfig.class);
                    if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                        ConfigManager.synchronize(level.getServer(), TemplatesConfig.class);
                    }

                    source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.export_spawns_to_config", formattedDate).withStyle(ChatFormatting.GOLD), true);
                } catch (IOException e) {
                    throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.overwrite_config", configFile.getFileName())).create();
                }
            }

            return 1;
        }

        Team team = data.getSpawn();
        if (source.getEntity() instanceof ServerPlayer player) {
            team = data.getTeamFromPlayer(player);
            if (team == null) {
                team = data.getSpawn();
            }
        }

        boolean showDefaultSpawns = team.isSpawn() && mode == Mode.NORMAL;
        Set<TemplatesConfig.Spawn> spawns = showDefaultSpawns ? team.getDefaultPossibleSpawns() : team.getPossibleSpawns();
        if (!spawns.isEmpty()) {
            source.sendSystemMessage(Component.translatable("skyblockbuilder.command.info.show_team_spawns", team.getName()).withStyle(ChatFormatting.GOLD));
        }

        for (TemplatesConfig.Spawn spawn : spawns) {
            BlockPos pos = spawn.pos();
            source.sendSystemMessage(Component.literal(" - ").append(RandomUtility.getFormattedPos(pos)));
            if (source.getEntity() instanceof ServerPlayer player) {
                level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
            } else {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
            }
        }

        return 1;
    }

    public enum Mode {
        NORMAL,
        DEBUG,
        EXPORT,
        EXPORT_TO_CONFIG;

        public boolean isExporting() {
            return this == EXPORT || this == EXPORT_TO_CONFIG;
        }
    }
}
