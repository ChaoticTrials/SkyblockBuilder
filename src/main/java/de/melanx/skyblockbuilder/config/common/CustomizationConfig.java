package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig("customization")
public class CustomizationConfig {

    @Config({"This will add the team name in front of the name of each player.",
            "Obviously, this will only work on a server where you can press tab for the player list."})
    public static boolean showTeamInTabList = true;

    @Config({"If the last player leaves a team, the team will automatically be deleted",
            "The island and all its blocks remain at this position!",
            "This position will not be used for any new team, only the remaining team will be deleted."})
    public static boolean deleteTeamsAutomatically = false;

    @Config("You may enable this when you encounter problems with game profiles")
    public static boolean forceUnsecureProfileNames = false;
}
