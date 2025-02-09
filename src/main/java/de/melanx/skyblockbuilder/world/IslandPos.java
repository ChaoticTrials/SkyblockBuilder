package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    private final int x;
    private final int z;
    private BlockPos center;

    public IslandPos(Level level, int x, int z, ConfiguredTemplate template) {
        this(x, Mth.clamp(WorldUtil.calcSpawnHeight(level, x, z) + template.getOffset().y(), level.getMinBuildHeight(), level.getMaxBuildHeight()), z, template.getOffset());
    }

    public IslandPos(int x, int y, int z, ConfiguredTemplate template) {
        this(x, y, z, template.getOffset());
    }

    public IslandPos(int x, int y, int z, TemplateInfo.Offset offset) {
        this.x = x;
        this.z = z;
        this.center = new BlockPos(this.x * WorldConfig.islandDistance + offset.x() + TemplatesConfig.defaultOffset, y, this.z * WorldConfig.islandDistance + offset.z() + TemplatesConfig.defaultOffset);
    }

    private IslandPos(int x, int z, BlockPos center) {
        this.x = x;
        this.z = z;
        this.center = center;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void changeHeight(int y) {
        this.center = this.center.atY(y);
    }

    public static IslandPos fromTag(CompoundTag tag) {
        //noinspection OptionalGetWithoutIsPresent
        return new IslandPos(
                tag.getInt("IslandX"),
                tag.getInt("IslandZ"),
                NbtUtils.readBlockPos(tag, "CenterPos").get()
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("IslandX", this.x);
        tag.putInt("IslandZ", this.z);
        tag.put("CenterPos", NbtUtils.writeBlockPos(this.center));
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof IslandPos)) {
            return false;
        }
        IslandPos islandPos = (IslandPos) o;
        return this.x == islandPos.x && this.z == islandPos.z;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.z;
        return result;
    }
}
