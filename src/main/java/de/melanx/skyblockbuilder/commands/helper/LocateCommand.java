package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import org.moddingx.libx.command.CommandUtil;

import java.util.Set;

public class LocateCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("locate")
                .requires(PermissionManager.INSTANCE::mayExecuteOpCommand)
                .then(Commands.literal("spread")
                        .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                .executes(LocateCommand::locateAllSpreads)
                                .then(Commands.argument("spread", StringArgumentType.string()).suggests(Suggestions.SPREADS_FOR_TEAM)
                                        .executes(LocateCommand::locateSpread))));
    }

    private static int locateSpread(CommandContext<CommandSourceStack> context) {
        String spreadName = CommandUtil.getArgumentOrDefault(context, "spread", String.class, "Spread");

        Team team = LocateCommand.getTeam(context);
        if (team == null) {
            return 0;
        }

        if (team.getPlacedSpreads(spreadName).isEmpty()) {
            context.getSource().sendFailure(SkyComponents.ERROR_SPREAD_NOT_EXIST);
            return 0;
        }

        LocateCommand.sendLocations(context.getSource(), team, spreadName);
        return 1;
    }

    private static int locateAllSpreads(CommandContext<CommandSourceStack> context) {
        Team team = LocateCommand.getTeam(context);
        if (team == null) {
            return 0;
        }

        Set<String> spreadNames = team.getAllSpreadNames();
        if (spreadNames.isEmpty()) {
            context.getSource().sendFailure(SkyComponents.ERROR_NO_SPREADS);
            return 0;
        }

        spreadNames.forEach(spreadName -> LocateCommand.sendLocations(context.getSource(), team, spreadName));

        return spreadNames.size();
    }

    private static void sendLocations(CommandSourceStack source, Team team, String spreadName) {
        source.sendSuccess(() -> {
            MutableComponent msg = SkyComponents.SUCCESS_LOCATED_SPREAD.apply(spreadName);
            for (Team.PlacedSpread spread : team.getPlacedSpreads(spreadName)) {
                msg.append("\n- ");
                msg.append(RandomUtility.getFormattedPos(spread.pos()));
            }

            return msg;
        }, true);
    }

    private static Team getTeam(CommandContext<CommandSourceStack> context) {
        String teamName = CommandUtil.getArgumentOrDefault(context, "team", String.class, "Spawn");
        ServerLevel level = context.getSource().getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeam(teamName);
        if (team == null) {
            context.getSource().sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
        }

        return team;
    }
}
