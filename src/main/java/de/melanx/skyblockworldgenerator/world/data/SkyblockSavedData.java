package de.melanx.skyblockworldgenerator.world.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.melanx.skyblockworldgenerator.world.IslandPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
public class SkyblockSavedData extends WorldSavedData {
    private static final String NAME = "custom_skyblock_islands";

    /**
     * The offset is chosen to put islands under default settings in the center of a chunk region.
     */
    private static final int OFFSET = 1;

    public BiMap<IslandPos, UUID> skyblocks = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData() {
        super(NAME);
    }

    public static SkyblockSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(SkyblockSavedData::new, NAME);
    }

    public IslandPos getSpawn() {
        if (skyblocks.containsValue(Util.DUMMY_UUID)) {
            return skyblocks.inverse().get(Util.DUMMY_UUID);
        }
        IslandPos pos = new IslandPos(OFFSET, OFFSET);
        skyblocks.put(pos, Util.DUMMY_UUID);
        markDirty();
        return pos;
    }

    public IslandPos create(UUID playerId) {
        int scale = 8;
        IslandPos islandPos;
        do {
            int[] pos = spiral.next();
            islandPos = new IslandPos(pos[0] * scale + OFFSET, pos[1] * scale + OFFSET);
        } while (skyblocks.containsKey(islandPos));

        skyblocks.put(islandPos, playerId);
        markDirty();
        return islandPos;
    }

    @Override
    public void read(CompoundNBT nbt) {
        HashBiMap<IslandPos, UUID> map = HashBiMap.create();
        for (INBT inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;
            map.put(IslandPos.fromTag(tag), tag.getUniqueId("Player"));
        }
        this.skyblocks = map;
        this.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Map.Entry<IslandPos, UUID> entry : skyblocks.entrySet()) {
            CompoundNBT entryTag = entry.getKey().toTag();
            entryTag.putUniqueId("Player", entry.getValue());
            list.add(entryTag);
        }
        nbt.putIntArray("SpiralState", spiral.toIntArray());
        nbt.put("Islands", list);
        return nbt;
    }

    // Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
    private static class Spiral {
        private int x = 0;
        private int y = 0;
        private int dx = 0;
        private int dy = -1;

        Spiral() {
        }

        Spiral(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        int[] next() {
            if (x == y || x < 0 && x == -y || x > 0 && x == 1 - y) {
                int t = dx;
                dx = -dy;
                dy = t;
            }
            x += dx;
            y += dy;
            return new int[]{x, y};
        }

        int[] toIntArray() {
            return new int[]{x, y, dx, dy};
        }

        static Spiral fromArray(int[] ints) {
            return new Spiral(ints[0], ints[1], ints[2], ints[3]);
        }
    }
}
