package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SpawnCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Teleports the player to spawn
        return Commands.literal("spawn").requires(source -> PermissionsConfig.Teleports.spawn || source.hasPermission(2)) // todo 1.21 check on execution
                .executes(context -> spawn(context.getSource()));
    }

    private static int spawn(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getSpawn();

        if (!player.hasPermissions(2) && !data.getOrCreateMetaInfo(player).canTeleportSpawn(level.getGameTime())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.cooldown",
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.spawnCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastSpawnTeleport()))));
            return 0;
        }

        // todo 1.21 simplify this "player.hasPermission"
        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().location())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleportation_not_allowed_dimension"));
            return 0;
        }

        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.crossDimensionTeleportation && player.level() != data.getLevel()) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleport_across_dimensions"));
            return 0;
        }

        if (!player.hasPermissions(2) && PermissionsConfig.Teleports.preventWhileFalling && player.fallDistance > 1) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.prevent_while_falling"));
            return 0;
        }

        data.getOrCreateMetaInfo(player).setLastSpawnTeleport(level.getGameTime());
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.teleport_to_spawn"), false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
