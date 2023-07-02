package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ListCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Lists all teams
        return Commands.literal("list")
                .executes(context -> listTeams(context.getSource()))
                // Lists all members in team
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                        .executes(context -> listPlayers(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int listTeams(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        List<Team> teams = data.getTeams().stream().sorted(Comparator.comparing(Team::getName)).filter(team -> !team.getName().equalsIgnoreCase("spawn")).toList();
        MutableComponent info = Component.translatable("skyblockbuilder.command.info.teams",
                teams.size(),
                teams.stream().filter(Team::isEmpty).count());
        info.withStyle(ChatFormatting.GOLD);
        source.sendSuccess(() -> info, false);

        for (Team team : teams) {
            if (!team.isSpawn()) {
                MutableComponent list = (Component.literal("- " + team.getName()));
                if (team.isEmpty()) {
                    list.append(" (");
                    list.append(Component.translatable("skyblockbuilder.command.argument.empty"));
                    list.append(")");
                    list.withStyle(ChatFormatting.RED);
                } else {
                    list.withStyle(ChatFormatting.GREEN);
                }

                source.sendSuccess(() -> list, false);
            }
        }

        return 1;
    }

    private static int listPlayers(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        GameProfileCache profileCache = source.getServer().getProfileCache();
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.info.team_detailed", team.getName(), team.getPlayers().size()).withStyle(ChatFormatting.GOLD), false);
        team.getPlayers().forEach(id -> {
            Optional<GameProfile> profile = profileCache.get(id);
            if (profile.isPresent()) {
                String name = profile.get().getName();
                if (!StringUtil.isNullOrEmpty(name)) {
                    source.sendSuccess(() -> Component.literal("- " + name), false);
                }
            }
        });

        return 1;
    }
}
