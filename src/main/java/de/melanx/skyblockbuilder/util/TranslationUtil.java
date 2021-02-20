package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.SkyblockBuilder;

public class TranslationUtil {
    public static String getTranslationKey(String extension) {
        return SkyblockBuilder.MODID + "." + extension;
    }

    public static String getCommandKey(String command) {
        return getTranslationKey("command." + command);
    }

    public static String getCommandInfoKey(String command) {
        return getCommandKey(command + ".info");
    }

    public static String getCommandErrorKey(String errorInfo) {
        return getCommandKey("error." + errorInfo);
    }
}
