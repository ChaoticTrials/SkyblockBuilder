package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class CreateCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("create")
                .executes(context -> create(context.getSource(), NameGenerator.randomName(new Random()), Arrays.asList(context.getSource().asPlayer())))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), Arrays.asList(context.getSource().asPlayer())))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                                .executes(context -> create(context.getSource(), StringArgumentType.getString(context, "name"), EntityArgument.getPlayers(context, "players")))));
    }


    private static int create(CommandSource source, String name, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (data.teamExists(name)) {
            source.sendFeedback(new StringTextComponent(String.format("Team %s already exists! Please choose another name!", name)).mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        Team team = data.createTeam(name);

        players.forEach(serverPlayerEntity -> {
            if (data.getTeamFromPlayer(serverPlayerEntity) != null) {
                source.sendFeedback(new StringTextComponent(String.format("%s is already in a team, it can not be added!", serverPlayerEntity.getDisplayName().getString())).mergeStyle(TextFormatting.RED), false);
            } else {
                team.addPlayer(serverPlayerEntity);
                WorldUtil.teleportToIsland(serverPlayerEntity, team.getIsland());
            }
        });


        source.sendFeedback(new StringTextComponent(String.format(("Successfully created team %s."), name)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }
}
