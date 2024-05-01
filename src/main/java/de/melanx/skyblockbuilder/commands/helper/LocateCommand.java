package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import org.moddingx.libx.command.CommandUtil;

import java.util.Set;

public class LocateCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("locate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spread")
                        .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                .then(Commands.argument("spread", StringArgumentType.string()).suggests(Suggestions.SPREADS_FOR_TEAM)
                                        .executes(LocateCommand::locateSpread))));
    }

    private static int locateSpread(CommandContext<CommandSourceStack> context) {
        String teamName = CommandUtil.getArgumentOrDefault(context, "team", String.class, "Spawn");
        String spreadName = CommandUtil.getArgumentOrDefault(context, "spread", String.class, "Spread");
        ServerLevel level = context.getSource().getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeam(teamName);
        if (team == null || team.isSpawn()) {
            context.getSource().sendFailure(Component.translatable("skyblockbuilder.command.error.team_not_exist"));
            return 0;
        }

        Set<Team.PlacedSpread> placedSpreads = team.getPlacedSpreads(spreadName);
        if (spreadName == null || placedSpreads.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("skyblockbuilder.command.error.spread_not_exist"));
            return 0;
        }

        context.getSource().sendSuccess(() -> {
            MutableComponent msg = Component.translatable("skyblockbuilder.command.success.located_spread", spreadName);
            for (Team.PlacedSpread spread : placedSpreads) {
                msg.append("\n- ");
                msg.append(RandomUtility.getFormattedPos(spread.pos()));
            }

            return msg;
        }, true);
        return 1;
    }
}
