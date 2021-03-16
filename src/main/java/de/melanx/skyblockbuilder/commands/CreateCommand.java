package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class CreateCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Let a player create a team if enabled in config
        return Commands.literal("create").requires(source -> ConfigHandler.createOwnTeam.get())
                .executes(context -> create(context.getSource(), null, Collections.emptyList()))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), Collections.emptyList()))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                                .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), EntityArgument.getPlayers(context, "players")))));
    }

    private static int create(CommandSource source, String name, Collection<ServerPlayerEntity> players) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (name == null) {
            Random rand = new Random();
            do {
                name = NameGenerator.randomName(rand);
            } while (data.teamExists(name));
        }

        Team team = data.createTeam(name);

        if (team == null) {
            source.sendFeedback(new StringTextComponent(String.format("Team %s already exists! Please choose another name!", name)).mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (players.isEmpty() && source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
            team.addPlayer(player);
            WorldUtil.teleportToIsland(player, team);
        } else {
            players.forEach(player -> {
                if (data.getTeamFromPlayer(player) != null) {
                    source.sendFeedback(new StringTextComponent(String.format("%s is already in a team, it cannot be added!", player.getDisplayName().getString())).mergeStyle(TextFormatting.RED), false);
                } else {
                    team.addPlayer(player);
                    WorldUtil.teleportToIsland(player, team);
                }
            });
        }

        source.sendFeedback(new StringTextComponent(String.format(("Successfully created team %s."), name)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }
}
