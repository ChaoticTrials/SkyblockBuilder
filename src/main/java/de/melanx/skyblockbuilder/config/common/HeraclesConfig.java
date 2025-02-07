package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.compat.heracles.HeraclesCompat;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(value = "compatability/" + HeraclesCompat.MODID, requiresMod = HeraclesCompat.MODID)
public class HeraclesConfig {

    @Config("If a player has no team, or the team does not have any spread, should this complete the task without visiting required locations?")
    public static boolean skipNonExistingSpreads = true;

    @Config("Reset quest progress for non-op players when leaving a team")
    public static boolean resetQuestProgress = false;
}
