package de.melanx.skyblockbuilder.world.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.melanx.skyblockbuilder.util.Registration;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
public class SkyblockSavedData extends WorldSavedData {
    private static final String NAME = "skyblock_builder";

    /**
     * The offset is chosen to put islands under default settings in the center of a chunk region.
     */
    private static final int OFFSET = 1;

    public BiMap<IslandPos, Pair<Set<BlockPos>, Set<UUID>>> skyblocks = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData() {
        super(NAME);
    }

    public static SkyblockSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(SkyblockSavedData::new, NAME);
    }

    public IslandPos getSpawn() {
        for (Pair<Set<BlockPos>, Set<UUID>> value : this.skyblocks.values()) {
            if (value.getValue().contains(Util.DUMMY_UUID)) {
                return this.skyblocks.inverse().get(value);
            }
        }
        IslandPos pos = new IslandPos(OFFSET, OFFSET);

        Set<UUID> players = new HashSet<>();
        players.add(Util.DUMMY_UUID);

        skyblocks.put(pos, Pair.of(this.getPossibleSpawns(pos), players));
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

        Set<UUID> players = new HashSet<>();
        players.add(playerId);

        Set<BlockPos> positions = getPossibleSpawns(islandPos.getCenter());
        skyblocks.put(islandPos, Pair.of(positions, players));
        markDirty();
        return islandPos;
    }

    @Override
    public void read(CompoundNBT nbt) {
        HashBiMap<IslandPos, Pair<Set<BlockPos>, Set<UUID>>> map = HashBiMap.create();
        for (INBT inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;

            Set<BlockPos> positions = new HashSet<>();
            for (INBT spawn : tag.getList("Spawns", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT spawnTag = (CompoundNBT) spawn;
                positions.add(new BlockPos(spawnTag.getDouble("posX"), spawnTag.getDouble("posY"), spawnTag.getDouble("posZ")));
            }

            Set<UUID> players = new HashSet<>();
            for (INBT player : tag.getList("Players", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT playerTag = (CompoundNBT) player;
                players.add(playerTag.getUniqueId("Player"));
            }

            map.put(IslandPos.fromTag(tag), Pair.of(positions, players));
        }
        this.skyblocks = map;
        this.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT islands = new ListNBT();
        for (Map.Entry<IslandPos, Pair<Set<BlockPos>, Set<UUID>>> entry : skyblocks.entrySet()) {
            CompoundNBT entryTag = entry.getKey().toTag();

            ListNBT players = new ListNBT();
            for (UUID player : entry.getValue().getValue()) {
                CompoundNBT playerTag = new CompoundNBT();
                playerTag.putUniqueId("Player", player);

                players.add(playerTag);
            }

            ListNBT positions = new ListNBT();
            for (BlockPos spawn : entry.getValue().getKey()) {
                CompoundNBT spawnTag = new CompoundNBT();
                spawnTag.putDouble("posX", spawn.getX() + 0.5);
                spawnTag.putDouble("posY", spawn.getY());
                spawnTag.putDouble("posZ", spawn.getZ() + 0.5);

                positions.add(spawnTag);
            }

            entryTag.put("Players", players);
            entryTag.put("Spawns", positions);
            islands.add(entryTag);
        }

        nbt.putIntArray("SpiralState", spiral.toIntArray());
        nbt.put("Islands", islands);
        return nbt;
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (this.skyblocks.get(pos) == null) {
            return getPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(pos).getLeft();
    }

    private static Set<BlockPos> getPossibleSpawns(BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (Template.Palette info : TemplateLoader.TEMPLATE.blocks) {
            for (Template.BlockInfo shit : info.func_237157_a_()) {
                if (shit.state == Registration.SPAWN_BLOCK.get().getDefaultState()) {
                    positions.add(center.add(shit.pos.toImmutable()));
                }
            }
        }
        return positions;
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
