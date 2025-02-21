package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class ToggleTeamSettingsCommand {

    static ArgumentBuilder<CommandSourceStack, ?> registerAllowVisit() {
        return Commands.literal("allowVisit")
                .executes(context -> ToggleTeamSettingsCommand.toggleAllowVisit(context.getSource()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> ToggleTeamSettingsCommand.toggleAllowVisit(context.getSource(), BoolArgumentType.getBool(context, "enabled"))));
    }

    static ArgumentBuilder<CommandSourceStack, ?> registerAllowRequest() {
        return Commands.literal("allowRequests")
                .executes(context -> ToggleTeamSettingsCommand.toggleAllowRequest(context.getSource()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> ToggleTeamSettingsCommand.toggleAllowRequest(context.getSource(), BoolArgumentType.getBool(context, "enabled"))));
    }

    // Visit Handling
    private static int toggleAllowVisit(CommandSourceStack source) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validatePlayerTeam(source);
        if (validationResult == null) {
            return 0;
        }

        boolean enabled = validationResult.team().allowsVisits();
        return ToggleTeamSettingsCommand.toggleAllowVisit(source, enabled, validationResult);
    }

    private static int toggleAllowVisit(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
        return ToggleTeamSettingsCommand.toggleAllowVisit(source, enabled, CommandUtil.validatePlayerTeam(source));
    }

    private static int toggleAllowVisit(CommandSourceStack source, boolean enabled, CommandUtil.ValidationResult validationResult) {
        if (validationResult == null) {
            return 0;
        }

        Pair<SkyblockManageTeamEvent.Result, Boolean> result = SkyblockHooks.onToggleVisits(validationResult.player(), validationResult.team(), enabled);
        if (result.getLeft() == SkyblockManageTeamEvent.Result.DENY) {
            source.sendFailure(SkyComponents.DENIED_TOGGLE_VISITS.apply(enabled ? SkyComponents.ARGUMENT_ENABLE : SkyComponents.ARGUMENT_DISABLE));
            return 0;
        }

        validationResult.team().setAllowVisit(result.getRight());
        source.sendSuccess(() -> SkyComponents.INFO_TOGGLE_VISIT.apply(enabled ? SkyComponents.ARGUMENT_ENABLED : SkyComponents.ARGUMENT_DISABLED).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    // Join Request Handling
    private static int toggleAllowRequest(CommandSourceStack source) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validatePlayerTeam(source);
        if (validationResult == null) {
            return 0;
        }

        boolean enabled = validationResult.team().allowsVisits();
        return ToggleTeamSettingsCommand.toggleAllowRequest(source, enabled, validationResult);
    }

    private static int toggleAllowRequest(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
        return ToggleTeamSettingsCommand.toggleAllowRequest(source, enabled, CommandUtil.validatePlayerTeam(source));
    }

    private static int toggleAllowRequest(CommandSourceStack source, boolean enabled, @Nullable CommandUtil.ValidationResult validationResult) {
        if (validationResult == null) {
            return 0;
        }

        Pair<SkyblockManageTeamEvent.Result, Boolean> result = SkyblockHooks.onToggleRequests(validationResult.player(), validationResult.team(), enabled);
        if (result.getLeft() == SkyblockManageTeamEvent.Result.DENY) {
            source.sendFailure(SkyComponents.DENIED_TOGGLE_REQUEST.apply(enabled ? SkyComponents.ARGUMENT_ENABLE : SkyComponents.ARGUMENT_DISABLE));
            return 0;
        } else {
            validationResult.team().setAllowJoinRequest(result.getRight());
            source.sendSuccess(() -> SkyComponents.INFO_TOGGLE_REQUEST.apply(enabled ? SkyComponents.ARGUMENT_ENABLED : SkyComponents.ARGUMENT_DISABLED).withStyle(ChatFormatting.GOLD), false);
            return 1;
        }
    }
}
