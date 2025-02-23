package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TeamCommandBase {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("team")
                .then(EditTeamSpawnsCommand.register())
                .then(RenameTeamCommand.register())
                .then(ToggleTeamSettingsCommand.registerAllowVisit())
                .then(ToggleTeamSettingsCommand.registerAllowRequest())
                .then(JoinRequestHandlingCommand.registerAccept())
                .then(JoinRequestHandlingCommand.registerDeny());
    }
}
