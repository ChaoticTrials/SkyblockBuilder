package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class VisitCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let the player visit another team
        return Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.VISIT_TEAMS)
                        .executes(context -> visit(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int visit(CommandSourceStack source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeam(name);

        if (team == null) {
            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (!player.hasPermissions(2) && !data.getOrCreateMetaInfo(player).canVisit(level.getGameTime())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.cooldown",
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.visitCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastVisitTeleport()))));
            return 0;
        }

        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().location())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleportation_not_allowed_dimension"));
            return 0;
        }

        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.crossDimensionTeleportation && player.level() != data.getLevel()) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleport_across_dimensions"));
            return 0;
        }

        switch (SkyblockHooks.onVisit(player, team)) {
            case DENY:
                source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.disabled.visit_team").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (team.hasPlayer(player)) {
                    source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.visit_own_team").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                if (!player.hasPermissions(2)) {
                    if (!PermissionsConfig.Teleports.allowVisits) {
                        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.disabled.team_visit").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                    if (!team.allowsVisits()) {
                        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.disabled.visit_team").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                }
                break;
            case ALLOW:
                break;
        }

        WorldUtil.teleportToIsland(player, team);
        data.getOrCreateMetaInfo(player).setLastVisitTeleport(level.getGameTime());
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.visit_team", name).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
