package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig("customization")
public class CustomizationConfig {

    @Config({"This will add the team name in front of the name of each player.",
            "Obviously, this will only work on a server where you can press tab for the player list."})
    public static boolean showTeamInTabList = true;
}
