package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class TeamCommand {
    public static ArgumentBuilder<CommandSource, ?> register() {
        // Let the player leave a team
        return Commands.literal("team")
                // Renaming a team
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), null))
                                .then(Commands.argument("team", StringArgumentType.word()).suggests(ManageCommand.SUGGEST_TEAMS).requires(source -> source.hasPermissionLevel(2))
                                        .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "team"))))));
    }

    private static int renameTeam(CommandSource source, String newName, String oldName) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        // Rename oldName to newName
        if (oldName != null) {
            Team team = data.getTeam(oldName);
            if (team == null) {
                source.sendFeedback(new StringTextComponent("This team does not exist.").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            data.renameTeam(team, newName);
        } else { // Get team from command user
            Entity entity = source.getEntity();
            if (!(entity instanceof ServerPlayerEntity)) {
                source.sendFeedback(new StringTextComponent("You aren't a player, what's your team?").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            Team team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendFeedback(new StringTextComponent("Currently you are in no team. Cannot change name of nothing.").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            data.renameTeam(team, newName);
        }

        source.sendFeedback(new StringTextComponent("Successfully renamed team to " + newName).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }
}
