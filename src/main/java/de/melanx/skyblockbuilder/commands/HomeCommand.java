package de.melanx.skyblockbuilder.commands;

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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class HomeCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Teleports the player back home
        return Commands.literal("home")
                .executes(context -> home(context.getSource()));
    }

    private static int home(CommandSourceStack source) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validatePlayerTeam(source);
        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        Level level = player.level();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = validationResult.team();
        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_HOME) && !data.getOrCreateMetaInfo(player).canTeleport(SkyMeta.TeleportType.HOME, level.getGameTime())) {
            source.sendFailure(SkyComponents.ERROR_COOLDOWN.apply(
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.Cooldowns.homeCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastTeleport(SkyMeta.TeleportType.HOME)))
            ));
            return 0;
        }

        if (CommandUtil.mayNotTeleport(source, data, player)) {
            return 0;
        }

        switch (SkyblockHooks.onHome(player, team)) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_TELEPORT_HOME);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_HOME)) {
                    source.sendFailure(SkyComponents.DISABLED_TELEPORT_HOME);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.getOrCreateMetaInfo(player).setLastTeleport(SkyMeta.TeleportType.HOME, level.getGameTime());
        source.sendSuccess(() -> SkyComponents.SUCCESS_TELEPORT_HOME.withStyle(ChatFormatting.GOLD), true);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
