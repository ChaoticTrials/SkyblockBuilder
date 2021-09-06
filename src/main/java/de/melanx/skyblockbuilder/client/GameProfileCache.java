package de.melanx.skyblockbuilder.client;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameProfileCache {

    private static final Map<UUID, GameProfile> uuidProfiles = Maps.newHashMap();
    private static final Map<String, GameProfile> nameProfiles = Maps.newHashMap();

    public static void addProfiles(Set<GameProfile> profiles) {
        for (GameProfile profile : profiles) {
            uuidProfiles.put(profile.getId(), profile);
            nameProfiles.put(profile.getName().toLowerCase(Locale.ROOT), profile);
        }
    }

    public static GameProfile get(UUID id) {
        return uuidProfiles.get(id);
    }

    public static GameProfile get(String name) {
        return nameProfiles.get(name.toLowerCase(Locale.ROOT));
    }

    public static String getName(UUID id) {
        return uuidProfiles.get(id).getName();
    }

    public static UUID getId(String name) {
        return nameProfiles.get(name.toLowerCase(Locale.ROOT)).getId();
    }
}
