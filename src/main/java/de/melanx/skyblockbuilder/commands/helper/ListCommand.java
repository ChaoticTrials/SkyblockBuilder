package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
       // Lists all teams
        return Commands.literal("list")
                .executes(context -> listTeams(context.getSource()))
                 // Lists all members in team
                .then(Commands.argument("team", StringArgumentType.word()).suggests(ManageCommand.SUGGEST_TEAMS)
                        .executes(context -> listPlayers(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int listTeams(CommandSource source) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        List<Team> teams = data.getTeams().stream().sorted(Comparator.comparing(Team::getName)).filter(team -> !team.getName().equalsIgnoreCase("spawn")).collect(Collectors.toList());
        IFormattableTextComponent info = new TranslationTextComponent("skyblockbuilder.command.info.teams",
                teams.size(),
                teams.stream().filter(Team::isEmpty).count());
        info.mergeStyle(TextFormatting.GOLD);
        source.sendFeedback(info, true);

        for (Team team : teams) {
            if (!team.getName().equalsIgnoreCase("spawn")) {
                IFormattableTextComponent list = (new StringTextComponent("- " + team.getName()));
                if (team.isEmpty()) {
                    list.appendString(" (");
                    list.append(new TranslationTextComponent("skyblockbuilder.command.argument.empty"));
                    list.appendString(")");
                    list.mergeStyle(TextFormatting.RED);
                } else {
                    list.mergeStyle(TextFormatting.GREEN);
                }

                source.sendFeedback(list, true);
            }
        }

        return 1;
    }

    private static int listPlayers(CommandSource source, String teamName) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.team_not_exist").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        PlayerProfileCache profileCache = source.getServer().getPlayerProfileCache();
        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.info.team_detailed", team.getName(), team.getPlayers().size()).mergeStyle(TextFormatting.GOLD), true);
        team.getPlayers().forEach(id -> {
            GameProfile profile = profileCache.getProfileByUUID(id);
            if (profile != null) {
                String name = profile.getName();
                if (!StringUtils.isNullOrEmpty(name)) {
                    source.sendFeedback(new StringTextComponent("- " + name), true);
                }
            }
        });

        return 1;
    }
}
