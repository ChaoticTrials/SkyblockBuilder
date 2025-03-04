package de.melanx.skyblockbuilder.commands.teleport;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SpawnCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Teleports the player to spawn
        return Commands.literal("spawn")
                .executes(context -> spawn(context.getSource()));
    }

    private static int spawn(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getSpawn();

        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_SPAWN) && !data.getOrCreateMetaInfo(player).canTeleport(SkyMeta.TeleportType.SPAWN, level.getGameTime())) {
            source.sendFailure(SkyComponents.ERROR_COOLDOWN.apply(
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.Cooldowns.spawnCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastTeleport(SkyMeta.TeleportType.SPAWN)))
            ));
            return 0;
        }

        if (CommandUtil.mayNotTeleport(source, data, player)) {
            return 0;
        }

        switch (SkyblockHooks.onTeleportToSpawn(player, team)) {
            case DENY -> {
                source.sendFailure(SkyComponents.DENIED_TELEPORT_TO_SPAWN);
                return 0;
            }
            case DEFAULT -> {
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_SPAWN)) {
                    source.sendFailure(SkyComponents.DISABLED_TELEPORT_SPAWN);
                    return 0;
                }
            }
            case ALLOW -> {}
        }

        data.getOrCreateMetaInfo(player).setLastTeleport(SkyMeta.TeleportType.SPAWN, level.getGameTime());
        source.sendSuccess(() -> SkyComponents.SUCCESS_TELEPORT_TO_SPAWN, false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
