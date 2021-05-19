package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class CreateCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Let a player create a team if enabled in config
        return Commands.literal("create").requires(source -> LibXConfigHandler.Utility.createOwnTeam)
                .executes(context -> create(context.getSource(), null, Collections.emptyList()))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), Collections.emptyList()))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                                .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), EntityArgument.getPlayers(context, "players")))));
    }

    private static int create(CommandSource source, String name, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (name == null) {
            Random rand = new Random();
            do {
                name = NameGenerator.randomName(rand);
            } while (data.teamExists(name));
        }

        if (SkyblockHooks.onCreateTeam(name)) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.create_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (players.isEmpty() && source.getEntity() instanceof ServerPlayerEntity && data.hasPlayerTeam((ServerPlayerEntity) source.getEntity())) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        Team team = data.createTeam(name);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.team_already_exist", name).mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (players.isEmpty() && source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
            data.addPlayerToTeam(team, player);
            WorldUtil.teleportToIsland(player, team);
        } else {
            players.forEach(player -> {
                if (data.getTeamFromPlayer(player) != null) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.player_has_team", player.getDisplayName().getString()).mergeStyle(TextFormatting.RED), true);
                } else {
                    data.addPlayerToTeam(team, player);
                    WorldUtil.teleportToIsland(player, team);
                }
            });
        }

        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.create_team", name).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }
}
