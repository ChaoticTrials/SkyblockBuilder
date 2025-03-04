package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moddingx.libx.fi.Function3;
import org.moddingx.libx.fi.Function4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SkyComponents {

    private static final Logger logger = LoggerFactory.getLogger(SkyComponents.class);
    private static final Set<String> untranslatedKeys = new HashSet<>();
    private static boolean LANGUAGE_LOADED = false;
    private static final HashMap<String, String> languageMap = new HashMap<>();

    private SkyComponents() {}

    private static void loadLanguage() {
        if (LANGUAGE_LOADED) {
            return;
        }

        LANGUAGE_LOADED = true;
        InputStream inputStream = SkyblockBuilder.class.getResourceAsStream("/assets/skyblockbuilder/lang/en_us.json");
        if (inputStream != null) {
            Language.loadFromJson(inputStream, languageMap::put);
        }
    }

    private static void logMissingKey(String key) {
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            return;
        }

        loadLanguage();
        if (languageMap.containsKey(key)) {
            return;
        }


        if (!untranslatedKeys.contains(key)) {
            untranslatedKeys.add(key);
            throw new IllegalArgumentException("Missing translation for key: " + key);
        }
    }

    public static MutableComponent translatable(String key, Object... args) {
        String fullKey = "skyblockbuilder." + key;
        logMissingKey(fullKey);
        return Component.translatable(fullKey, args);
    }

    public static MutableComponent compat(String modid, String key, Object... args) {
        String fullKey = modid + ".skyblockbuilder." + key;
        logMissingKey(fullKey);
        return Component.translatable(fullKey, args);
    }

    public static MutableComponent command(String key, Object... args) {
        return SkyComponents.translatable("command." + key, args);
    }

    public static MutableComponent argument(String key, Object... args) {
        return SkyComponents.command("argument." + key, args);
    }

    public static MutableComponent generated(String key, Object... args) {
        return SkyComponents.command("generated." + key, args);
    }

    public static MutableComponent denied(String key, Object... args) {
        return SkyComponents.command("denied." + key, args);
    }

    public static MutableComponent disabled(String key, Object... args) {
        return SkyComponents.command("disabled." + key, args);
    }

    public static MutableComponent error(String key, Object... args) {
        return SkyComponents.command("error." + key, args);
    }

    public static MutableComponent info(String key, Object... args) {
        return SkyComponents.command("info." + key, args);
    }

    public static MutableComponent success(String key, Object... args) {
        return SkyComponents.command("success." + key, args).withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent warning(String key, Object... args) {
        return SkyComponents.command("warning." + key, args);
    }

    public static MutableComponent event(String key, Object... args) {
        return SkyComponents.translatable("event." + key, args);
    }

    public static MutableComponent structureSaver(String key, Object... args) {
        return SkyComponents.translatable("item.structure_saver." + key, args);
    }

    public static MutableComponent structureSaverTooltip(String key, Object... args) {
        return SkyComponents.structureSaver(key + ".tooltip", args);
    }

    public static MutableComponent structureSaverDescription(String key, Object... args) {
        return SkyComponents.structureSaver(key + ".desc", args);
    }

    public static MutableComponent screen(String key, Object... args) {
        return SkyComponents.translatable("screen." + key, args);
    }

    public static MutableComponent screenTitle(String key, Object... args) {
        return SkyComponents.screen(key + ".title", args);
    }

    public static MutableComponent button(String key, Object... args) {
        return SkyComponents.screen("button." + key, args);
    }

    public static MutableComponent widget(String key, Object... args) {
        return SkyComponents.screen("widget." + key, args);
    }

    public static MutableComponent dump(String key, Object... args) {
        return SkyComponents.screen("dump." + key, args);
    }

    public static MutableComponent dumpText(String key, Object... args) {
        return SkyComponents.dump("text." + key, args);
    }

    public static MutableComponent errorScreen(String key, Object... args) {
        return SkyComponents.screen("error." + key, args);
    }

    // Command Arguments
    public static final MutableComponent ARGUMENT_DISABLE = SkyComponents.argument("disable");
    public static final MutableComponent ARGUMENT_DISABLED = SkyComponents.argument("disabled");
    public static final MutableComponent ARGUMENT_EMPTY = SkyComponents.argument("empty");
    public static final MutableComponent ARGUMENT_ENABLE = SkyComponents.argument("enable");
    public static final MutableComponent ARGUMENT_ENABLED = SkyComponents.argument("enabled");

    // Command Basic
    public static final BiFunction<String, Component, MutableComponent> COMMAND_GENERATED_SUCCESS = (structureName, formattedCoords) -> SkyComponents.generated("success", structureName, formattedCoords);
    public static final MutableComponent COMMAND_GENERATED_FAIL = SkyComponents.generated("fail");

    // Command Denied
    public static final MutableComponent DENIED_ACCEPT_INVITATIONS = SkyComponents.denied("accept_invitations");
    public static final MutableComponent DENIED_ACCEPT_JOIN_REQUEST = SkyComponents.denied("accept_join_request");
    public static final MutableComponent DENIED_ADD_PLAYERS_TO_TEAM = SkyComponents.denied("add_players_to_team");
    public static final MutableComponent DENIED_CLEAR_TEAM = SkyComponents.denied("clear_team");
    public static final MutableComponent DENIED_CREATE_SPAWN = SkyComponents.denied("create_spawn");
    public static final MutableComponent DENIED_CREATE_TEAM = SkyComponents.denied("create_team");
    public static final MutableComponent DENIED_DECLINE_INVITATIONS = SkyComponents.denied("decline_invitations");
    public static final MutableComponent DENIED_DELETE_TEAM = SkyComponents.denied("delete_team");
    public static final MutableComponent DENIED_DENY_JOIN_REQUEST = SkyComponents.denied("deny_join_request");
    public static final MutableComponent DENIED_INVITE_PLAYER = SkyComponents.denied("invite_player");
    public static final MutableComponent DENIED_JOIN_REQUEST = SkyComponents.denied("join_request");
    public static final MutableComponent DENIED_LEAVE_TEAM = SkyComponents.denied("leave_team");
    public static final MutableComponent DENIED_MODIFY_SPAWNS0 = SkyComponents.denied("modify_spawns0");
    public static final MutableComponent DENIED_MODIFY_SPAWNS1 = SkyComponents.denied("modify_spawns1");
    public static final MutableComponent DENIED_REMOVE_PLAYERS_FROM_TEAM = SkyComponents.denied("remove_players_from_team");
    public static final MutableComponent DENIED_RENAME_TEAM = SkyComponents.denied("rename_team");
    public static final MutableComponent DENIED_RESET_SPAWNS = SkyComponents.denied("reset_spawns");
    public static final MutableComponent DENIED_TELEPORT_HOME = SkyComponents.denied("teleport_home");
    public static final MutableComponent DENIED_VISIT_TEAM = SkyComponents.denied("visit_team");
    public static final MutableComponent DENIED_TELEPORT_TO_SPAWN = SkyComponents.denied("teleport_to_spawn");
    public static final Function<Component, MutableComponent> DENIED_TOGGLE_REQUEST = arg -> SkyComponents.denied("toggle_request", arg);
    public static final Function<Component, MutableComponent> DENIED_TOGGLE_VISITS = arg -> SkyComponents.denied("toggle_visits", arg);

    // Command Disabled
    public static final MutableComponent DISABLED_ACCEPT_INVITATIONS = SkyComponents.disabled("accept_invitations");
    public static final MutableComponent DISABLED_ACCEPT_JOIN_REQUEST = SkyComponents.disabled("accept_join_request");
    public static final MutableComponent DISABLED_DECLINE_INVITATIONS = SkyComponents.disabled("decline_invitations");
    public static final MutableComponent DISABLED_DENY_JOIN_REQUEST = SkyComponents.disabled("deny_join_request");
    public static final MutableComponent DISABLED_JOIN_REQUEST = SkyComponents.disabled("join_request");
    public static final MutableComponent DISABLED_MANAGE_TEAMS = SkyComponents.disabled("manage_teams");
    public static final MutableComponent DISABLED_MODIFY_SPAWNS = SkyComponents.disabled("modify_spawns");
    public static final MutableComponent DISABLED_RENAME_TEAM = SkyComponents.disabled("rename_team");
    public static final MutableComponent DISABLED_SEND_INVITATIONS = SkyComponents.disabled("send_invitations");
    public static final MutableComponent DISABLED_TEAM_JOIN_REQUEST = SkyComponents.disabled("team_join_request");
    public static final MutableComponent DISABLED_TEAM_VISIT = SkyComponents.disabled("team_visit");
    public static final MutableComponent DISABLED_TELEPORT_SPAWN = SkyComponents.disabled("teleport_spawn");
    public static final MutableComponent DISABLED_TELEPORT_HOME = SkyComponents.disabled("teleport_home");
    public static final MutableComponent DISABLED_VISIT_TEAM = SkyComponents.disabled("visit_team");

    // Command Errors
    public static final MutableComponent ERROR_ACCEPT_INVITATIONS = SkyComponents.error("accept_invitations");
    public static final Function<String, MutableComponent> ERROR_COOLDOWN = formattedCooldown -> SkyComponents.error("cooldown", formattedCooldown);
    public static final Function<String, MutableComponent> ERROR_CREATING_FILE = path -> SkyComponents.error("creating_file", path);
    public static final Function<String, MutableComponent> ERROR_CREATING_PATH = path -> SkyComponents.error("creating_path", path);
    public static final MutableComponent ERROR_DECLINE_INVITATIONS = SkyComponents.error("decline_invitations");
    public static final Function<String, MutableComponent> ERROR_DELETE_TEAM = teamName -> SkyComponents.error("delete_team", teamName);
    public static final MutableComponent ERROR_NO_INVITATIONS = SkyComponents.error("no_invitations");
    public static final MutableComponent ERROR_NO_PLAYER_ADDED = SkyComponents.error("no_player_added");
    public static final MutableComponent ERROR_NO_SPREADS = SkyComponents.error("no_spreads");
    public static final MutableComponent ERROR_PLAYER_ALREADY_INVITED = SkyComponents.error("player_already_invited");
    public static final MutableComponent ERROR_PLAYER_HAS_NO_TEAM = SkyComponents.error("player_has_no_team");
    public static final Function<String, MutableComponent> ERROR_PLAYER_HAS_TEAM = playerName -> SkyComponents.error("player_has_team", playerName);
    public static final MutableComponent ERROR_POSITION_TOO_FAR_AWAY = SkyComponents.error("position_too_far_away");
    public static final MutableComponent ERROR_PREVENT_WHILE_FALLING = SkyComponents.error("prevent_while_falling");
    public static final MutableComponent ERROR_REMOVE_SPAWN0 = SkyComponents.error("remove_spawn0");
    public static final MutableComponent ERROR_REMOVE_SPAWN1 = SkyComponents.error("remove_spawn1");
    public static final MutableComponent ERROR_SPREAD_NOT_EXIST = SkyComponents.error("spread_not_exist");
    public static final Function<String, MutableComponent> ERROR_TEAM_ALREADY_EXIST = teamName -> SkyComponents.error("team_already_exist", teamName);
    public static final MutableComponent ERROR_TEAM_NOT_EXIST = SkyComponents.error("team_not_exist");
    public static final MutableComponent ERROR_TELEPORT_ACROSS_DIMENSIONS = SkyComponents.error("teleport_across_dimensions");
    public static final MutableComponent ERROR_TELEPORTATION_NOT_ALLOWED_DIMENSION = SkyComponents.error("teleportation_not_allowed_dimension");
    public static final MutableComponent ERROR_USER_HAS_NO_TEAM = SkyComponents.error("user_has_no_team");
    public static final MutableComponent ERROR_USER_HAS_TEAM = SkyComponents.error("user_has_team");
    public static final MutableComponent ERROR_USER_NO_PLAYER = SkyComponents.error("user_no_player");
    public static final MutableComponent ERROR_VISIT_OWN_TEAM = SkyComponents.error("visit_own_team");
    public static final MutableComponent ERROR_WRONG_POSITION = SkyComponents.error("wrong_position");

    // Command Info
    public static final BiFunction<String, String, MutableComponent> INFO_INVITED_TO_TEAM0 = (playerName, teamName) -> SkyComponents.info("invited_to_team0", playerName, teamName);
    public static final MutableComponent INFO_INVITED_TO_TEAM1 = SkyComponents.info("invited_to_team1");
    public static final Function<String, MutableComponent> INFO_SHOW_TEAM_SPAWNS = teamName -> SkyComponents.info("show_team_spawns", teamName);
    public static final BiFunction<String, Integer, MutableComponent> INFO_TEAM_DETAILED = (teamName, memberCount) -> SkyComponents.info("team_detailed", teamName, memberCount);
    public static final BiFunction<Integer, Long, MutableComponent> INFO_TEAMS = (teamsCount, emptyTeamsCount) -> SkyComponents.info("teams", teamsCount, emptyTeamsCount);
    public static final Function<Component, MutableComponent> INFO_TOGGLE_REQUEST = arg -> SkyComponents.info("toggle_request", arg);
    public static final Function<Component, MutableComponent> INFO_TOGGLE_VISIT = arg -> SkyComponents.info("toggle_visit", arg);

    // Command Success
    public static final BiFunction<String, String, MutableComponent> SUCCESS_ADD_ONE_PLAYER = (playerName, teamName) -> SkyComponents.success("add_one_player", playerName, teamName);
    public static final BiFunction<Integer, String, MutableComponent> SUCCESS_ADD_MULTIPLE_PLAYERS = (playersAmount, teamName) -> SkyComponents.success("add_multiple_players", playersAmount, teamName);
    public static final BiFunction<String, String, MutableComponent> SUCCESS_CONVERT_TEMPLATE = (fileName, convertedName) -> SkyComponents.success("convert_template", fileName, convertedName);
    public static final Function<String, MutableComponent> SUCCESS_CREATE_TEAM = teamName -> SkyComponents.success("create_team", teamName);
    public static final Function<String, MutableComponent> SUCCESS_DECLINED_INVITATION = teamName -> SkyComponents.success("declined_invitation", teamName);
    public static final Function<Integer, MutableComponent> SUCCESS_DELETE_MULTIPLE_TEAMS = teamsAmount -> SkyComponents.success("delete_multiple_teams", teamsAmount);
    public static final Function<String, MutableComponent> SUCCESS_DELETE_ONE_TEAM = teamName -> SkyComponents.success("delete_one_team", teamName);
    public static final Function<String, MutableComponent> SUCCESS_DENY_REQUEST_ACCEPTED = teamName -> SkyComponents.success("deny_request_accepted", teamName);
    public static final Function<String, MutableComponent> SUCCESS_EXPORT_INVENTORY = path -> SkyComponents.success("export_inventory", path);
    public static final Function<String, MutableComponent> SUCCESS_JOIN_REQUEST = teamName -> SkyComponents.success("join_request", teamName);
    public static final Function<String, MutableComponent> SUCCESS_JOIN_REQUEST_ACCEPTED = teamName -> SkyComponents.success("join_request_accepted", teamName);
    public static final Function<String, MutableComponent> SUCCESS_JOINED_TEAM = teamName -> SkyComponents.success("joined_team", teamName);
    public static final MutableComponent SUCCESS_LEFT_TEAM = SkyComponents.success("left_team");
    public static final Function<String, MutableComponent> SUCCESS_LOCATED_SPREAD = spreadName -> SkyComponents.success("located_spread", spreadName);
    public static final Function<Integer, MutableComponent> SUCCESS_REMOVE_ALL_PLAYERS_FROM_TEAM = playersAmount -> SkyComponents.success("remove_all_players_from_team", playersAmount);
    public static final BiFunction<Integer, String, MutableComponent> SUCCESS_REMOVE_MULTIPLE_PLAYERS = (playersAmount, teamName) -> SkyComponents.success("remove_multiple_players", playersAmount, teamName);
    public static final Function<String, MutableComponent> SUCCESS_RENAME_TEAM = teamName -> SkyComponents.success("rename_team", teamName);
    public static final Function<String, MutableComponent> SUCCESS_RESET_ISLAND = templateName -> SkyComponents.success("reset_island", templateName);
    public static final MutableComponent SUCCESS_RESET_SPAWNS = SkyComponents.success("reset_spawns");
    public static final Function3<Integer, Integer, Integer, MutableComponent> SUCCESS_SPAWN_ADDED = (posX, posY, posZ) -> SkyComponents.success("spawn_added", posX, posY, posZ);
    public static final Function3<Integer, Integer, Integer, MutableComponent> SUCCESS_SPAWN_REMOVED = (posX, posY, posZ) -> SkyComponents.success("spawn_removed", posX, posY, posZ);
    public static final MutableComponent SUCCESS_TELEPORT_HOME = SkyComponents.success("teleport_home");
    public static final MutableComponent SUCCESS_TELEPORT_TO_SPAWN = SkyComponents.success("teleport_to_spawn");
    public static final Function<String, MutableComponent> SUCCESS_VISIT_TEAM = teamName -> SkyComponents.success("visit_team", teamName);

    // Command Warning
    public static final MutableComponent WARNING_EDIT_SPAWN_SPAWNS = SkyComponents.warning("edit_spawn_spawns");

    // Events
    public static final Function<String, MutableComponent> EVENT_ACCEPT_INVITE = playerName -> SkyComponents.event("accept_invite", playerName);
    public static final BiFunction<Component, Component, MutableComponent> EVENT_ACCEPT_JOIN_REQUEST = (acceptingPlayerName, acceptedPlayerName) -> SkyComponents.event("accept_join_request", acceptingPlayerName, acceptedPlayerName);
    public static final BiFunction<Component, Component, MutableComponent> EVENT_DENY_JOIN_REQUEST = (denyingPlayerName, deniedPlayerName) -> SkyComponents.event("deny_join_request", denyingPlayerName, deniedPlayerName);
    public static final BiFunction<Component, String, MutableComponent> EVENT_INVITE_PLAYER = (invitorName, playerName) -> SkyComponents.event("invite_player", invitorName, playerName);
    public static final Function<Component, MutableComponent> EVENT_JOIN_REQUEST0 = playerName -> SkyComponents.event("join_request0", playerName);
    public static final MutableComponent EVENT_JOIN_REQUEST1 = SkyComponents.event("join_request1");
    public static final Function<String, MutableComponent> EVENT_PLAYER_JOINED = playerName -> SkyComponents.event("player_joined", playerName);
    public static final Function<String, MutableComponent> EVENT_REMOVE_PLAYER = playerName -> SkyComponents.event("remove_player", playerName);
    public static final Function3<Component, String, String, MutableComponent> EVENT_RENAME_TEAM = (playerName, oldName, name) -> SkyComponents.event("rename_team", playerName, oldName, name);

    // Structure Saver Item
    public static final MutableComponent ITEM_STRUCTURE_SAVER_INFO_TOOLTIP = SkyComponents.structureSaverTooltip("info");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_SAVE_TOOLTIP = SkyComponents.structureSaverTooltip("save");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_RESTORE_TOOLTIP = SkyComponents.structureSaverTooltip("restore");
    public static final Function4<Integer, Integer, Integer, Integer, MutableComponent> ITEM_STRUCTURE_SAVER_POSITION_TOOLTIP = (i, posX, posY, posZ) -> SkyComponents.structureSaverTooltip("position", i, posX, posY, posZ);
    public static final Function4<Integer, Integer, Integer, Integer, MutableComponent> STRUCTURE_SAVER_POS = (i, posX, posY, posZ) -> SkyComponents.structureSaverDescription("position_set", i, posX, posY, posZ);
    public static final MutableComponent ITEM_STRUCTURE_SAVER_IGNORE_AIR_TOOLTIP = SkyComponents.structureSaverTooltip("ignore_air");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_NBT_TO_SNBT_TOOLTIP = SkyComponents.structureSaverTooltip("nbt_to_snbt");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_KEEP_POSITIONS_TOOLTIP = SkyComponents.structureSaverTooltip("keep_positions");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_SAVE_TO_CONFIG_TOOLTIP = SkyComponents.structureSaverTooltip("save_to_config");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_IGNORE_AIR_DESC = SkyComponents.structureSaverDescription("ignore_air");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_NBT_TO_SNBT_DESC = SkyComponents.structureSaverDescription("nbt_to_snbt");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_KEEP_POSITIONS_DESC = SkyComponents.structureSaverDescription("keep_positions");
    public static final MutableComponent ITEM_STRUCTURE_SAVER_SAVE_TO_CONFIG_DESC = SkyComponents.structureSaverDescription("save_to_config");

    // Screen
    public static final MutableComponent SCREEN_BUTTON_SAVE = SkyComponents.button("save");
    public static final MutableComponent SCREEN_BUTTON_DELETE = SkyComponents.button("delete");
    public static final MutableComponent SCREEN_BUTTON_OPEN_FOLDER_TOOLTIP = SkyComponents.button("open_folder.tooltip");
    public static final MutableComponent SCREEN_WIDGET_STRUCTURE_NAME = SkyComponents.widget("structure_name");
    public static final Function<String, MutableComponent> SCREEN_OPEN_FOLDER_ERROR = path -> SkyComponents.screen("open_folder.error", path);
    public static final MutableComponent SCREEN_STRUCTURE_SAVER_TAB_ISLAND = SkyComponents.structureSaverTooltip("island").withStyle(ChatFormatting.BLUE);
    public static final MutableComponent SCREEN_STRUCTURE_SAVER_TAB_SPREAD = SkyComponents.structureSaverTooltip("spread").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent SCREEN_STRUCTURE_SAVER_TAB_NETHER = SkyComponents.structureSaverTooltip("nether").withStyle(ChatFormatting.RED);

    // Screen Titles
    public static final MutableComponent SCREEN_STRUCTURE_SAVER = SkyComponents.screenTitle("structure_saver");
    public static final MutableComponent SCREEN_SELECT_PALETTE = SkyComponents.screenTitle("select_palette");
    public static final MutableComponent SCREEN_DUMP_TITLE = SkyComponents.screenTitle("dump");

    // Dump Screen
    public static final MutableComponent SCREEN_DUMP_TEXT_CONFIGS = SkyComponents.dumpText("configs");
    public static final MutableComponent SCREEN_DUMP_TEXT_TEMPLATES = SkyComponents.dumpText("templates");
    public static final MutableComponent SCREEN_DUMP_TEXT_LEVEL_DAT = SkyComponents.dumpText("level_dat");
    public static final MutableComponent SCREEN_DUMP_TEXT_LATEST_LOG = SkyComponents.dumpText("latest_log");
    public static final MutableComponent SCREEN_DUMP_TEXT_CRASH_REPORT = SkyComponents.dumpText("crash_report");
    public static final MutableComponent SCREEN_DUMP_TEXT_DATA_FILE = SkyComponents.dumpText("data_file");
    public static final MutableComponent SCREEN_DUMP_TEXT_CREATE_ON_SERVER = SkyComponents.dumpText("create_on_server");
    public static final MutableComponent SCREEN_DUMP_BUTTON_CREATE = SkyComponents.dump("button.create");
    public static final Function<String, MutableComponent> SCREEN_DUMP_SUCCESS = path -> SkyComponents.dump("success", path);
    public static final MutableComponent SCREEN_DUMP_SUCCESS_SERVER = SkyComponents.dump("success.server");
    public static final MutableComponent SCREEN_DUMP_CREATE_ISSUE = SkyComponents.dump("create_issue");
    public static final MutableComponent SCREEN_DUMP_FAILURE = SkyComponents.dump("failure").withStyle(ChatFormatting.RED);

    // Error Screen
    public static final MutableComponent SCREEN_ERROR_TITLE = SkyComponents.errorScreen("title");
    public static final MutableComponent SCREEN_ERROR_MESSAGE = SkyComponents.errorScreen("message");
    public static final MutableComponent SCREEN_ERROR_GIVE_BUTTON = SkyComponents.errorScreen("button.give");

    // Miscellaneous
    public static final Function<String, MutableComponent> SCHEMATIC_SAVED = path -> Component.translatable("skyblockbuilder.schematic.saved", path);
    public static final MutableComponent NO_SKYBLOCK = Component.translatable("skyblockbuilder.error.no_skyblock");
    public static final Function<String, MutableComponent> COMPAT_DISABLED_MANAGEMENT = modIds -> Component.translatable("skyblockbuilder.compat.disabled_management", modIds);

    // Integration Components
    public static final MutableComponent MINEMENTION_TEAM = Component.translatable("minemention.skyblockbuilder.team");
    public static final MutableComponent TASK_SPREAD_LOCATION = Component.translatable("task.skyblockbuilder.spread_location");
    public static final MutableComponent SETTING_SPREAD_LOCATION_TITLE = Component.translatable("setting.skyblockbuilder.spread_location.title");
    public static final MutableComponent SETTING_SPREAD_LOCATION_ICON = Component.translatable("setting.skyblockbuilder.spread_location.icon");
    public static final MutableComponent SETTING_SPREAD_LOCATION_SPREAD = Component.translatable("setting.skyblockbuilder.spread_location.spread");
    public static final MutableComponent SETTING_SPREAD_LOCATION_NO_VALID_SPREADS = Component.translatable("setting.skyblockbuilder.spread_location.no_valid_spreads");
    public static final MutableComponent SETTING_SPREAD_LOCATION_NO_SPREADS_DEFINED = Component.translatable("setting.skyblockbuilder.spread_location.no_spreads_defined");
    public static final MutableComponent SETTING_SPREAD_LOCATION_VISIT_SPREADS = Component.translatable("setting.skyblockbuilder.spread_location.visit_spreads");
    public static final MutableComponent TASK_SPREAD_LOCATION_TITLE_SINGULAR = Component.translatable("task.skyblockbuilder.spread_location.title.singular");
    public static final MutableComponent TASK_SPREAD_LOCATION_TITLE_PLURAL = Component.translatable("task.skyblockbuilder.spread_location.title.plural");
    public static final MutableComponent HERACLES_RESET_QUEST_PROGRESS = Component.translatable("heracles.skyblockbuilder.reset_quest_progress").withStyle(ChatFormatting.RED);
    public static final MutableComponent CADMUS_CLAIM_SPAWN = Component.translatable("cadmus.skyblockbuilder.claim_spawn");
}
