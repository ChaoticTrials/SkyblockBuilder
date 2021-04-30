package de.melanx.skyblockbuilder.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompatHelper {
    
    private static final List<String> teamManagementDisablingMods = new ArrayList<>();
    private static boolean spawnTeleportEnabled = true;

    /**
     * Used to disable that users can modify anything about teams, e.g. creating new teams, leaving a team, renaming
     * a team, adding or removing spawnpoints...
     *
     * @param modid The modid of the mod which disables management
     */
    public static void disableAllTeamManagement(String modid) {
        teamManagementDisablingMods.add(modid);
        teamManagementDisablingMods.sort(Comparator.naturalOrder());
        SkyblockBuilder.getLogger().warn(modid + " disabled all team management features.");
    }

    public static void disableSpawnTeleport(String modid) {
        spawnTeleportEnabled = false;
        SkyblockBuilder.getLogger().warn(modid + " disabled teleporting to spawn on world join.");
    }

    public static void checkTeamManagement() throws CommandSyntaxException {
        if (!teamManagementDisablingMods.isEmpty()) {
            throw new SimpleCommandExceptionType(new TranslationTextComponent("skyblockbuilder.compat.disabled_management", String.join(", ", teamManagementDisablingMods))).create();
        }
    }

    public static boolean teamManagementEnabled() {
        return teamManagementDisablingMods.isEmpty();
    }

    public static boolean isSpawnTeleportEnabled() {
        return spawnTeleportEnabled;
    }
}
