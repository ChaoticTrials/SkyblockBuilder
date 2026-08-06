package de.melanx.skyblockbuilder.client;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.Util;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameProfileCache {

    private static final Map<UUID, GameProfile> uuidProfiles = Maps.newHashMap();
    private static final Map<String, GameProfile> nameProfiles = Maps.newHashMap();

    public static void addProfiles(Set<GameProfile> profiles) {
        for (GameProfile profile : profiles) {
            uuidProfiles.put(profile.id(), profile);
            nameProfiles.put(profile.name().toLowerCase(Locale.ROOT), profile);
        }
    }

    public static GameProfile get(UUID id) {
        return uuidProfiles.get(id);
    }

    public static GameProfile get(String name) {
        return nameProfiles.get(name.toLowerCase(Locale.ROOT));
    }

    public static String getName(UUID id) {
        GameProfile profile = uuidProfiles.get(id);
        if (profile == null) {
            return "";
        }

        return profile.name();
    }

    public static UUID getId(String name) {
        GameProfile profile = nameProfiles.get(name.toLowerCase(Locale.ROOT));
        if (profile == null) {
            return Util.NIL_UUID;
        }

        return profile.id();
    }
}
