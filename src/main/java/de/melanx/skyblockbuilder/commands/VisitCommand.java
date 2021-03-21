package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockVisitEvent;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event;

import java.util.stream.Collectors;

public class VisitCommand {
    
    public static final SuggestionProvider<CommandSource> SUGGEST_VISIT_TEAMS = (context, builder) -> ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
            .getTeams().stream().filter(Team::allowsVisits).map(Team::getName).filter(name -> !name.equalsIgnoreCase("spawn")).collect(Collectors.toSet()), builder);

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Let the player visit another team
        return Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_VISIT_TEAMS)
                        .executes(context -> visit(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int visit(CommandSource source, String name) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeam(name);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("What do you want to visit? This team does not exist!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        switch (SkyblockHooks.onVisit(player, team)) {
            case DENY:
                source.sendFeedback(new StringTextComponent("This team don't want visitors, sorry.").mergeStyle(TextFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (team.hasPlayer(player)) {
                    source.sendFeedback(new StringTextComponent("You can not visit your own team.").mergeStyle(TextFormatting.RED), false);
                    return 0;
                }
                if (!player.hasPermissionLevel(2)) {
                    if (!ConfigHandler.allowVisits.get()) {
                        source.sendFeedback(new StringTextComponent("Team visits are disabled.").mergeStyle(TextFormatting.RED), false);
                        return 0;
                    }
                    if (!team.allowsVisits()) {
                        source.sendFeedback(new StringTextComponent("This team don't want visitors, sorry.").mergeStyle(TextFormatting.RED), false);
                        return 0;
                    }
                }
                break;
            case ALLOW:
                break;
        }
        
        WorldUtil.teleportToIsland(player, team);
        source.sendFeedback(new StringTextComponent(String.format("You're now a visitor of team %s.", name)).mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }
}
