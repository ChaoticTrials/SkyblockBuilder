package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.moddingx.libx.annotation.meta.RemoveIn;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    private final int x;
    private final int z;
    private BlockPos center;
    private final TemplateInfo.Offset offset;

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public IslandPos(Level level, int x, int z) {
        this(x, Mth.clamp(WorldUtil.calcSpawnHeight(level, x, z) + TemplateLoader.getConfiguredTemplate().getOffsetY(), level.getMinBuildHeight(), level.getMaxBuildHeight()), z, TemplateLoader.getConfiguredTemplate());
    }

    public IslandPos(Level level, int x, int z, ConfiguredTemplate template) {
        this(x, Mth.clamp(WorldUtil.calcSpawnHeight(level, x, z) + template.getOffsetY(), level.getMinBuildHeight(), level.getMaxBuildHeight()), z, template.getOffset());
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public IslandPos(int x, int y, int z) {
        this(x, y, z, TemplateLoader.getConfiguredTemplate());
    }

    public IslandPos(int x, int y, int z, ConfiguredTemplate template) {
        this(x, y, z, template.getOffset());
    }

    public IslandPos(int x, int y, int z, TemplateInfo.Offset offset) {
        this.x = x;
        this.z = z;
        this.offset = offset;
        this.center = new BlockPos(this.x * ConfigHandler.World.islandDistance + offset.x(), y, this.z * ConfigHandler.World.islandDistance + offset.z());
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void changeHeight(int y) {
        this.center = this.center.atY(y);
    }

    public static IslandPos fromTag(CompoundTag tag) { // TODO 1.20 fix the offset
        return new IslandPos(tag.getInt("IslandX"), tag.getInt("Height"), tag.getInt("IslandZ"), new TemplateInfo.Offset(tag.contains("OffsetX") ? tag.getInt("OffsetX") : ConfigHandler.World.offset, tag.contains("OffsetZ") ? tag.getInt("OffsetZ") : ConfigHandler.World.offset));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("IslandX", this.x);
        tag.putInt("IslandZ", this.z);
        tag.putInt("Height", this.center.getY());
        tag.putInt("OffsetX", this.offset.x());
        tag.putInt("OffsetZ", this.offset.z());
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
