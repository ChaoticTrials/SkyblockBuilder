package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.SkyblockBuilder;

public class CompatHelper {
    public static boolean ALLOW_TEAM_MANAGEMENT = true;

    /**
     * Used to disable that users can modify anything about teams, e.g. creating new teams, leaving a team, renaming
     * a team, adding or removing spawnpoints...
     *
     * @param modid The modid of the mod which disables management
     */
    public static void disableAllTeamManagement(String modid) {
        ALLOW_TEAM_MANAGEMENT = false;
        SkyblockBuilder.LOGGER.warn(modid + " disabled all team management things.");
    }
}
