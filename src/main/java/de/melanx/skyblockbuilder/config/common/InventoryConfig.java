package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig("inventory")
public class InventoryConfig {

    @Config({"Should all items be reset on first world join? [default: false]",
            "This will delete all the items given on spawn from other mods guide books."})
    public static boolean clearInv = false;

    @Config("Should players' items be dropped when leaving a team? [default: true]")
    public static boolean dropItems = true;
}
