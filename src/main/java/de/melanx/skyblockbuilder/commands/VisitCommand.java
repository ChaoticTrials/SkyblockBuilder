package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class VisitCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Let the player visit another team
        return Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(Suggestions.VISIT_TEAMS)
                        .executes(context -> visit(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int visit(CommandSource source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeam(name);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.team_not_exist").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        switch (SkyblockHooks.onVisit(player, team)) {
            case DENY:
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.visit_team").mergeStyle(TextFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (team.hasPlayer(player)) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.visit_own_team").mergeStyle(TextFormatting.RED), false);
                    return 0;
                }
                if (!player.hasPermissionLevel(2)) {
                    if (!LibXConfigHandler.Utility.Teleports.allowVisits) {
                        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.team_visit").mergeStyle(TextFormatting.RED), false);
                        return 0;
                    }
                    if (!team.allowsVisits()) {
                        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.visit_team").mergeStyle(TextFormatting.RED), false);
                        return 0;
                    }
                }
                break;
            case ALLOW:
                break;
        }

        WorldUtil.teleportToIsland(player, team);
        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.visit_team", name).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }
}
