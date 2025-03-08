package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class CreateCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let a player create a team if enabled in config
        return Commands.literal("create").requires(source -> PermissionManager.INSTANCE.hasPermission(source, PermissionManager.Permission.TEAM_CREATE))
                .executes(context -> CreateCommand.create(context.getSource(), null))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> CreateCommand.create(context.getSource(), StringArgumentType.getString(context, "name"))));
    }

    private static int create(CommandSourceStack source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        name = CreateCommand.generateName(name, data);

        if (SkyblockHooks.onCreateTeam(name)) {
            source.sendFailure(SkyComponents.DENIED_CREATE_TEAM);
            return 0;
        }

        if (source.getEntity() instanceof ServerPlayer player && data.hasPlayerTeam(player)) {
            source.sendFailure(SkyComponents.ERROR_USER_HAS_TEAM);
            return 0;
        }

        Team team = data.createTeam(name);

        String finalName = name;
        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_TEAM_ALREADY_EXIST.apply(finalName));
            return 0;
        }

        if (source.getEntity() instanceof ServerPlayer player) {
            data.addPlayerToTeam(team, player);
            WorldUtil.teleportToIsland(player, team);

            source.sendSuccess(() -> SkyComponents.SUCCESS_CREATE_TEAM.apply(finalName), true);
            return 1;
        }

        return 0;
    }

    private static String generateName(String name, SkyblockSavedData data) {
        if (name == null) {
            Random rand = new Random();
            do {
                name = NameGenerator.randomName(rand);
            } while (data.teamExists(name));
        }

        return name;
    }
}
