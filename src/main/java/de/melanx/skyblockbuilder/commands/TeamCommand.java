package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.world.data.Teams;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class TeamCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("team")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name")))));
    }

    private static int createTeam(CommandSource source, String name) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        Teams teams = Teams.get(world);
        ServerPlayerEntity player = source.asPlayer();
        if (teams.hasTeam(player)) {
            player.sendStatusMessage(new StringTextComponent("You're already in a team."), false);
        } else if (teams.exists(name)) {
            player.sendStatusMessage(new StringTextComponent("Team " + name + " already exists. Please choose another name."), false);
        } else {
            teams.add(name, player.getUniqueID());
            player.sendStatusMessage(new StringTextComponent("Successfully created and joined team " + name), false);
            return 1;
        }
        return 0;
    }
}
