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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class CreateCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let a player create a team if enabled in config
        return Commands.literal("create").requires(source -> LibXConfigHandler.Utility.createOwnTeam)
                .executes(context -> create(context.getSource(), null, Collections.emptyList()))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), Collections.emptyList()))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(commandSource -> commandSource.hasPermission(2))
                                .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), EntityArgument.getPlayers(context, "players")))));
    }

    private static int create(CommandSourceStack source, String name, Collection<ServerPlayer> players) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (name == null) {
            Random rand = new Random();
            do {
                name = NameGenerator.randomName(rand);
            } while (data.teamExists(name));
        }

        if (SkyblockHooks.onCreateTeam(name)) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.denied.create_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (players.isEmpty() && source.getEntity() instanceof ServerPlayer && data.hasPlayerTeam((ServerPlayer) source.getEntity())) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        Team team = data.createTeam(name);

        if (team == null) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.team_already_exist", name).withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (players.isEmpty() && source.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) source.getEntity();
            data.addPlayerToTeam(team, player);
            WorldUtil.teleportToIsland(player, team);
        } else {
            players.forEach(player -> {
                if (data.getTeamFromPlayer(player) != null) {
                    source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.player_has_team", player.getDisplayName().getString()).withStyle(ChatFormatting.RED), false);
                } else {
                    data.addPlayerToTeam(team, player);
                    WorldUtil.teleportToIsland(player, team);
                }
            });
        }

        source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.success.create_team", name).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
